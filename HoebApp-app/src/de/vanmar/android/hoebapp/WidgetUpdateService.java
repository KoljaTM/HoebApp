package de.vanmar.android.hoebapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EService;

import de.vanmar.android.hoebapp.db.MediaContentProvider;

@SuppressLint("Registered")
@EService
public class WidgetUpdateService extends Service {

	private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat(
			"dd.MM.", Locale.GERMAN);

	@Override
	public void onStart(final Intent intent, final int startId) {
		performWidgetUpdate();
	}

	@Background
	void performWidgetUpdate() {
		// Build the widget update
		final RemoteViews updateViews = buildUpdate(this);

		// Push update for this widget to the home screen
		final ComponentName thisWidget = new ComponentName(this,
				HoebAppWidgetProvider_.class);
		final AppWidgetManager manager = AppWidgetManager.getInstance(this);
		manager.updateAppWidget(thisWidget, updateViews);
	}

	@Override
	public IBinder onBind(final Intent intent) {
		// We don't need to bind to this service
		return null;
	}

	RemoteViews buildUpdate(final Context context) {
		// query the aggregates
		final Uri aggregateUri = Uri.parse(MediaContentProvider.CONTENT_URI
				+ MediaContentProvider.MEDIA_AGGREGATE);
		final ContentProviderClient contentProvider = context
				.getContentResolver().acquireContentProviderClient(
						MediaContentProvider.CONTENT_URI);
		String count = "";
		String duedate = "";
		Cursor cursor = null;
		boolean duedateReached = false;
		try {
			cursor = contentProvider
					.query(aggregateUri, null, null, null, null);
			if (cursor.moveToNext()) {
				count = cursor
						.getString(MediaContentProvider.COLUMN_INDEX_AGGREGATE_COUNT);
				final long dueDateLong = cursor
						.getLong(MediaContentProvider.COLUMN_INDEX_AGGREGATE_MIN_DUEDATE);
				if (dueDateLong == 0) {
					duedate = "";
				} else {
					final Date duedateDate = new Date(dueDateLong);
					duedate = DISPLAY_DATE_FORMAT.format(duedateDate);
					if (duedateDate.before(new Date())) {
						duedateReached = true;
					}
				}
			}
		} catch (final Exception e) {
			Log.e(HoebAppWidgetProvider.class.getCanonicalName(),
					"Error occurred while querying Media Aggregates", e);
			count = "#";
			duedate = "#";
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		// Get the layout for the App Widget and attach an on-click listener
		// to the button; use different layout if duedate is reached
		final RemoteViews views = new RemoteViews(context.getPackageName(),
				duedateReached ? R.layout.widget_late : R.layout.widget);

		// Create an Intent to launch HoebAppActivity
		final Intent intent = new Intent(context, HoebAppActivity_.class);
		final PendingIntent pendingIntent = PendingIntent.getActivity(context,
				0, intent, 0);
		views.setOnClickPendingIntent(R.id.widget, pendingIntent);
		views.setTextViewText(R.id.count, count);
		views.setTextViewText(R.id.duedate, duedate);
		return views;
	}
}