package de.vanmar.android.hoebapp;

import android.test.ActivityInstrumentationTestCase2;
import com.jayway.android.robotium.solo.Solo;
import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.test.mocking.SoapMockNanoHTTPD;
import de.vanmar.android.hoebapp.test.mocking.TestUtils;
import de.vanmar.android.hoebapp.util.NetworkHelper;
import org.junit.Assert;

public class GetMediaTest extends
		ActivityInstrumentationTestCase2<HoebAppActivity_> {

	private Solo solo;
	private SoapMockNanoHTTPD httpMock;

	public GetMediaTest() {
		super(HoebAppActivity_.class);
	}

	@Override
	public void setUp() throws Exception {
		TestUtils.initEmpty(getInstrumentation().getTargetContext());

		httpMock = SoapMockNanoHTTPD.ensureRunningAndSetup();
	}

	private Solo initActivityAndGetSolo() {
		return new Solo(getInstrumentation(), getActivity());
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	public void testOpenPreferencePageIfNoUserdataSet() {
		// given
		TestUtils.setUserdata(getInstrumentation().getTargetContext());
		solo = initActivityAndGetSolo();

		// when
		solo.clickOnActionBarItem(R.id.refresh);

		// then
		Assert.assertTrue(solo
				.searchText("Bitte Kundennummer und Geheimnummer konfigurieren"));
		solo.assertCurrentActivity("should call preferences page",
				PreferencesActivity_.class);
	}

	public void testDisplayLoginError() {
		// given
		TestUtils.setUserdata(getInstrumentation().getTargetContext(),
				new Account("error", "password"));
		solo = initActivityAndGetSolo();

		// when
		solo.clickOnActionBarItem(R.id.refresh);

		// then
		Assert.assertTrue(solo.waitForText("Login fehlgeschlagen", 1, 20000));
		solo.assertCurrentActivity("should stay on main page",
				HoebAppActivity_.class);
	}

	public void testGetMediaList() throws InterruptedException {
		// given
		TestUtils.setUserdata(getInstrumentation().getTargetContext(),
				new Account("username", "yyy"));
		solo = initActivityAndGetSolo();

		// before
		Assert.assertTrue(solo.searchText("HÖB-Fans"));
		Assert.assertTrue(solo.searchText("0 Titel entliehen"));

		// when
		solo.clickOnActionBarItem(R.id.refresh);

		// then
		Assert.assertTrue(solo.waitForText("11 Titel entliehen"));
		Assert.assertTrue(solo.searchText("•<Die>• Wunschinsel ...", 1, true));
		Assert.assertTrue(solo.searchText("Willy Werkels Schiffe-Buch", 1, true));
		Assert.assertTrue(solo.searchText("24.06.2014", 1, true));

		solo.assertCurrentActivity("should stay on main page",
				HoebAppActivity_.class);
	}

	public void testRememberRenewCheckbox() throws InterruptedException {
		// given
		TestUtils.setUserdata(getInstrumentation().getTargetContext(),
				new Account("username", "yyy"));
		TestUtils.prepareTestDatabase(getInstrumentation().getContext(),
				getInstrumentation().getTargetContext(), "hoebdata.ver12.db");
		solo = initActivityAndGetSolo();

		// before
		Assert.assertTrue(solo.waitForText("11 Titel entliehen"));
		final int firstRenewableItem = 2;
		solo.clickOnCheckBox(firstRenewableItem);

		// when
		solo.scrollToBottom();
		solo.waitForText("Sturmnacht");
		solo.scrollToTop();
		solo.waitForText("Geschichte Europas");

		// then
		assertTrue(solo.isCheckBoxChecked(firstRenewableItem));
	}

	public void testGotoDetailActivity() throws InterruptedException {
		// given
		TestUtils.setUserdata(getInstrumentation().getTargetContext(),
				new Account("username", "yyy"));
		TestUtils.prepareTestDatabase(getInstrumentation().getContext(),
				getInstrumentation().getTargetContext(), "hoebdata.ver9.db");

		solo = initActivityAndGetSolo();

		// before
		Assert.assertTrue(solo.waitForText("18 Titel entliehen"));

		// when
		solo.clickOnText("Jim Knopf");

		// then
		Assert.assertTrue(solo.waitForText("Ende, Michael"));
		Assert.assertTrue(solo.waitForText("b ENDE"));
		Assert.assertTrue(solo.waitForText("4 Exemplare verfügbar"));
		solo.assertCurrentActivity("should go to details page",
				DetailActivity_.class);
	}

	public void testDontGotoDetailActivityAfterDbUpgrade()
			throws InterruptedException {
		// given
		TestUtils.setUserdata(getInstrumentation().getTargetContext(),
				new Account("username", "yyy"));
		TestUtils.prepareTestDatabase(getInstrumentation().getContext(),
				getInstrumentation().getTargetContext(), "hoebdata.ver8.db");
		solo = initActivityAndGetSolo();

		// before
		Assert.assertTrue(solo.waitForText("18 Titel entliehen"));

		// when
		solo.clickOnText("Jim Knopf");

		// then
		solo.assertCurrentActivity("should not go to details page",
				HoebAppActivity_.class);
	}

	public void testInternetNotAvailable() {
		// given
		getActivity().networkHelper = new NetworkHelper() {
			@Override
			public boolean networkAvailable() {
				return false;
			}
		};
		solo = initActivityAndGetSolo();

		// when
		solo.clickOnActionBarItem(R.id.refresh);

		// then
		Assert.assertTrue(solo.searchText("Internet nicht verfügbar"));
	}

	public void testIOException() {
		// given
		SoapMockNanoHTTPD.stopServer();
		TestUtils.setUserdata(getInstrumentation().getTargetContext(),
				new Account("username", "yyy"));
		solo = initActivityAndGetSolo();

		// when
		solo.clickOnActionBarItem(R.id.refresh);

		// then
		Assert.assertTrue(solo
				.searchText("Bücherhallen-Server nicht erreichbar"));
	}
}
