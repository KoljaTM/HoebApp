package de.vanmar.android.hoebapp;

import java.io.IOException;

import org.junit.Assert;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.CheckBox;

import com.jayway.android.robotium.solo.Solo;

import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.test.mocking.MockResponses;
import de.vanmar.android.hoebapp.test.mocking.TestUtils;
import de.vanmar.android.hoebapp.util.NetworkHelper;

public class GetMediaTest extends
		ActivityInstrumentationTestCase2<HoebAppActivity_> {

	private Solo solo;

	public GetMediaTest() {
		super(HoebAppActivity_.class);
	}

	@Override
	public void setUp() throws Exception {
		TestUtils.initEmpty(getInstrumentation().getTargetContext());
		TestUtils.initMocks(getInstrumentation().getContext());
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
				new Account("xxx", "yyy"));
		MockResponses.reset();
		MockResponses.forRequestDoAnswer(".*fn=Login.*", "loginform.html");
		MockResponses.forRequestDoAnswer(".*alswww2.dll/Obj_567281354477961.*",
				"loginfailed.html");
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
				new Account("xxx", "yyy"));
		MockResponses.reset();
		MockResponses.forRequestDoAnswer(".*fn=Login.*", "loginform.html");
		MockResponses.forRequestDoAnswer(".*alswww2.dll/Obj_567281354477961.*",
				"loginsuccess.html");
		MockResponses.forRequestDoAnswer(".*fn=MyLoans.*", "medialist.html");
		MockResponses.forRequestDoAnswer(".*Method=PageDown.*",
				"medialistbottom.html");
		solo = initActivityAndGetSolo();

		// before
		Assert.assertTrue(solo.searchText("HÖB-Fans"));
		Assert.assertTrue(solo.searchText("0 Titel entliehen"));
		Assert.assertEquals(0, solo.getView(R.id.adView).getHeight());

		// when
		solo.clickOnActionBarItem(R.id.refresh);

		// then
		Assert.assertTrue(solo.waitForText("18 Titel entliehen"));
		Assert.assertTrue(solo.searchText("Drei Männer im Schnee", 1, true));
		Assert.assertTrue(solo.searchText("Petzi und Paffhans", 1, true));

		solo.assertCurrentActivity("should stay on main page",
				HoebAppActivity_.class);
	}

	public void testRememberRenewCheckbox() throws InterruptedException {
		// given
		TestUtils.setUserdata(getInstrumentation().getTargetContext(),
				new Account("xxx", "yyy"));
		TestUtils.prepareTestDatabase(getInstrumentation().getContext(),
				getInstrumentation().getTargetContext(), "hoebdata.ver9.db");
		solo = initActivityAndGetSolo();

		// before
		Assert.assertTrue(solo.waitForText("18 Titel entliehen"));
		final int firstRenewableItem = 2;
		CheckBox checkBox = solo.getCurrentCheckBoxes().get(firstRenewableItem);
		solo.clickOnView(checkBox, true);

		// when
		solo.scrollToBottom();
		solo.waitForText("Jim Knopf");
		solo.scrollToTop();
		solo.waitForText("Drei Männer im Schnee");

		// then
		checkBox = solo.getCurrentCheckBoxes().get(firstRenewableItem);
		assertTrue(checkBox.isChecked());
	}

	public void testGotoDetailActivity() throws InterruptedException {
		// given
		TestUtils.setUserdata(getInstrumentation().getTargetContext(),
				new Account("xxx", "yyy"));
		TestUtils.prepareTestDatabase(getInstrumentation().getContext(),
				getInstrumentation().getTargetContext(), "hoebdata.ver9.db");
		MockResponses.reset();
		MockResponses
				.forRequestDoAnswer(
						"https://www.buecherhallen.de/alswww2.dll/APS_PRESENT_BIB\\?Style=Portal3&SubStyle=&Lang=GER&ResponseEncoding=utf-8&no=T010228560",
						"detailJimKnopfLukas.html");

		solo = initActivityAndGetSolo();

		// before
		Assert.assertTrue(solo.waitForText("18 Titel entliehen"));

		// when
		solo.clickOnText("Jim Knopf");

		// then
		Assert.assertTrue(solo.waitForText("Die beiden Freunde erleben"));
		solo.assertCurrentActivity("should go to details page",
				DetailActivity_.class);
	}

	public void testDontGotoDetailActivityAfterDbUpgrade()
			throws InterruptedException {
		// given
		TestUtils.setUserdata(getInstrumentation().getTargetContext(),
				new Account("xxx", "yyy"));
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
		TestUtils.setUserdata(getInstrumentation().getTargetContext(),
				new Account("xxx", "yyy"));
		MockResponses.reset();
		MockResponses.forRequestDoAnswer(".*fn=Login.*", new IOException(
				"Test Exception"));
		solo = initActivityAndGetSolo();

		// when
		solo.clickOnActionBarItem(R.id.refresh);

		// then
		Assert.assertTrue(solo
				.searchText("Bücherhallen-Server nicht erreichbar"));
	}
}
