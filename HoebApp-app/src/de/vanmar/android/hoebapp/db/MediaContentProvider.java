package de.vanmar.android.hoebapp.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class MediaContentProvider extends ContentProvider {

	// database
	private HoebAppDbHelper database;

	// Used for the UriMacher
	private static final int MEDIA = 10;
	private static final int MEDIA_ID = 20;
	private static final int AGGREGATE = 30;
	private static final int REFRESH = 40;

	public static final String AUTHORITY = "de.vanmar.android.hoebapp.contentprovider.media";

	private static final String BASE_PATH = "media";
	public static final String MEDIA_AGGREGATE = "/media_aggregate";
	public static final String MEDIA_REFRESH = "/refresh";
	private static final String AGGREGATE_PATH = BASE_PATH + MEDIA_AGGREGATE;
	private static final String REFRESH_PATH = BASE_PATH + MEDIA_REFRESH;

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/media";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/media_item";
	public static final String CONTENT_AGGREGATE_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ MEDIA_AGGREGATE;

	public static final int COLUMN_INDEX_AGGREGATE_COUNT = 0;
	public static final int COLUMN_INDEX_AGGREGATE_MIN_DUEDATE = 1;
	private static final String AGGREGATE_QUERY = String.format(
			"Select count(*), min(%s) from %s", MediaDbHelper.COLUMN_DUEDATE,
			MediaDbHelper.MEDIA_TABLE_NAME);

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, MEDIA);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", MEDIA_ID);
		sURIMatcher.addURI(AUTHORITY, AGGREGATE_PATH, AGGREGATE);
		sURIMatcher.addURI(AUTHORITY, REFRESH_PATH, REFRESH);
	}

	@Override
	public boolean onCreate() {
		database = new HoebAppDbHelper(getContext());
		return false;
	}

	@Override
	public ContentProviderResult[] applyBatch(
			final ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		try {
			database.getWritableDatabase().beginTransaction();
			final ContentProviderResult[] result = super.applyBatch(operations);
			database.getWritableDatabase().setTransactionSuccessful();
			return result;
		} finally {
			database.getWritableDatabase().endTransaction();
		}
	}

	@Override
	public int delete(final Uri uri, final String selection,
			final String[] selectionArgs) {
		final int uriType = sURIMatcher.match(uri);
		final SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case MEDIA:
			rowsDeleted = sqlDB.delete(MediaDbHelper.MEDIA_TABLE_NAME,
					selection, selectionArgs);
			break;
		case MEDIA_ID:
			final String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(MediaDbHelper.MEDIA_TABLE_NAME,
						MediaDbHelper.COLUMN_ID + "=" + id, null);
			} else {
				rowsDeleted = sqlDB.delete(MediaDbHelper.MEDIA_TABLE_NAME,
						MediaDbHelper.COLUMN_ID + "=" + id + " and "
								+ selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public String getType(final Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values) {
		final int uriType = sURIMatcher.match(uri);
		final SQLiteDatabase sqlDB = database.getWritableDatabase();
		long id = 0;
		switch (uriType) {
		case MEDIA:
			id = sqlDB.insert(MediaDbHelper.MEDIA_TABLE_NAME, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection,
			final String selection, final String[] selectionArgs,
			final String sortOrder) {

		// Using SQLiteQueryBuilder instead of query() method
		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Check if the caller has requested a column which does not exists
		checkColumns(projection);

		// Set the table
		queryBuilder.setTables(MediaDbHelper.MEDIA_TABLE_NAME);

		final int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case MEDIA:
			break;
		case MEDIA_ID:
			// Adding the ID to the original query
			queryBuilder.appendWhere(MediaDbHelper.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
		case AGGREGATE:
			final Cursor cursor = getCursorForAggregateQuery();
			// Make sure that potential listeners are getting notified
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
		case REFRESH:
			database.getWritableDatabase().close();
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		final SQLiteDatabase db = database.getReadableDatabase();
		final Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	private Cursor getCursorForAggregateQuery() {
		final SQLiteDatabase db = database.getWritableDatabase();
		final Cursor cursor = db.rawQuery(AGGREGATE_QUERY, null);
		return cursor;
	}

	@Override
	public int update(final Uri uri, final ContentValues values,
			final String selection, final String[] selectionArgs) {

		final int uriType = sURIMatcher.match(uri);
		final SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case MEDIA:
			rowsUpdated = sqlDB.update(MediaDbHelper.MEDIA_TABLE_NAME, values,
					selection, selectionArgs);
			break;
		case MEDIA_ID:
			final String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(MediaDbHelper.MEDIA_TABLE_NAME,
						values, MediaDbHelper.COLUMN_ID + "=" + id, null);
			} else {
				rowsUpdated = sqlDB.update(MediaDbHelper.MEDIA_TABLE_NAME,
						values, MediaDbHelper.COLUMN_ID + "=" + id + " and "
								+ selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	private void checkColumns(final String[] projection) {
		if (projection != null) {
			final HashSet<String> requestedColumns = new HashSet<String>(
					Arrays.asList(projection));
			final HashSet<String> availableColumns = new HashSet<String>(
					Arrays.asList(MediaDbHelper.ALL_COLUMNS));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}
}
