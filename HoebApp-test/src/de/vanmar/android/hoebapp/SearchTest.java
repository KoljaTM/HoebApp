package de.vanmar.android.hoebapp;

import android.test.ActivityInstrumentationTestCase2;
import com.jayway.android.robotium.solo.Solo;
import de.vanmar.android.hoebapp.test.mocking.SoapMockNanoHTTPD;

public class SearchTest extends
		ActivityInstrumentationTestCase2<SearchActivity_> {

	private Solo solo;

	public SearchTest() {
		super(SearchActivity_.class);
	}

	@Override
	public void setUp() throws Exception {
		SoapMockNanoHTTPD.ensureRunningAndSetup();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	public void testSimpleSearch() throws Exception {
		assertTrue(solo.searchText("Katalogsuche"));
		solo.enterText(0, "Android");
		solo.clickOnButton("Suchen");

		assertTrue(solo.waitForText("Android User  2014/2 alles zum Thema Android"));

		assertTrue(solo.searchText("Rehberg, Andreas Itzchak"));
		assertTrue(solo.searchText("Buch Erwachsene"));
		assertTrue(solo.searchText("Zeitschrift"));
	}

}
