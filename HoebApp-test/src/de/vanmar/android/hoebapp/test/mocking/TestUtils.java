package de.vanmar.android.hoebapp.test.mocking;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import android.content.ContentProviderClient;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import de.vanmar.android.hoebapp.HoebAppActivity;
import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.db.MediaContentProvider;
import de.vanmar.android.hoebapp.util.HttpCallBuilder;
import de.vanmar.android.hoebapp.util.Preferences_;

public class TestUtils {
	private static final String DB_NAME = "hoebdata";
	private static final String DB_PATH = "/data/data/de.vanmar.android.hoebapp/databases/hoebdata";

	/**
	 * Initialize the app with
	 * <ul>
	 * <li>Empty database</li>
	 * <li>EULA accepted</li>
	 * <li>Dummy account set</li>
	 * </ul>
	 * 
	 * @param context
	 */
	public static void initEmpty(final Context context) {
		prepareTestDatabase(context);
		acceptedEula(context);
		setUserdata(context, new Account("xxx", "yyy"));
	}

	/**
	 * Initializes the database to empty
	 * 
	 * @param context
	 * @throws IOException
	 */
	public static void prepareTestDatabase(final Context context) {
		context.deleteDatabase(DB_NAME);
		refreshContentProvider(context);
	}

	private static void refreshContentProvider(final Context context) {
		final Uri refreshUri = Uri.parse(MediaContentProvider.CONTENT_URI
				+ MediaContentProvider.MEDIA_REFRESH);
		final ContentProviderClient contentProvider = context
				.getContentResolver().acquireContentProviderClient(
						MediaContentProvider.CONTENT_URI);
		try {
			contentProvider.query(refreshUri, null, null, null, null).close();
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
	}

	/** Initializes the database from the given asset file */
	public static void prepareTestDatabase(final Context testContext,
			final Context targetContext, final String filename) {
		targetContext.deleteDatabase(DB_NAME);

		InputStream in = null;
		OutputStream out = null;
		try {
			in = testContext.getAssets().open("testdbs/" + filename);
			out = new FileOutputStream(DB_PATH);
			copyFile(in, out);
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
		} catch (final IOException e) {
			Log.e("tag", "Failed to copy asset file: " + filename, e);
		}
		refreshContentProvider(targetContext);
	}

	private static void copyFile(final InputStream in, final OutputStream out)
			throws IOException {
		final byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	public static void initMocks(final Context context) {
		HttpCallBuilder.setHttpClient(new MockHttpClient());
		MockHttpClient.setContext(context);
	}

	public static void noMocks() {
		HttpCallBuilder.setHttpClient(null);
	}

	public static void setUserdata(final Context context,
			final Account... accountArray) {
		final Preferences_ prefs = new Preferences_(context);
		final List<Account> accounts = Arrays.asList(accountArray);
		prefs.accounts().put(Account.toString(accounts));
	}

	public static void acceptedEula(final Context context) {
		final Preferences_ prefs = new Preferences_(context);
		prefs.acceptedEULA().put(HoebAppActivity.EULA_VERSION);
	}

}
