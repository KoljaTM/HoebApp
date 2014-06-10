package de.vanmar.android.hoebapp;

import android.test.ActivityInstrumentationTestCase2;
import com.jayway.android.robotium.solo.Solo;
import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.test.mocking.SoapMockNanoHTTPD;
import de.vanmar.android.hoebapp.test.mocking.TestUtils;

public class NotepadTest extends
		ActivityInstrumentationTestCase2<NotepadActivity_> {

	private Solo solo;

	public NotepadTest() {
		super(NotepadActivity_.class);
	}


	@Override
	public void setUp() throws Exception {
		SoapMockNanoHTTPD.ensureRunningAndSetup();
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	public void testNotepad() throws Exception {
		// given
		TestUtils.setUserdata(getInstrumentation().getTargetContext(), new Account("username", "password"));

		// when
		solo = new Solo(getInstrumentation(), getActivity());

		// then
		assertTrue(solo.searchText("Butcher, Jim"));
		assertTrue(solo.searchText("•<Die>• dunklen Fälle des Harry Dresden"));
		assertTrue(solo.searchText("Buch Erwachsene"));
		assertTrue(solo.searchText("Workshop Aquarell"));
	}

}
