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

public class LocationContentProvider extends ContentProvider {

	// database
	private HoebAppDbHelper database;

	// Used for the UriMacher
	private static final int LOCATIONS = 10;
	private static final int LOCATION_ID = 20;

	private static final String AUTHORITY = "de.vanmar.android.hoebapp.contentprovider.locations";

	private static final String BASE_PATH = "location";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/locations";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/location";

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, LOCATIONS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", LOCATION_ID);
	}

	@Override
	public boolean onCreate() {
		database = new HoebAppDbHelper(getContext());
		return false;
	}

	@Override
	public ContentProviderResult[] applyBatch(
			ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		try {
			database.getWritableDatabase().beginTransaction();
			ContentProviderResult[] result = super.applyBatch(operations);
			database.getWritableDatabase().setTransactionSuccessful();
			return result;
		} finally {
			database.getWritableDatabase().endTransaction();
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case LOCATIONS:
			rowsDeleted = sqlDB.delete(LocationDbHelper.LOCATION_TABLE_NAME,
					selection, selectionArgs);
			break;
		case LOCATION_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(
						LocationDbHelper.LOCATION_TABLE_NAME,
						LocationDbHelper.COLUMN_ID + "=" + id, null);
			} else {
				rowsDeleted = sqlDB.delete(
						LocationDbHelper.LOCATION_TABLE_NAME,
						LocationDbHelper.COLUMN_ID + "=" + id + " and "
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
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		long id = 0;
		switch (uriType) {
		case LOCATIONS:
			id = sqlDB.insert(LocationDbHelper.LOCATION_TABLE_NAME, null,
					values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Uisng SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Check if the caller has requested a column which does not exists
		checkColumns(projection);

		// Set the table
		queryBuilder.setTables(LocationDbHelper.LOCATION_TABLE_NAME);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case LOCATIONS:
			break;
		case LOCATION_ID:
			// Adding the ID to the original query
			queryBuilder.appendWhere(LocationDbHelper.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case LOCATIONS:
			rowsUpdated = sqlDB.update(LocationDbHelper.LOCATION_TABLE_NAME,
					values, selection, selectionArgs);
			break;
		case LOCATION_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(
						LocationDbHelper.LOCATION_TABLE_NAME, values,
						LocationDbHelper.COLUMN_ID + "=" + id, null);
			} else {
				rowsUpdated = sqlDB.update(
						LocationDbHelper.LOCATION_TABLE_NAME, values,
						LocationDbHelper.COLUMN_ID + "=" + id + " and "
								+ selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	private void checkColumns(String[] projection) {
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(
					Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(
					Arrays.asList(LocationDbHelper.ALL_COLUMNS));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}
}
