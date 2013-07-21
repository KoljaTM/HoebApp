package de.vanmar.android.hoebapp.db;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class MediaDbHelper {

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_AUTHOR = "author";
	public static final String COLUMN_DUEDATE = "duedate";
	public static final String COLUMN_LOANDATE = "loandate";
	public static final String COLUMN_SIGNATURE = "signature";
	public static final String COLUMN_RENEW_LINK = "renew_link";
	public static final String COLUMN_NO_RENEW_REASON = "no_renew_reason";
	public static final String COLUMN_NUM_RENEWS = "num_renews";
	public static final String COLUMN_ACCOUNT = "account";
	public static final String COLUMN_MEDIUM_ID = "medium_id";

	public static final int KEY_ID = 0;
	public static final int KEY_TITLE = 1;
	public static final int KEY_AUTHOR = 2;
	public static final int KEY_DUEDATE = 3;
	public static final int KEY_LOANDATE = 4;
	public static final int KEY_SIGNATURE = 5;
	public static final int KEY_RENEW_LINK = 6;
	public static final int KEY_NO_RENEW_REASON = 7;
	public static final int KEY_NUM_RENEWS = 8;
	public static final int KEY_ACCOUNT = 9;
	public static final int KEY_MEDIUM_ID = 10;

	public static String[] ALL_COLUMNS = new String[] { COLUMN_ID,
			COLUMN_TITLE, COLUMN_AUTHOR, COLUMN_DUEDATE, COLUMN_LOANDATE,
			COLUMN_SIGNATURE, COLUMN_RENEW_LINK, COLUMN_NO_RENEW_REASON,
			COLUMN_NUM_RENEWS, COLUMN_ACCOUNT, COLUMN_MEDIUM_ID };
	public static final DateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public static final String MEDIA_TABLE_NAME = "media";
	static final String MEDIA_TABLE_CREATE = String.format(
			//
			"CREATE TABLE %s (" + //
					"%s integer primary key," + //
					"%s text," + //
					"%s text," + //
					"%s integer," + //
					"%s integer," + //
					"%s text," + //
					"%s text," + //
					"%s text," + //
					"%s integer," + //
					"%s text," + //
					"%s text);", //
			MEDIA_TABLE_NAME, COLUMN_ID, COLUMN_TITLE, COLUMN_AUTHOR,
			COLUMN_DUEDATE, COLUMN_LOANDATE, COLUMN_SIGNATURE,
			COLUMN_RENEW_LINK, COLUMN_NO_RENEW_REASON, COLUMN_NUM_RENEWS,
			COLUMN_ACCOUNT, COLUMN_MEDIUM_ID);

	static final String MEDIA_TABLE_DROP = "DROP TABLE IF EXISTS "
			+ MEDIA_TABLE_NAME;
}