package de.vanmar.android.hoebapp;

import android.support.v4.app.FragmentActivity;
import android.widget.ListView;

import com.google.ads.AdView;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;

import de.vanmar.android.hoebapp.service.LibraryService;
import de.vanmar.android.hoebapp.util.NetworkHelper;

@EActivity(R.layout.notepad)
public class NotepadActivity extends FragmentActivity {

	@ViewById(R.id.adView)
	AdView adView;

	@ViewById(R.id.notepadlist)
	ListView notepadList;

	@Bean
	LibraryService libraryService;

	@Bean
	NetworkHelper networkHelper;

	@Override
	protected void onStart() {
		super.onStart();
	
		loadNotepadList();
	}
	
	@Background
	void loadNotepadList(){
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

}
