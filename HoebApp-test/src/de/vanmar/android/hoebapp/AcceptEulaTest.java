package de.vanmar.android.hoebapp;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.vanmar.android.hoebapp.util.Preferences_;

public class AcceptEulaTest extends
		ActivityInstrumentationTestCase2<HoebAppActivity_> {

	private Solo solo;

	private Preferences_ prefs;

	public AcceptEulaTest() {
		super(HoebAppActivity_.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		prefs = new Preferences_(getInstrumentation().getTargetContext());
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	public void testDisplayAndAcceptEula() throws Exception {
		setAcceptedEula(0);

		solo = new Solo(getInstrumentation(), getActivity());
		assertTrue(solo.searchText("Nutzungsbedingungen"));
		solo.clickOnButton("Ja, ich kenne das Risiko");
		assertTrue(solo.searchText("HÖB-Fans"));
		solo.assertCurrentActivity("should call app info page",
				HoebAppActivity_.class);
		assertTrue(solo.getCurrentActivity().hasWindowFocus());
		assertEquals(HoebAppActivity.EULA_VERSION, getAcceptedEula());
	}

	public void testDisplayAndDeclineEula() throws Exception {
		setAcceptedEula(0);

		solo = new Solo(getInstrumentation(), getActivity());
		assertTrue(solo.searchText("Nutzungsbedingungen"));
		solo.clickOnButton("Nein, Danke.");
		assertFalse(solo.getCurrentActivity().hasWindowFocus());
		assertEquals(0, getAcceptedEula());
	}

	private void setAcceptedEula(final int version) {
		prefs.acceptedEULA().put(version);
	}

	private int getAcceptedEula() {
		return prefs.acceptedEULA().get();
	}

	public void testDontDisplayDialogIfAlreadyAccepted() {
		// given
		setAcceptedEula(HoebAppActivity.EULA_VERSION);
		solo = new Solo(getInstrumentation(), getActivity());

		// then
		assertFalse(solo.searchText("Nutzungsbedingungen"));
		assertTrue(solo.searchText("HÖB-Fans"));
		assertEquals(HoebAppActivity.EULA_VERSION, getAcceptedEula());
	}
}
