package de.vanmar.android.hoebapp.db;

public class LocationDbHelper {

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_SIGN = "sign";
	public static final String COLUMN_ADDRESS = "address";
	public static final String COLUMN_ADDRESS2 = "address2";
	public static final String COLUMN_POSTALCODE = "postalcode";
	public static final String COLUMN_CITY = "city";
	public static final String COLUMN_PHONE = "phone";
	public static final String COLUMN_FAX = "fax";
	public static final String COLUMN_MAIL = "mail";
	public static final String COLUMN_OPENING_TIMES = "opening_times";
	public static final String COLUMN_LATITUDE = "latitude";
	public static final String COLUMN_LONGITUDE = "longitude";

	public static final int KEY_ID = 0;
	public static final int KEY_NAME = 1;
	public static final int KEY_SIGN = 2;
	public static final int KEY_ADDRESS = 3;
	public static final int KEY_ADDRESS2 = 4;
	public static final int KEY_POSTALCODE = 5;
	public static final int KEY_CITY = 6;
	public static final int KEY_PHONE = 7;
	public static final int KEY_FAX = 8;
	public static final int KEY_MAIL = 9;
	public static final int KEY_OPENING_TIMES = 10;
	public static final int KEY_LATITUDE = 11;
	public static final int KEY_LONGITUDE = 12;

	public static String[] ALL_COLUMNS = { COLUMN_ID, COLUMN_NAME, COLUMN_SIGN,
			COLUMN_ADDRESS, COLUMN_ADDRESS2, COLUMN_POSTALCODE, COLUMN_CITY,
			COLUMN_PHONE, COLUMN_FAX, COLUMN_MAIL, COLUMN_OPENING_TIMES,
			COLUMN_LATITUDE, COLUMN_LONGITUDE };

	public static final String LOCATION_TABLE_NAME = "location";
	static final String LOCATION_TABLE_CREATE = String.format(
			//
			"CREATE TABLE %s (" + //
					"%s integer primary key," + //
					"%s text," + //
					"%s text," + //
					"%s text," + //
					"%s text," + //
					"%s text," + //
					"%s text," + //
					"%s text," + //
					"%s text," + //
					"%s text," + //
					"%s text," + //
					"%s real," + //
					"%s real);", //
			LOCATION_TABLE_NAME, COLUMN_ID, COLUMN_NAME, COLUMN_SIGN,
			COLUMN_ADDRESS, COLUMN_ADDRESS2, COLUMN_POSTALCODE, COLUMN_CITY,
			COLUMN_PHONE, COLUMN_FAX, COLUMN_MAIL, COLUMN_OPENING_TIMES,
			COLUMN_LATITUDE, COLUMN_LONGITUDE);

	static final String LOCATION_TABLE_DROP = "DROP TABLE IF EXISTS "
			+ LOCATION_TABLE_NAME;

}