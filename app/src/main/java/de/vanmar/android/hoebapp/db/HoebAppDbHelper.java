package de.vanmar.android.hoebapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.widget.Toast;
import de.vanmar.android.hoebapp.R;
import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.util.Preferences_;
import de.vanmar.android.hoebapp.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Locale;

public class HoebAppDbHelper extends SQLiteOpenHelper {

	final Preferences_ prefs;

	private static final String INSERT_LOCATIONS_SQL_PATH = "sql/insert_locations.sql";
	private static final String DATABASE_NAME = "hoebdata";
	private static final int DATABASE_VERSION = 14;
	public static final DateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
	private final Context context;

	public HoebAppDbHelper(final Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
		prefs = new Preferences_(context);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		try {
			db.execSQL(MediaDbHelper.MEDIA_TABLE_CREATE);
			createLocations(db);
		} catch (final Exception e) {
			Toast.makeText(context, R.string.locationLoadFailure,
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
						  final int newVersion) {
		if (oldVersion < 7) {
			db.execSQL("alter table media add column account text;");

			final SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(context);
			final String username = preferences.getString("username", null);
			final String password = preferences.getString("password", null);

			if (!StringUtils.isEmpty(username, password)) {
				final Account account = new Account(username, password);
				final ContentValues values = new ContentValues();
				values.put("account", username);
				db.update("media", values, null, null);
				prefs.accounts().put(
						Account.toString(Collections.singletonList(account)));
			}
		}
		if (oldVersion < 9) {
			db.execSQL("alter table media add column medium_id text;");
		}
		if (oldVersion < 11) {
			db.execSQL("alter table media add column type text;");
			db.execSQL("alter table media add column img_url text;");
		}
		if (oldVersion < 12) {
			db.execSQL("alter table media add column can_renew integer;");
		}

		try {
			db.execSQL(LocationDbHelper.LOCATION_TABLE_DROP);
			createLocations(db);
		} catch (final Exception e) {
			Toast.makeText(context, R.string.locationLoadFailure,
					Toast.LENGTH_SHORT).show();
		}
	}

	private void createLocations(final SQLiteDatabase db) throws IOException {
		db.execSQL(LocationDbHelper.LOCATION_TABLE_CREATE);

		final InputStream in = context.getAssets().open(
				INSERT_LOCATIONS_SQL_PATH);
		final BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = r.readLine()) != null) {
			db.execSQL(line);
		}
	}
}