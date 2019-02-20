package de.vanmar.android.hoebapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import org.androidannotations.annotations.*;
import org.androidannotations.annotations.sharedpreferences.Pref;
import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.bo.Account.Appearance;
import de.vanmar.android.hoebapp.bo.RenewItem;
import de.vanmar.android.hoebapp.db.MediaContentProvider;
import de.vanmar.android.hoebapp.db.MediaDbHelper;
import de.vanmar.android.hoebapp.service.LoginFailedException;
import de.vanmar.android.hoebapp.service.SoapLibraryService;
import de.vanmar.android.hoebapp.service.TechnicalException;
import de.vanmar.android.hoebapp.util.NetworkHelper;
import de.vanmar.android.hoebapp.util.Preferences_;
import de.vanmar.android.hoebapp.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@EActivity
@OptionsMenu(R.menu.menu)
public class HoebAppActivity extends FragmentActivity implements
		LoaderCallbacks<Cursor> {

	private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat(
			"dd.MM.yyyy", Locale.GERMAN);

	public static final int EULA_VERSION = 1;

	@Pref
	Preferences_ prefs;

	@Bean
	NetworkHelper networkHelper;

	@ViewById(R.id.titlebar)
	TextView titlebar;

	ListView medialist;

	private SimpleCursorAdapter adapter;

	@Bean
	SoapLibraryService soapLibraryService;

	boolean libraryServiceBound = false;

	private final Set<RenewItem> renewList = new HashSet<RenewItem>();

	private final OnClickListener checkboxListener = new OnClickListener() {

		@Override
		public void onClick(final View view) {
			if (view instanceof CheckBox) {
				final CheckBox checkbox = (CheckBox) view;
				if (checkbox.isChecked()) {
					renewList.add((RenewItem) checkbox.getTag());
				} else {
					renewList.remove(checkbox.getTag());
				}
			}
		}
	};

	private List<Account> accounts;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		displayEulaDialog();

		initList();

		MobileAds.initialize(this, "ca-app-pub-9064469114375841~7925558812");
	}

	@Override
	protected void onStart() {
		super.onStart();

		loadAds();
	}

	private void loadAds() {
		AdView adView = (AdView) this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
	}

	@Override
	protected void onResume() {
		super.onResume();
		final List<Account> accountPreferences = Account.fromString(prefs
				.accounts().get());
		if (accounts == null || !accounts.equals(accountPreferences)) {
			this.accounts = accountPreferences;
			refreshList();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isTaskRoot()) {
			AQUtility.cleanCacheAsync(this, 0, 0);
		}
	}

	@OptionsItem(R.id.renew)
	void doRenew() {
		if (networkHelper.networkAvailable()) {
			final List<Account> accounts = Account.fromString(prefs.accounts()
					.get());
			if (accounts.isEmpty()) {
				displayWarning(getString(R.string.usernameNotSet));
				doSettings();
				return;
			}
			final ProgressDialog dialog = ProgressDialog.show(this, "",
					getString(R.string.renewWaitMessage), true);

			if (renewList.isEmpty()) {
				final AlertDialog alertDialog = new AlertDialog.Builder(this)
						.setTitle(R.string.renew)
						.setMessage(R.string.renewNoItemsSelected)
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(final DialogInterface dialog,
														final int id) {
										dialog.cancel();
									}
								}
						)
						.create();

				alertDialog.show();
				return;
			}

			executeRenewInBackground(dialog);
		} else {
			Toast.makeText(this, R.string.networkNotAvailable,
					Toast.LENGTH_LONG).show();
		}

	}

	@Background
	void executeRenewInBackground(final ProgressDialog dialog) {
		try {
			soapLibraryService.renewMedia(renewList, this);
			dialog.dismiss();
		} catch (final Exception e) {
			displayError(e);
			dialog.cancel();
		}
	}

	@OptionsItem(R.id.refresh)
	void doRefresh() {
		if (networkHelper.networkAvailable()) {
			loginAndGetMedia();
		} else {
			Toast.makeText(this, R.string.networkNotAvailable,
					Toast.LENGTH_LONG).show();
		}
	}

	@OptionsItem(R.id.preferences)
	void doSettings() {
		this.startActivity(new Intent(this, PreferencesActivity_.class));
	}

	@OptionsItem(R.id.locations)
	void doLocations() {
		this.startActivity(new Intent(this, LocationsActivity_.class));
	}

	@OptionsItem(R.id.notepad)
	void doNotepad() {
		this.startActivity(new Intent(this, NotepadActivity_.class));
	}

	@OptionsItem(R.id.search)
	void doSearch() {
		this.startActivity(new Intent(this, SearchActivity_.class));
	}

	@Override
	public boolean onSearchRequested() {
		doSearch();
		return true;
	}

	@OptionsItem(R.id.help)
	void doHelp() {
		this.startActivity(new Intent(this, HelpActivity_.class));
	}

	@OptionsItem(R.id.about)
	void doAbout() {
		this.startActivity(new Intent(this, AboutActivity_.class));
	}

	@Background
	void loginAndGetMedia() {
		try {
			final List<Account> accounts = Account.fromString(prefs.accounts()
					.get());
			if (accounts.isEmpty()) {
				displayWarning(getString(R.string.usernameNotSet));
				doSettings();
				return;
			}
			displayInTitle(getString(R.string.pleaseWait));
			//libraryService.refreshMediaList(this);
			soapLibraryService.refreshMediaList(this);
		} catch (final Exception e) {
			displayError(e);
			displayTitleCount(medialist.getCount());
		}
	}

	@UiThread
	void displayInTitle(final String text) {
		titlebar.setText(text);
	}

	@UiThread
	void displayWarning(final String warning) {
		Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
	}

	private void displayTitleCount(final int count) {
		final long lastAccess = prefs.lastAccess().get();
		String title;
		if (lastAccess == 0) {
			title = getString(R.string.titleCount, count);
		} else {
			title = String.format(getString(R.string.titleCountAndDate), count,
					DISPLAY_DATE_FORMAT.format(new Date(lastAccess)));
		}
		displayInTitle(title);
	}

	@UiThread
	void displayError(final Exception exception) {
		if (exception instanceof LoginFailedException) {
			Toast.makeText(this, R.string.loginfailed, Toast.LENGTH_SHORT)
					.show();
			Log.w(HoebAppActivity.class.getCanonicalName(), "LoginFailedException");
		} else if (exception instanceof TechnicalException) {
			if (exception.getCause() instanceof IOException) {
				Toast.makeText(this, R.string.ioException, Toast.LENGTH_SHORT)
						.show();
				Log.e(HoebAppActivity.class.getCanonicalName(), "IOException: "
								+ exception.getClass() + exception.getMessage(),
						exception
				);
			} else {
				Toast.makeText(this, R.string.technicalError,
						Toast.LENGTH_SHORT).show();
				Log.e(HoebAppActivity.class.getCanonicalName(), "TechnicalException: "
								+ exception.getClass() + exception.getMessage(),
						exception
				);
			}
		}
	}

	private void initList() {
		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
		final String[] from = new String[]{};
		// Fields on the UI to which we map
		final int[] to = new int[]{};

		getSupportLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(this, R.layout.medialist_item, null,
				from, to, 0) {
			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				AQuery aq = new AQuery(view);
				aq.find(R.id.title).text(cursor.getString(MediaDbHelper.KEY_TITLE));
				String dueDate = DISPLAY_DATE_FORMAT.format(new Date(cursor.getLong(MediaDbHelper.KEY_DUEDATE)));
				CheckBox checkBox = aq.find(R.id.checkBox).getCheckBox();

				boolean canRenew = cursor.getInt(MediaDbHelper.KEY_CAN_RENEW) == 1;
				checkBox.setEnabled(canRenew);
				if (canRenew) {
					aq.find(R.id.dueDate).text(dueDate);
				} else {
					String noRenewReason = cursor.getString(MediaDbHelper.KEY_NO_RENEW_REASON);
					if (StringUtils.isEmpty(noRenewReason)) {
						aq.find(R.id.dueDate).text(String.format(getString(R.string.dateNotRenewable), dueDate));
					} else {
						aq.find(R.id.dueDate).text(String.format(getString(R.string.dateNotRenewableReason), dueDate, noRenewReason));
					}
				}
				final String signature = cursor
						.getString(MediaDbHelper.KEY_SIGNATURE);
				final String username = cursor
						.getString(MediaDbHelper.KEY_ACCOUNT);
				final RenewItem renewItem = new RenewItem(getAccount(username), signature);
				checkBox.setTag(renewItem);
				checkBox.setChecked(canRenew && renewList.contains(renewItem));
				checkBox.setOnClickListener(checkboxListener);

				view.setBackgroundResource(getResourceForAccount(username));
			}
		};
		/*adapter.setViewBinder(new ViewBinder() {


					final String signature = cursor
							.getString(MediaDbHelper.KEY_SIGNATURE);
					final String username = cursor
							.getString(MediaDbHelper.KEY_ACCOUNT);
					final CheckBox checkbox = (CheckBox) view;
					final boolean renewPossible = cursor.getInt(columnIndex) == 1;
					checkbox.setEnabled(renewPossible);
					final RenewItem renewItem = new RenewItem(username,
							signature);
					checkbox.setTag(renewItem);
					checkbox.setChecked(renewPossible
							&& renewList.contains(renewItem));
					checkbox.setOnClickListener(checkboxListener);
					return true;
				} else if (columnIndex == MediaDbHelper.KEY_ACCOUNT) {
					final String username = cursor
							.getString(MediaDbHelper.KEY_ACCOUNT);
					final LinearLayout layout = (LinearLayout) view;
					layout.setBackgroundResource(getResourceForAccount(username));
					return true;
				}
				return false;
			}
		});
*/
		medialist = (ListView) findViewById(R.id.medialist);

		medialist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent,
									final View view, final int position, final long id) {

				final Cursor item = (Cursor) adapter.getItem(position);
				final String mediumId = item
						.getString(MediaDbHelper.KEY_MEDIUM_ID);

				if (mediumId != null) {
					final Intent intent = new Intent(HoebAppActivity.this,
							DetailActivity_.class);
					intent.putExtra(DetailActivity.EXTRA_MEDIUM_ID, mediumId);
					startActivity(intent);
				}
			}
		});
		medialist.setAdapter(adapter);
		refreshList();
	}

	private void refreshList() {
		getContentResolver().notifyChange(MediaContentProvider.CONTENT_URI,
				null);
	}

	private void displayEulaDialog() {
		final int acceptedEulaVersion = prefs.acceptedEULA().get();
		if (acceptedEulaVersion < EULA_VERSION) {

			final WebView aboutView = new WebView(this);
			aboutView.loadUrl("file:///android_asset/html/about.html");
			final ScrollView scrollView = new ScrollView(this);
			scrollView.addView(aboutView);
			final AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.eulaHeading)
					.setView(scrollView)
					.setPositiveButton(R.string.accept,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									prefs.acceptedEULA().put(EULA_VERSION);
									dialog.cancel();
								}
							}
					)
					.setNegativeButton(R.string.decline,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									HoebAppActivity.this.finish();
								}
							}
					).create();

			dialog.show();
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		final String[] projection = MediaDbHelper.ALL_COLUMNS;
		final CursorLoader cursorLoader = new CursorLoader(this,
				MediaContentProvider.CONTENT_URI, projection, null, null,
				MediaDbHelper.COLUMN_DUEDATE);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
		final int count = cursor.getCount();
		displayTitleCount(count);
		renewList.clear();
		adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		// data is not available anymore, delete reference
		adapter.swapCursor(null);
	}

	private int getResourceForAccount(final String username) {
		for (final Account account : accounts) {
			if (account.getUsername().equals(username)) {
				return account.getAppearance().getDrawable();
			}
		}
		return Appearance.NONE.getDrawable();
	}

	private Account getAccount(final String username) {
		for (final Account account : accounts) {
			if (account.getUsername().equals(username)) {
				return account;
			}
		}
		return null;
	}

}
