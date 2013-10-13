package de.vanmar.android.hoebapp;

import java.util.List;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

import de.vanmar.android.hoebapp.bo.SearchMedia;
import de.vanmar.android.hoebapp.service.LibraryService;
import de.vanmar.android.hoebapp.service.LoginFailedException;
import de.vanmar.android.hoebapp.service.TechnicalException;
import de.vanmar.android.hoebapp.util.NetworkHelper;

@EActivity(R.layout.notepad)
public class NotepadActivity extends FragmentActivity {

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

	private ArrayAdapter<SearchMedia> notepadAdapter;
	
	@Override
	protected void onStart() {
		super.onStart();
	
		loadNotepadList();
	}
	
	@Background
	void loadNotepadList(){
		loadNotepad();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// The activity has become visible (it is now "resumed").
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

	@AfterViews
	void setupListAdapter() {
		notepadAdapter = new ArrayAdapter<SearchMedia>(this,
				R.layout.searchresultlist_item) {
			@Override
			public View getView(final int position, final View convertView,
					final ViewGroup parent) {
				final View view = getLayoutInflater().inflate(
						R.layout.searchresultlist_item, null);
				final SearchMedia item = getItem(position);
				((TextView) view.findViewById(R.id.title)).setText(item
						.getTitle());
				((TextView) view.findViewById(R.id.author)).setText(item
						.getAuthor());
				final StringBuffer type = new StringBuffer();
				if (item.getType() != null) {
					type.append(item.getType());
				}
				if (item.getYear() != null) {
					type.append(getString(R.string.yearFormatted,
							item.getYear()));
				}
				((TextView) view.findViewById(R.id.type)).setText(type);

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
			final List<SearchMedia> searchMedia = libraryService.loadNotepad();
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
	void displaySearchResults(final List<SearchMedia> searchMedia) {
		notepadAdapter.clear();
		for (final SearchMedia item : searchMedia) {
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
