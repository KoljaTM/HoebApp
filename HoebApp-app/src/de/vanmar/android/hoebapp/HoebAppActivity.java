package de.vanmar.android.hoebapp;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.bo.Account.Appearance;
import de.vanmar.android.hoebapp.bo.RenewItem;
import de.vanmar.android.hoebapp.db.MediaContentProvider;
import de.vanmar.android.hoebapp.db.MediaDbHelper;
import de.vanmar.android.hoebapp.service.LibraryService;
import de.vanmar.android.hoebapp.service.LoginFailedException;
import de.vanmar.android.hoebapp.service.TechnicalException;
import de.vanmar.android.hoebapp.util.NetworkHelper;
import de.vanmar.android.hoebapp.util.Preferences_;

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

	@ViewById(R.id.adView)
	AdView adView;

	ListView medialist;

	private SimpleCursorAdapter adapter;

	@Bean
	LibraryService libraryService;

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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		displayEulaDialog();

		initList();
	}

	@Override
	protected void onStart() {
		super.onStart();
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
				doRenewAll(dialog);
				return;
			}

			executeRenewInBackground(dialog);
		} else {
			Toast.makeText(this, R.string.networkNotAvailable,
					Toast.LENGTH_LONG).show();
		}

	}

	private void doRenewAll(final ProgressDialog progressDialog) {
		final AlertDialog confirmDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.renew)
				.setMessage(R.string.renewAllHint)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
								executeRenewAllInBackground(progressDialog);
							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
								progressDialog.cancel();
							}
						}).create();

		confirmDialog.show();
	}

	@Background
	void executeRenewInBackground(final ProgressDialog dialog) {
		try {
			libraryService.renewMedia(renewList, this);
			dialog.dismiss();
			loadAds();
		} catch (final Exception e) {
			displayError(e);
			dialog.cancel();
		}
	}

	@Background
	void executeRenewAllInBackground(final ProgressDialog dialog) {
		try {
			libraryService.renewAllMedia(this);
			dialog.dismiss();
			loadAds();
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
			libraryService.refreshMediaList(this);
			loadAds();
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
			Log.w(getClass().getCanonicalName(), "LoginFailedException");
		} else if (exception instanceof TechnicalException) {
			if (exception.getCause() instanceof IOException) {
				Toast.makeText(this, R.string.ioException, Toast.LENGTH_SHORT)
						.show();
				Log.e(getClass().getCanonicalName(), "IOException: "
						+ exception.getClass() + exception.getMessage(),
						exception);
			} else {
				Toast.makeText(this, R.string.technicalError,
						Toast.LENGTH_SHORT).show();
				Log.e(getClass().getCanonicalName(), "TechnicalException: "
						+ exception.getClass() + exception.getMessage(),
						exception);
			}
		}
	}

	@UiThread
	void loadAds() {
		final AdRequest adRequest = new AdRequest();
		adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
		adView.loadAd(adRequest);
	}

	private void initList() {
		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
		final String[] from = new String[] { MediaDbHelper.COLUMN_TITLE,
				MediaDbHelper.COLUMN_DUEDATE, MediaDbHelper.COLUMN_RENEW_LINK,
				MediaDbHelper.COLUMN_ACCOUNT };
		// Fields on the UI to which we map
		final int[] to = new int[] { R.id.title, R.id.dueDate, R.id.checkBox,
				R.id.tableLayout };

		getSupportLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(this, R.layout.medialist_item, null,
				from, to, 0);
		adapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(final View view, final Cursor cursor,
					final int columnIndex) {

				if (columnIndex == MediaDbHelper.KEY_DUEDATE) {
					final long dueDate = cursor.getLong(columnIndex);
					final TextView textView = (TextView) view;
					if (cursor.getString(MediaDbHelper.KEY_RENEW_LINK) == null) {
						// item can not be renewed
						final String noRenewReasonString = cursor
								.getString(MediaDbHelper.KEY_NO_RENEW_REASON);
						if (noRenewReasonString == null) {
							textView.setText(getString(
									R.string.dateNotRenewable,
									DISPLAY_DATE_FORMAT
											.format(new Date(dueDate))));
						} else {
							textView.setText(String.format(
									getString(R.string.dateNotRenewableReason),
									DISPLAY_DATE_FORMAT
											.format(new Date(dueDate)),
									noRenewReasonString));
						}
					} else {
						// item can be renewed
						textView.setText(DISPLAY_DATE_FORMAT.format(new Date(
								dueDate)));
					}
					return true;
				} else if (columnIndex == MediaDbHelper.KEY_RENEW_LINK) {
					final String renewName = cursor.getString(columnIndex);
					final String signature = cursor
							.getString(MediaDbHelper.KEY_SIGNATURE);
					final String username = cursor
							.getString(MediaDbHelper.KEY_ACCOUNT);
					final CheckBox checkbox = (CheckBox) view;
					final boolean renewPossible = renewName != null;
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

		medialist = (ListView) findViewById(R.id.medialist);
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
							})
					.setNegativeButton(R.string.decline,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									HoebAppActivity.this.finish();
								}
							}).create();

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

}
