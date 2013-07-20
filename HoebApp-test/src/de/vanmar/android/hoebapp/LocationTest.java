package de.vanmar.android.hoebapp;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.jayway.android.robotium.solo.Solo;

import de.vanmar.android.hoebapp.test.mocking.TestUtils;

public class LocationTest extends
		ActivityInstrumentationTestCase2<LocationsActivity_> {

	private Solo solo;

	public LocationTest() {
		super(LocationsActivity_.class);
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

	public void testDisplayLocationAlphabeticallyIfNoPositionFound() {
		// given
		assertTrue(solo.searchText("Bitte warten, Position wird abgerufen"));

		// when
		solo.waitForDialogToClose(10000);

		// then
		assertFalse(solo.searchText("Bitte warten, Position wird abgerufen"));
		solo.scrollToTop();
		final ListView locationList = solo.getCurrentListViews().get(0);

		final ListAdapter adapter = locationList.getAdapter();
		final String lastEntry = null;
		for (int i = 0; i < adapter.getCount(); i++) {
			final String currentEntry = ((Cursor) adapter.getItem(i))
					.getString(1);
			if (lastEntry != null) {
				assertTrue(String.format("%s should be greater than %s",
						currentEntry, lastEntry),
						currentEntry.compareTo(lastEntry) >= 0);
			}
		}
	}

	public void testDisplayLocationOrderedByDistanceIfPositionFound() {
		if (PackageManager.PERMISSION_DENIED == getActivity()
				.getPackageManager().checkPermission(
						permission.ACCESS_MOCK_LOCATION,
						"de.vanmar.android.hoebapp")) {
			Log.w("No Permission",
					"Permission ACCESS_MOCK_LOCATION missing, cannot run test");
			return;
		}

		// given
		assertTrue(solo.searchText("Bitte warten, Position wird abgerufen"));

		// when
		final LocationManager locationManager = (LocationManager) getInstrumentation()
				.getTargetContext().getSystemService(Context.LOCATION_SERVICE);

		locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, // requiresNetwork,
				false, // requiresSatellite,
				false, // requiresCell,
				false, // hasMonetaryCost,
				false, // supportsAltitude,
				false, // supportsSpeed,
				false, // supportsBearing,
				android.location.Criteria.POWER_MEDIUM, // powerRequirement
				android.location.Criteria.ACCURACY_FINE); // accuracy
		final Location location = new Location(LocationManager.GPS_PROVIDER);
		location.setLatitude(37.422006);
		location.setLongitude(-122.084095);
		location.setTime(System.currentTimeMillis());
		location.setAccuracy(25);
		locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER,
				true);
		locationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER,
				LocationProvider.AVAILABLE, null, System.currentTimeMillis());
		locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER,
				location);

		solo.waitForDialogToClose(10000);

		// then
		assertFalse(solo.searchText("Bitte warten, Position wird abgerufen"));
		solo.scrollToTop();
		final ListView locationList = solo.getCurrentListViews().get(0);

		final ListAdapter adapter = locationList.getAdapter();
		final String lastEntry = null;
		for (int i = 0; i < adapter.getCount(); i++) {
			final String currentEntry = ((Cursor) adapter.getItem(i))
					.getString(1);
			if (lastEntry != null) {
				assertTrue(String.format("%s should be greater than %s",
						currentEntry, lastEntry),
						currentEntry.compareTo(lastEntry) >= 0);
			}
		}
	}
}
