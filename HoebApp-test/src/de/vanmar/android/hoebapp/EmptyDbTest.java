package de.vanmar.android.hoebapp;

import org.junit.Assert;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.vanmar.android.hoebapp.test.mocking.TestUtils;

public class EmptyDbTest extends
		ActivityInstrumentationTestCase2<HoebAppActivity_> {

	private Solo solo;

	public EmptyDbTest() {
		super(HoebAppActivity_.class);
	}

	@Override
	public void setUp() throws Exception {
		TestUtils.initEmpty(getInstrumentation().getTargetContext());

		solo = new Solo(getInstrumentation(), getActivity());
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	public void testInitialDisplay() throws Exception {
		Assert.assertTrue(solo.searchText("HÖB-Fans"));
		Assert.assertTrue(solo.searchText("0 Titel entliehen"));
	}

	public void testShowAppInfo() {
		// when
		solo.clickOnActionBarItem(R.id.about);

		// then
		assertNotNull(solo.getView(R.id.abouttext));
		solo.assertCurrentActivity("should call app info page",
				AboutActivity_.class);
	}

	public void testShowHelpPage() {
		// when
		solo.clickOnActionBarItem(R.id.help);

		// then
		assertNotNull(solo.getView(R.id.helptext));
		solo.assertCurrentActivity("should call help page", HelpActivity_.class);
	}
}
