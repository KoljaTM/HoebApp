package de.vanmar.android.hoebapp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import com.xtremelabs.robolectric.RobolectricTestRunner;

import de.vanmar.android.hoebapp.util.Preferences_;

@RunWith(RobolectricTestRunner.class)
public class HoebAppActivityTest {

	private HoebAppActivity_ activity;
	private Preferences_ prefs;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		activity = new HoebAppActivity_();
		// TODO: Not currently working, will retry with upgraded robolectric
		// activity.onCreate(null);
		// prefs = activity.prefs;
	}

	@Test
	public void shouldDisplayMedialist() {
		// TODO: Not currently working, will retry with upgraded robolectric
		// version
		// // when
		// activity.onResume();
		//
		// // then
		// Robolectric.shadowOf(activity.getContentResolver()).getNotifiedUris()
		// .contains(MediaContentProvider.CONTENT_URI);
	}

}
