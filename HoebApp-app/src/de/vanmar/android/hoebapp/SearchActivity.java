package de.vanmar.android.hoebapp;

import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.androidquery.AQuery;
import com.google.ads.AdView;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;

import de.vanmar.android.hoebapp.bo.SearchMedia;
import de.vanmar.android.hoebapp.service.LibraryService;
import de.vanmar.android.hoebapp.service.LoginFailedException;
import de.vanmar.android.hoebapp.service.TechnicalException;
import de.vanmar.android.hoebapp.util.NetworkHelper;
import de.vanmar.android.hoebapp.util.StringUtils;

@EActivity(R.layout.search)
public class SearchActivity extends FragmentActivity {

	private static final int SEARCH_AREA_SIMPLE = 0;
	private static final int SEARCH_AREA_ADVANCED = 1;

	@ViewById(R.id.adView)
	AdView adView;

	@ViewById(R.id.searchArea)
	ViewSwitcher searchArea;

	@ViewById(R.id.simpleSearchButton)
	Button simpleSearchButton;

	@ViewById(R.id.advancedSearchButton)
	Button advancedSearchButton;

	@ViewById(R.id.searchBox)
	EditText searchBox;

	@ViewById(R.id.searchBox1)
	EditText searchBox1;

	@ViewById(R.id.searchType1)
	Spinner searchType1;

	@ViewById(R.id.searchBox2)
	EditText searchBox2;

	@ViewById(R.id.searchType2)
	Spinner searchType2;

	@ViewById(R.id.searchBox3)
	EditText searchBox3;

	@ViewById(R.id.searchType3)
	Spinner searchType3;

	@ViewById(R.id.searchResults)
	ListView searchResults;

	@Bean
	LibraryService libraryService;

	@SystemService
	InputMethodManager inputMethodManager;

	@Bean
	NetworkHelper networkHelper;

	private ArrayAdapter<SearchMedia> searchResultAdapter;

