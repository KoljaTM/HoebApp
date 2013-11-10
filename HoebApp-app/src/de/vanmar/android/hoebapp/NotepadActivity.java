package de.vanmar.android.hoebapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.google.ads.AdView;
import com.googlecode.androidannotations.annotations.*;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;
import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.bo.MediaDetails;
import de.vanmar.android.hoebapp.service.LibraryService;
import de.vanmar.android.hoebapp.service.LoginFailedException;
import de.vanmar.android.hoebapp.service.TechnicalException;
import de.vanmar.android.hoebapp.util.NetworkHelper;
import de.vanmar.android.hoebapp.util.Preferences_;

import java.util.List;

@EActivity(R.layout.notepad)
@OptionsMenu(R.menu.notepadmenu)
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

        loadNotepad();
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

    @OptionsItem(R.id.refresh)
    void doRefresh() {
        loadNotepad();
    }

    @AfterViews
    void setupListAdapter() {
        notepadAdapter = new ArrayAdapter<MediaDetails>(this,
                R.layout.notepad_item) {
            @Override
            public View getView(final int position, final View convertView,
                                final ViewGroup parent) {
                final View view = getLayoutInflater().inflate(
                        R.layout.notepad_item, null);
                final MediaDetails item = getItem(position);
                ((TextView) view.findViewById(R.id.author)).setText(item
                        .getAuthor());
                ((TextView) view.findViewById(R.id.title)).setText(item
                        .getTitle());
                ((TextView) view.findViewById(R.id.type)).setText(item
                        .getType());
                ((TextView) view.findViewById(R.id.signature)).setText(item
                        .getSignature());

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
                view.findViewById(R.id.removeFromNotepad).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
						notepadAdapter.remove(item);
                        removeFromNotepad(item);
                    }
                });

                return view;
            }

        };
        notepadList.setAdapter(notepadAdapter);
    }


    @Background
    void removeFromNotepad(MediaDetails mediaDetails) {
        try {
            libraryService.removeFromNotepad(mediaDetails.getOwner(), mediaDetails.getId());
        } catch (TechnicalException e) {
            displayError(e);
        }
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
        exception.printStackTrace();
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
