package de.vanmar.android.hoebapp;

import java.util.HashSet;
import java.util.Set;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdView;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;

import de.vanmar.android.hoebapp.db.LocationContentProvider;
import de.vanmar.android.hoebapp.db.LocationDbHelper;
import de.vanmar.android.hoebapp.util.GeoCalculationHelper;

@EActivity
@OptionsMenu(R.menu.locationsmenu)
public class LocationsActivity extends FragmentActivity implements
		LoaderCallbacks<Cursor> {

	private static final long STALE_LOCATION = 60000;

	private static final long TIME_FOR_LOCATION = 10000;
	private static final String GEO_STRING = "geo:%s,%s?q=%s %s %s";

	private static final long MAX_AGE_LOCATION = 1000 * 60 * 10;

	@SystemService
	LocationManager locationManager;

	@ViewById(R.id.adView)
	AdView adView;

	private SimpleCursorAdapter adapter;
	private ListView locationlist;

	private Location bestLocation = null;

	private final Set<Long> expandedItems = new HashSet<Long>();

	private final OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(final View view) {
			final TextView name = (TextView) view;
			final View details = ((View) name.getParent())
					.findViewById(R.id.locationDetails);
			final Long id = (Long) view.getTag();
			final boolean visible;
			if (expandedItems.contains(id)) {
				expandedItems.remove(id);
				visible = false;
			} else {
				expandedItems.add(id);
				visible = true;
			}
			setListItemAppearance(visible, name, details);
		}

	};

	private final LocationListener locationListener = new LocationListener() {

		@Override
		public void onStatusChanged(final String provider, final int status,
				final Bundle extras) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(final String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderDisabled(final String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLocationChanged(final Location location) {

			if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
				// if we get a GPS fix, we are happy and go on
				bestLocation = location;
				doneLocating();
			} else {
				if (bestLocation == null) {
					bestLocation = location;
				} else {
					final long timeDelta = location.getTime()
							- bestLocation.getTime();
					if (timeDelta > STALE_LOCATION) {
						bestLocation = location;
					}
				}
			}
		}
	};

	private ProgressDialog progressDialog;

	private boolean loading = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.locations);
		initList();
		registerLocationUpdates();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// The activity has become visible (it is now "resumed").
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Another activity is taking focus (this activity is about to be
		// "paused").

	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterLocationUpdates();
	}

	@OptionsItem(R.id.refresh)
	void doRefresh() {
		registerLocationUpdates();
	}

	private void initList() {
		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
		final String[] from = new String[] { LocationDbHelper.COLUMN_ID,
				LocationDbHelper.COLUMN_NAME, LocationDbHelper.COLUMN_ADDRESS,
				LocationDbHelper.COLUMN_OPENING_TIMES,
				LocationDbHelper.COLUMN_PHONE, LocationDbHelper.COLUMN_MAIL,
				LocationDbHelper.COLUMN_LATITUDE };
		// Fields on the UI to which we map
		final int[] to = new int[] { R.id.name, R.id.name, R.id.address,
				R.id.opening_times, R.id.phone, R.id.mail, R.id.distance };

		getSupportLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(this, R.layout.locationlist_item,
				null, from, to, 0);

		adapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(final View view, final Cursor cursor,
					final int columnIndex) {
				switch (columnIndex) {
				case LocationDbHelper.KEY_ID:
					return false;
				case LocationDbHelper.KEY_ADDRESS:
					final String address = cursor
							.getString(LocationDbHelper.KEY_ADDRESS);
					final String address2 = cursor
							.getString(LocationDbHelper.KEY_ADDRESS2);
					final String addressExtra = (address2 == null ? "" : "("
							+ address2 + ")");
					final String postalCode = cursor
							.getString(LocationDbHelper.KEY_POSTALCODE);
					final String city = cursor
							.getString(LocationDbHelper.KEY_CITY);
					final String latitude = cursor
							.getString(LocationDbHelper.KEY_LATITUDE);
					final String longitude = cursor
							.getString(LocationDbHelper.KEY_LONGITUDE);

					((TextView) view).setText(String.format(
							getString(R.string.addressFormatted), address,
							addressExtra, postalCode, city));

					((View) view.getParent()).findViewById(
							R.id.directions_button).setOnClickListener(
							new OnClickListener() {

								@Override
								public void onClick(final View v) {
									final Intent intent = new Intent(
											Intent.ACTION_VIEW, Uri
													.parse(String.format(
															GEO_STRING,
															latitude,
															longitude, address,
															postalCode, city)));
									// +
									// cursor.getString(LocationDbHelper.KEY_PHONE)));
									try {
										startActivity(intent);
									} catch (final ActivityNotFoundException e) {
										Toast.makeText(LocationsActivity.this,
												R.string.cannot_navigate,
												Toast.LENGTH_LONG).show();
									}
								}
							});

					return true;
				case LocationDbHelper.KEY_NAME:
					final long id = cursor.getLong(LocationDbHelper.KEY_ID);
					view.setTag(id);
					setListItemAppearance(expandedItems.contains(id),
							(TextView) view, ((View) view.getParent())
									.findViewById(R.id.locationDetails));
					view.setOnClickListener(onClickListener);
					return false;
				case LocationDbHelper.KEY_MAIL:
					((View) view.getParent()).findViewById(R.id.mail_button)
							.setOnClickListener(new OnClickListener() {
								final String email = cursor
										.getString(LocationDbHelper.KEY_MAIL);

								@Override
								public void onClick(final View v) {
									final Intent emailIntent = new Intent(
											android.content.Intent.ACTION_SEND);
									final String emailList[] = { email };
									emailIntent.putExtra(
											android.content.Intent.EXTRA_EMAIL,
											emailList);
									emailIntent.setType("plain/text");
									try {
										startActivity(emailIntent);
									} catch (final ActivityNotFoundException e) {
										Toast.makeText(LocationsActivity.this,
												R.string.cannot_send_mail,
												Toast.LENGTH_LONG).show();
									}
								}
							});
					return false;
				case LocationDbHelper.KEY_PHONE:
					final String phone = cursor
							.getString(LocationDbHelper.KEY_PHONE);
					((View) view.getParent()).findViewById(R.id.call_button)
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(final View v) {
									final Intent dialIntent = new Intent();
									dialIntent.setAction(Intent.ACTION_DIAL);
									dialIntent.setData(Uri
											.parse("tel:" + phone));
									try {
										startActivity(dialIntent);
									} catch (final ActivityNotFoundException e) {
										Toast.makeText(LocationsActivity.this,
												R.string.cannot_call,
												Toast.LENGTH_LONG).show();
									}
								}
							});
					return false;
				case LocationDbHelper.KEY_LATITUDE:
					final double lat = cursor
							.getDouble(LocationDbHelper.KEY_LATITUDE);
					final double lon = cursor
							.getDouble(LocationDbHelper.KEY_LONGITUDE);
					((TextView) view).setText(getDistance(lat, lon));
					return true;
				}
				return false;
			}
		});

		locationlist = (ListView) findViewById(R.id.locationlist);
		locationlist.setAdapter(adapter);
	}

	private void setListItemAppearance(final boolean visible,
			final TextView nameView, final View detailsView) {
		if (visible) {
			nameView.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.expanded, 0, 0, 0);
			detailsView.setVisibility(View.VISIBLE);
		} else {
			nameView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.expand,
					0, 0, 0);
			detailsView.setVisibility(View.GONE);
		}
	}

	@UiThread
	void displayWarning(final String warning) {
		Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		Log.w("LocationActivity", "onCreateLoader");

		final String[] projection = LocationDbHelper.ALL_COLUMNS;
		final String orderBy;
		if (bestLocation == null) {
			orderBy = LocationDbHelper.COLUMN_NAME;
		} else {
			// calculate distance with pythagoras formula
			// dist=sqrt(delta(lat)^2+delta(lon)^2*fudgeFactor)*distanceFactor
			// (fudgeFactor for differing length of longitudinal degrees)
			final double latitude = bestLocation.getLatitude();
			final double longitude = bestLocation.getLongitude();
			final double fudgeFactor = Math.pow(
					Math.cos(Math.toRadians(latitude)), 2);
			orderBy = String
					.format("((LATITUDE-(%s))*(LATITUDE-(%s))+(LONGITUDE-(%s))*(LONGITUDE-(%s))*%s)",
							latitude, latitude, longitude, longitude,
							fudgeFactor);
		}

		return new CursorLoader(this, LocationContentProvider.CONTENT_URI,
				projection, null, null, orderBy);
	}

	private String getDistance(final double lat, final double lon) {
		if (bestLocation == null) {
			return null;
		} else {
			final double distanceKm = GeoCalculationHelper
					.calculateDirectDistance(bestLocation.getLatitude(),
							bestLocation.getLongitude(), lat, lon) / 1000;
			Log.w("Distance: ", "" + distanceKm);
			return getString(R.string.locationDistance, distanceKm);
		}
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
		Log.w("LocationActivity", "onLoadFinished");
		adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		Log.w("LocationActivity", "onLoaderReset");
		// data is not available anymore, delete reference
		adapter.swapCursor(null);
	}

	private void registerLocationUpdates() {
		Log.w("LocationActivity", "registerLocationUpdates");
		progressDialog = ProgressDialog.show(this, "",
				getString(R.string.locationWaitMessage), true, true);
		progressDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				doneLocating();
			}
		});
		loading = true;

		// Register the listener with the Location Manager to receive location
		// updates
		Location lastKnownLocation = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (lastKnownLocation != null
				&& System.currentTimeMillis() - lastKnownLocation.getTime() < MAX_AGE_LOCATION) {
			bestLocation = lastKnownLocation;
		}
		int providers = 0;
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			providers++;
		}
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
			providers++;
		}

		if (providers > 0) {
			locationTimeout();
		} else {
			doneLocating();
		}
	}

	private void unregisterLocationUpdates() {
		loading = false;
		locationManager.removeUpdates(locationListener);
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	private void doneLocating() {
		Log.w("LocationActivity", "Done locating");
		if (loading) {
			unregisterLocationUpdates();
			updateLocation();
		}
	}

	@UiThread(delay = TIME_FOR_LOCATION)
	void locationTimeout() {
		Log.w("LocationActivity", "Timeout");
		doneLocating();
	}

	void updateLocation() {
		Log.w("Location", "Loc: " + bestLocation);
		getSupportLoaderManager().restartLoader(0, null, this);
	}
}
