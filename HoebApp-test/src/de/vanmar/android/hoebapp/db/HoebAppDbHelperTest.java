package de.vanmar.android.hoebapp.db;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.test.InstrumentationTestCase;
import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.test.mocking.TestUtils;

public class HoebAppDbHelperTest extends InstrumentationTestCase {

	Context context;
	Context targetContext;

	HoebAppDbHelper dbHelper = null;
	Cursor cursor = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		context = getInstrumentation().getContext();
		targetContext = getInstrumentation().getTargetContext();
	}

	@Override
	protected void tearDown() throws Exception {
		if (cursor != null) {
			cursor.close();
		}
		if (dbHelper != null) {
			dbHelper.close();
		}
	}

	public void testCreateDB() throws SQLException {
		TestUtils.prepareTestDatabase(targetContext);

		dbHelper = new HoebAppDbHelper(targetContext);
		cursor = dbHelper.getReadableDatabase().query(
				MediaDbHelper.MEDIA_TABLE_NAME, MediaDbHelper.ALL_COLUMNS,
				null, null, null, null, null);
		assertTrue("Should be initially empty", cursor.isAfterLast());
		assertTrue("Should be initially empty", cursor.getCount() == 0);
	}

	public void testUpgradeDBFromVer6() throws SQLException {
		TestUtils.prepareTestDatabase(context, targetContext,
				"hoebdata.ver6.db");

		setUsernamePasswordPrefs("user", "pw123");

		dbHelper = new HoebAppDbHelper(targetContext);
		cursor = dbHelper.getReadableDatabase().query(
				MediaDbHelper.MEDIA_TABLE_NAME, MediaDbHelper.ALL_COLUMNS,
				null, null, null, null, null);
		assertTrue("Should contain 8 rows", cursor.getCount() == 8);
		while (cursor.moveToNext()) {
			assertTrue("Account should be set",
					"user".equals(cursor.getString(MediaDbHelper.KEY_ACCOUNT)));
		}

		final List<Account> accountDataFromPrefs = getAccountDataFromPrefs();
		assertTrue(accountDataFromPrefs.size() == 1);
		assertTrue("user".equals(accountDataFromPrefs.get(0).getUsername()));
		assertTrue("pw123".equals(accountDataFromPrefs.get(0).getPassword()));
	}

	private void setUsernamePasswordPrefs(final String username,
			final String password) {
		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(targetContext);
		final Editor editor = preferences.edit();
		editor.putString("username", username);
		editor.putString("password", password);
		editor.commit();
	}

	private List<Account> getAccountDataFromPrefs() {
		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(targetContext);
		return Account.fromString(preferences.getString("accounts", null));
	}
}