	@Override
	protected void onStart() {
		super.onStart();
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
	void setupListeners() {
		searchBox.setOnEditorActionListener(new OnEditorActionListener() {
			// enter key in search box triggers search
			@Override
			public boolean onEditorAction(final TextView v, final int actionId,
					final KeyEvent event) {
				if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
					return false;
				}
				if (actionId == EditorInfo.IME_ACTION_SEARCH || event == null
						|| event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					onSimpleSearchButtonClicked();
				}
				return true;
			}
		});
		searchBox1.setOnEditorActionListener(new OnEditorActionListener() {
			// enter key in advanced search box 1 switches to next search box
			@Override
			public boolean onEditorAction(final TextView v, final int actionId,
					final KeyEvent event) {
				if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
					return false;
				}
				if (actionId == EditorInfo.IME_ACTION_SEARCH || event == null
						|| event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					searchBox2.requestFocus();
				}
				return true;
			}
		});
		searchBox2.setOnEditorActionListener(new OnEditorActionListener() {
			// enter key in advanced search box 2 switches to next search box
			@Override
			public boolean onEditorAction(final TextView v, final int actionId,
					final KeyEvent event) {
				if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
					return false;
				}
				if (actionId == EditorInfo.IME_ACTION_SEARCH || event == null
						|| event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					searchBox3.requestFocus();
				}
				return true;
			}
		});
		searchBox3.setOnEditorActionListener(new OnEditorActionListener() {
			// enter key in advanced search box 3 triggers search
			@Override
			public boolean onEditorAction(final TextView v, final int actionId,
					final KeyEvent event) {
				if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
					return false;
				}
				if (actionId == EditorInfo.IME_ACTION_SEARCH || event == null
						|| event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					onAdvancedSearchButtonClicked();
				}
				return true;
			}
		});
	}

	@AfterViews
	void setupSearchTypes() {
		searchType1.setSelection(0);
		searchType2.setSelection(1);
		searchType3.setSelection(2);
	}

	@AfterViews
	void setupListAdapter() {
		searchResultAdapter = new ArrayAdapter<SearchMedia>(this,
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

				new AQuery(view).id(R.id.image).image(item.getImgUrl());

				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(final View v) {
						final Intent intent = new Intent(SearchActivity.this,
								DetailActivity_.class);
						intent.putExtra(DetailActivity.EXTRA_MEDIUM_ID,
								item.getId());
						startActivity(intent);
					}
				});

				return view;
			}

		};
		searchResults.setAdapter(searchResultAdapter);
	}

	@Click(R.id.simpleSearchButton)
	void onSimpleSearchButtonClicked() {
		if (networkHelper.networkAvailable()) {
			displaySearchResults(Collections.<SearchMedia> emptyList());
			setSearchButtonActive(false);
			executeSearch(searchBox.getText(), LibraryService.CATEGORY_KEYWORD,
					"", "", "", "");
			inputMethodManager.hideSoftInputFromWindow(
					searchBox.getWindowToken(), 0);
		} else {
			Toast.makeText(this, R.string.networkNotAvailable,
					Toast.LENGTH_LONG).show();
		}
	}

	@Click(R.id.advancedSearchButton)
	void onAdvancedSearchButtonClicked() {
		if (networkHelper.networkAvailable()) {
			displaySearchResults(Collections.<SearchMedia> emptyList());
			setSearchButtonActive(false);
			final String category1 = getResources().getStringArray(
					R.array.searchcategorykeys)[searchType1
					.getSelectedItemPosition()];
			final String category2 = getResources().getStringArray(
					R.array.searchcategorykeys)[searchType2
					.getSelectedItemPosition()];
			final String category3 = getResources().getStringArray(
					R.array.searchcategorykeys)[searchType3
					.getSelectedItemPosition()];
			executeSearch(searchBox1.getText().toString(), category1,
					searchBox2.getText().toString(), category2, searchBox3
							.getText().toString(), category3);
			inputMethodManager.hideSoftInputFromWindow(
					searchBox.getWindowToken(), 0);
		} else {
			Toast.makeText(this, R.string.networkNotAvailable,
					Toast.LENGTH_LONG).show();
		}
	}

	@Click(R.id.switchToAdvancedSearchButton)
	void onSwitchToAdvancedSearchButtonClicked() {
		searchArea.setDisplayedChild(SEARCH_AREA_ADVANCED);
		if (StringUtils.isEmpty(searchBox1.getText())) {
			searchBox1.setText(searchBox.getText());
		}
	}

	@Click(R.id.switchToSimpleSearchButton)
	void onSwitchToSimpleSearchButtonClicked() {
		searchArea.setDisplayedChild(SEARCH_AREA_SIMPLE);
		if (StringUtils.isEmpty(searchBox.getText())) {
			searchBox.setText(searchBox1.getText());
		}
	}

	@UiThread
	void setSearchButtonActive(final boolean active) {
		simpleSearchButton.setEnabled(active);
		simpleSearchButton.setText(active ? R.string.searchButton
				: R.string.searchButtonWaiting);
		advancedSearchButton.setEnabled(active);
		advancedSearchButton.setText(active ? R.string.searchButton
				: R.string.searchButtonWaiting);
	}

	@Background
	void executeSearch(final CharSequence text1, final CharSequence type1,
			final CharSequence text2, final CharSequence type2,
			final CharSequence text3, final CharSequence type3) {
		try {
			final List<SearchMedia> searchMedia = libraryService.searchMedia(
					this, text1.toString(), type1.toString(), text2.toString(),
					type2.toString(), text3.toString(), type3.toString());
			displaySearchResults(searchMedia);
			hideSearchArea();
		} catch (final Exception e) {
			displayError(e);
		}
		setSearchButtonActive(true);
	}

	@UiThread
	void displaySearchResults(final List<SearchMedia> searchMedia) {
		searchResultAdapter.clear();
		for (final SearchMedia item : searchMedia) {
			searchResultAdapter.add(item);
		}
	}

	@UiThread
	void hideSearchArea() {
		searchArea.setVisibility(View.GONE);
	}

	@Override
	public void onBackPressed() {
		if (searchArea.getVisibility() != View.VISIBLE) {
			searchArea.setVisibility(View.VISIBLE);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onSearchRequested() {
		searchArea.setVisibility(View.VISIBLE);
		return true;
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
