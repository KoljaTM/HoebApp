package de.vanmar.android.hoebapp;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdView;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.bo.MediaDetails;
import de.vanmar.android.hoebapp.service.LibraryService;
import de.vanmar.android.hoebapp.service.LoginFailedException;
import de.vanmar.android.hoebapp.service.TechnicalException;
import de.vanmar.android.hoebapp.util.NetworkHelper;
import de.vanmar.android.hoebapp.util.Preferences_;

@EActivity(R.layout.notepad)
public class NotepadActivity extends FragmentActivity {
	
	@Pref
	Preferences_ prefs;

	@ViewById(R.id.adView)
	AdView adView;

	@ViewById(R.id.notepadlist)
	ListView notepadList;

	@ViewById(R.id.titlebar)
	TextView titlebar;

	@Bean
	LibraryService libraryService;

	@Bean
	NetworkHelper networkHelper;

	private ArrayAdapter<MediaDetails> notepadAdapter;
	private List<Account> accounts;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		loadNotepadList();
	}

	@Override
	protected void onResume() {
		super.onResume();

		final List<Account> accountPreferences = Account.fromString(prefs
				.accounts().get());
		if (accounts == null || !accounts.equals(accountPreferences)) {
			this.accounts = accountPreferences;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Another activity is taking focus (this activity is about to be
		// "paused").
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Background
	void loadNotepadList(){
		loadNotepad();
	}


	@AfterViews
	void setupListAdapter() {
		notepadAdapter = new ArrayAdapter<MediaDetails>(this,
				R.layout.searchresultlist_item) {

			@Override
			public View getView(final int position, final View convertView,
					final ViewGroup parent) {
				final View view = getLayoutInflater().inflate(
						R.layout.searchresultlist_item, null);
				final MediaDetails item = getItem(position);
				((TextView) view.findViewById(R.id.title)).setText(item
						.getTitle());
				((TextView) view.findViewById(R.id.author)).setText(item
						.getAuthor());
				final StringBuffer type = new StringBuffer();
				if (item.getType() != null) {
					type.append(item.getType());
				}

				final LinearLayout layout = (LinearLayout) view;
				layout.setBackgroundResource(item.getOwner().getAppearance().getDrawable());

				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(final View v) {
						final Intent intent = new Intent(NotepadActivity.this,
								DetailActivity_.class);
						intent.putExtra(DetailActivity.EXTRA_MEDIUM_ID,
								item.getId());
						startActivity(intent);
					}
				});

				return view;
			}

		};
		notepadList.setAdapter(notepadAdapter);
	}

	@Background
	void loadNotepad() {
		try {
			displayInTitle(getString(R.string.pleaseWait));
			final List<MediaDetails> searchMedia = libraryService.loadNotepad();
			displaySearchResults(searchMedia);
			displayInTitle("");
		} catch (final Exception e) {
			displayError(e);
		}
	}

	@UiThread
	void displayInTitle(final String text) {
		titlebar.setText(text);
	}

	@UiThread
	void displaySearchResults(final List<MediaDetails> searchMedia) {
		notepadAdapter.clear();
		for (final MediaDetails item : searchMedia) {
			notepadAdapter.add(item);
		}
	}

	@UiThread
	void displayError(final Exception exception) {
		if (exception instanceof LoginFailedException) {
			Toast.makeText(this, R.string.loginfailed, Toast.LENGTH_SHORT)
					.show();
			Log.w(getClass().getCanonicalName(), "LoginFailedException");
		} else if (exception instanceof TechnicalException) {
			Toast.makeText(this, R.string.technicalError, Toast.LENGTH_SHORT)
					.show();
			Log.e(getClass().getCanonicalName(), "TechnicalException: "
					+ exception.getClass() + exception.getMessage());
		}
	}
}
