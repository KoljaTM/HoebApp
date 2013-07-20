package de.vanmar.android.hoebapp;

import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.vanmar.android.hoebapp.test.mocking.MockResponses;
import de.vanmar.android.hoebapp.test.mocking.TestUtils;

public class SearchTest extends
		ActivityInstrumentationTestCase2<SearchActivity_> {

	private Solo solo;

	public SearchTest() {
		super(SearchActivity_.class);
	}

	@Override
	public void setUp() throws Exception {
		TestUtils.initMocks(getInstrumentation().getContext());

		solo = new Solo(getInstrumentation(), getActivity());
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	public void testSimpleSearch() throws Exception {
		MockResponses.reset();
		MockResponses
				.forRequestDoAnswer(
						"https://www.buecherhallen.de/alswww2.dll/\\?Style=Portal3&SubStyle=&Lang=GER&ResponseEncoding=utf-8&Method=QueryWithLimits&SearchType=AdvancedSearch&TargetSearchType=AdvancedSearch&DB=SearchServer&q.PageSize=20&q.form.t1.term=kw%3D&q.form.t1.expr=Android&q.form.t2.logic=\\+and\\+&q.form.t2.term=&q.form.t2.expr=&q.form.t3.logic=\\+and\\+&q.form.t3.term=&q.form.t3.expr=",
						"searchResultAndroid.html");

		assertTrue(solo.searchText("Katalogsuche"));
		solo.enterText(0, "Android");
		solo.clickOnButton("Suchen");

		assertTrue(solo.waitForText("Der Android-Tablet-PC"));

		assertTrue(solo.searchText("Post, Uwe"));
		assertTrue(solo.searchText("Android-Apps entwickeln"));
		assertTrue(solo.searchText("eBook"));
		assertTrue(solo.searchText("2012"));
	}

}
