package de.vanmar.android.hoebapp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import org.androidannotations.annotations.EReceiver;


@EReceiver
public class HoebAppWidgetProvider extends AppWidgetProvider {
	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			context.startForegroundService(new Intent(context, WidgetUpdateService_.class));
		} else {
			context.startService(new Intent(context, WidgetUpdateService_.class));
		}
	}
}
