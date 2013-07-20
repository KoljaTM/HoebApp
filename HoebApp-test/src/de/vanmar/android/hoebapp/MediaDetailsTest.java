package de.vanmar.android.hoebapp;

import org.junit.Assert;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import de.vanmar.android.hoebapp.test.mocking.MockResponses;
import de.vanmar.android.hoebapp.test.mocking.TestUtils;

public class MediaDetailsTest extends
		ActivityInstrumentationTestCase2<DetailActivity_> {

	private Solo solo;

	public MediaDetailsTest() {
		super(DetailActivity_.class);
	}

	@Override
	public void setUp() throws Exception {

	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	public void testDetails() throws Exception {
		TestUtils.initMocks(getInstrumentation().getContext());
		MockResponses.reset();
		MockResponses
				.forRequestDoAnswer(
						"https://www.buecherhallen.de/alswww2.dll/APS_PRESENT_BIB\\?Style=Portal3&SubStyle=&Lang=GER&ResponseEncoding=utf-8&no=T010228572",
						"detailJimKnopf.html");

		final Intent intent = new Intent(getInstrumentation()
				.getTargetContext(), DetailActivity_.class);
		intent.putExtra(DetailActivity.EXTRA_MEDIUM_ID, "T010228572");
		setActivityIntent(intent);

		solo = new Solo(getInstrumentation(), getActivity());

		Assert.assertTrue(solo.waitForText("Jim Knopf und die wilde 13"));
		Assert.assertTrue(solo.searchText("Barmbek"));
		Assert.assertTrue(solo.searchText("verfügbar"));
	}

	public void testDetailsNoMocks() throws Exception {
		TestUtils.noMocks();

		final Intent intent = new Intent(getInstrumentation()
				.getTargetContext(), DetailActivity_.class);
		intent.putExtra(DetailActivity.EXTRA_MEDIUM_ID, "T010228572");
		setActivityIntent(intent);

		solo = new Solo(getInstrumentation(), getActivity());

		Assert.assertTrue(solo.waitForText("Jim Knopf und die wilde 13"));
		Assert.assertTrue(solo.searchText("Barmbek"));
		Assert.assertTrue(solo.searchText("Exemplar"));
	}

}
