package de.vanmar.android.hoebapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import de.vanmar.android.hoebapp.db.MediaContentProvider;
import de.vanmar.android.hoebapp.db.MediaDbHelper;
import de.vanmar.android.hoebapp.service.SoapLibraryService;
import de.vanmar.android.hoebapp.util.NetworkHelper;
import de.vanmar.android.hoebapp.util.Preferences_;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Date;

@EReceiver
public class UpdateService extends BroadcastReceiver {

	private static final int NOTIFICATION_ID = 1;

	private static final int DAY_IN_MILLIS = 86400000;

	@Pref
	Preferences_ prefs;

	@Bean
	SoapLibraryService soapLibraryService;

	@Bean
	NetworkHelper networkHelper;

	NotificationManagerCompat notificationManager;

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final Boolean doAutoUpdate = prefs.doAutoUpdate().get();
		final Boolean doAutoUpdateWifiOnly = prefs.doAutoUpdateWifiOnly().get();
		// refresh media list if necessary and autoupdate activated
		if (doAutoUpdate
				&& (doAutoUpdateWifiOnly ? networkHelper.wifiAvailable()
				: networkHelper.networkAvailable())) {
			refreshListIfNecessary(context);
		}

		fireNotificationIfNecessary(context);
	}

	@Background
	void refreshListIfNecessary(final Context context) {
		final long lastAccess = prefs.lastAccess().get();
		final long now = new Date().getTime();
		if (now - lastAccess > DAY_IN_MILLIS) {
			try {
				Log.e("UpdateService", "Updating");
				soapLibraryService.refreshMediaList(context);
			} catch (final Exception e) {
				// Refresh failed; no point in bothering the user with it
				Log.e("UpdateService", "Update failed", e);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void fireNotificationIfNecessary(final Context context) {
		final long dueDate = (System.currentTimeMillis() / DAY_IN_MILLIS)
				* DAY_IN_MILLIS;

		final int mediaCount = mediaDueCount(context, dueDate);

		// only send notification once for each dueDate
		final Long notificationSent = prefs.notificationSent().get();

		notificationManager = NotificationManagerCompat.from(context);

		if (mediaCount > 0 && notificationSent < dueDate) {
			// Create an Intent to launch HoebAppActivity
			final Intent intent = new Intent(context, HoebAppActivity_.class);
			final PendingIntent pendingIntent = PendingIntent.getActivity(
					context, 0, intent, 0);
			final CharSequence tickerText = context
					.getText(R.string.notificationTicker);
			final CharSequence title = context
					.getText(R.string.notificationTitle);
			final CharSequence text = mediaCount == 1 ? context
					.getText(R.string.notificationTextSingle) : String.format(
					context.getText(R.string.notificationText).toString(),
					mediaCount);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
			builder.setSmallIcon(R.drawable.logo_hh);
			builder.setTicker(tickerText);
			builder.setWhen(System.currentTimeMillis());
			builder.setContentTitle(title);
			builder.setContentIntent(pendingIntent);
			final Notification notification = builder.getNotification();

			notificationManager.notify(NOTIFICATION_ID, notification);

			prefs.notificationSent().put(dueDate);
		} else if (mediaCount <= 0) {
			notificationManager.cancel(NOTIFICATION_ID);
		}
	}

	private int mediaDueCount(final Context context, final long dueDate) {
		final Cursor mediaDueCursor = context.getContentResolver().query(
				MediaContentProvider.CONTENT_URI, new String[]{},
				MediaDbHelper.COLUMN_DUEDATE + "<=?",
				new String[]{String.valueOf(dueDate)}, null);
		final int mediaCount = mediaDueCursor.getCount();
		mediaDueCursor.close();
		return mediaCount;
	}

}