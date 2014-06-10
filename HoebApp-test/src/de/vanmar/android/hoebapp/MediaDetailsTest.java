package de.vanmar.android.hoebapp;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import com.jayway.android.robotium.solo.Solo;
import de.vanmar.android.hoebapp.test.mocking.SoapMockNanoHTTPD;
import org.junit.Assert;

public class MediaDetailsTest extends
		ActivityInstrumentationTestCase2<DetailActivity_> {

	private Solo solo;

	public MediaDetailsTest() {
		super(DetailActivity_.class);
	}

	@Override
	public void setUp() throws Exception {
		SoapMockNanoHTTPD.ensureRunningAndSetup();
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	public void testDetails() throws Exception {
		final Intent intent = new Intent(getInstrumentation()
				.getTargetContext(), DetailActivity_.class);
		intent.putExtra(DetailActivity.EXTRA_MEDIUM_ID, "T010228560");
		setActivityIntent(intent);

		solo = new Solo(getInstrumentation(), getActivity());

		Assert.assertTrue(solo.waitForText("Jim Knopf und Lukas der Lokomotivführer"));
		Assert.assertTrue(solo.searchText("Barmbek"));
		Assert.assertTrue(solo.searchText("verfügbar"));
	}

	public void testDetailsNoMocks() throws Exception {
		final Intent intent = new Intent(getInstrumentation()
				.getTargetContext(), DetailActivity_.class);
		intent.putExtra(DetailActivity.EXTRA_MEDIUM_ID, "T010228560");
		setActivityIntent(intent);

		solo = new Solo(getInstrumentation(), getActivity());

		Assert.assertTrue(solo.waitForText("Jim Knopf und Lukas der Lokomotivführer"));
		Assert.assertTrue(solo.searchText("Barmbek"));
		Assert.assertTrue(solo.searchText("Exemplar"));
	}

}
