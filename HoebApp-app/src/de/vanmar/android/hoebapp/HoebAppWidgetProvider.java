package de.vanmar.android.hoebapp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.googlecode.androidannotations.annotations.EReceiver;

@EReceiver
public class HoebAppWidgetProvider extends AppWidgetProvider {
	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		context.startService(new Intent(context, WidgetUpdateService_.class));
	}
}
