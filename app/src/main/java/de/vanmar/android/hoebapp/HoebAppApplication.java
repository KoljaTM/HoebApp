package de.vanmar.android.hoebapp;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.androidquery.callback.BitmapAjaxCallback;

import de.vanmar.android.hoebapp.service.LoginFailedException;
import de.vanmar.android.hoebapp.service.TechnicalException;
import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.UiThread;

@EApplication
public class HoebAppApplication extends Application {

	// Start updates 5 minutes after app start
	private static final long UPDATE_START_AFTER = 5 * 60 * 1000;

	// check for update every hour
	private static final long UPDATE_EVERY = 60 * 60 * 1000;

	@Override
	public void onCreate() {
		super.onCreate();

		// set the max size of the memory cache, default is 1M pixels (4MB)
		BitmapAjaxCallback.setMaxPixelLimit(2000000);

		setupAlarms();
	}

	private void setupAlarms() {
		final AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
				new Intent(this, UpdateService_.class),
				PendingIntent.FLAG_UPDATE_CURRENT);

		mgr.setRepeating(AlarmManager.RTC, System.currentTimeMillis()
				+ UPDATE_START_AFTER, UPDATE_EVERY, pendingIntent);
	}

	@UiThread
	void displayError(final Exception exception) {
		if (exception instanceof LoginFailedException) {
			Toast.makeText(this, R.string.loginfailed, Toast.LENGTH_SHORT)
					.show();
			Log.w(getClass().getCanonicalName(), "LoginFailedException");
		} else if (exception instanceof TechnicalException) {
			Toast.makeText(this, R.string.technicalError, Toast.LENGTH_SHORT)
					.show();
			Log.e(getClass().getCanonicalName(), "TechnicalException: "
					+ exception.getClass() + exception.getMessage());
		}
	}

}
