package de.vanmar.android.hoebapp.service;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.bo.MediaDetails;
import de.vanmar.android.hoebapp.bo.RenewItem;
import de.vanmar.android.hoebapp.db.MediaContentProvider;
import de.vanmar.android.hoebapp.db.MediaDbHelper;
import de.vanmar.android.hoebapp.test.mocking.SoapMockNanoHTTPD;
import de.vanmar.android.hoebapp.test.mocking.TestUtils;
import de.vanmar.android.hoebapp.util.MyRobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ksoap2.transport.HttpTransportSE;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowContentProviderOperation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.robolectric.shadows.ShadowContentProviderOperation.TYPE_DELETE;
import static org.robolectric.shadows.ShadowContentProviderOperation.TYPE_INSERT;

@RunWith(MyRobolectricTestRunner.class)
public class SoapLibraryServiceTest {

	public static final String CHECKED_USERNAME = "use rna me";
	private SoapLibraryService libraryService;
	private Context context;

	@Mock
	private ContentResolver contentResolver;

	@Mock
	private AssetManager assetManager;
	@Mock
	private HttpTransportFactory mockHttpTransportFactory;
	@Mock
	private HttpTransportSE mockHttpTransport;

	public static SoapMockNanoHTTPD httpMock;

	@Before
	public void setUp() throws Exception {
		if (httpMock == null) {
			// cannot use @BeforeClass
			// because unfortunately RobolectricTestRunner runs the @BeforeClass method in another classLoader.
			httpMock = new SoapMockNanoHTTPD();
		}
		MockitoAnnotations.initMocks(this);
		context = new Activity() {

			@Override
			public ContentResolver getContentResolver() {
				return contentResolver;
			}

			@Override
			public AssetManager getAssets() {
				return assetManager;
			}

		};
		libraryService = SoapLibraryService_.getInstance_(context);
		libraryService.httpTransportFactory = mockHttpTransportFactory;
		given(mockHttpTransportFactory.getHttpTransport(anyString())).willAnswer(new Answer<HttpTransportSE>() {
			@Override
			public HttpTransportSE answer(InvocationOnMock invocationOnMock) throws Throwable {
				String url = (String) invocationOnMock.getArguments()[0];
				return new HttpTransportSE(getMockUrl(url));
			}
		});

//		given(assetManager.open(anyString())).willAnswer(new HtmlMockAnswer());
		httpMock.clearUris();
	}

	private String getMockUrl(String url) {
		String replaced = url.replaceAll("(http|https)://[^/]*", "http://localhost:8888");
		System.out.println(url);
		System.out.println(replaced);
		return replaced;
	}

	@Test
	public void shouldRefreshMediaList() throws Exception {
		// given
		givenStandardLogin();

		// when
		libraryService.refreshMediaList(context);

		// then
		@SuppressWarnings("unchecked")
		final ArgumentCaptor<ArrayList<ContentProviderOperation>> capturer = (ArgumentCaptor<ArrayList<ContentProviderOperation>>) (Object) ArgumentCaptor
				.forClass(ArrayList.class);
		verify(contentResolver).applyBatch(eq(MediaContentProvider.AUTHORITY),
				capturer.capture());
		final ArrayList<ContentProviderOperation> operations = capturer
				.getValue();
		assertThat("1 delete, 11 inserts", operations.size(), is(12));
		ContentProviderOperation operation = operations.get(0);
		final ShadowContentProviderOperation deleteOperation = Robolectric
				.shadowOf(operation);
		assertThat("delete old", deleteOperation.getType() == TYPE_DELETE, is(true));
		assertThat("delete old", operation.getUri(),
				is(equalTo(MediaContentProvider.CONTENT_URI)));

		for (int i = 1; i < 11; i++) {
			ContentProviderOperation operationI = operations.get(i);
			final ShadowContentProviderOperation insertOperation = Robolectric
					.shadowOf(operationI);
			assertThat("insert " + i, insertOperation.getType() == TYPE_INSERT, is(true));
			assertThat("insert " + i, operationI.getUri(),
					is(equalTo(MediaContentProvider.CONTENT_URI)));
			assertThat(
					"insert " + i,
					(String) insertOperation.getContentValues().get(
							MediaDbHelper.COLUMN_ACCOUNT),
					is(equalTo("username"))
			);
		}
		final ShadowContentProviderOperation insertOperation = Robolectric
				.shadowOf(operations.get(1));
		assertThat(insertOperation.getContentValues().get(MediaDbHelper.COLUMN_AUTHOR).toString(), is("Stein, Arnd"));
		assertThat(insertOperation.getContentValues().get(MediaDbHelper.COLUMN_TITLE).toString(), is("•<Die>• Wunschinsel ..."));
		assertThat(insertOperation.getContentValues().get(MediaDbHelper.COLUMN_MEDIUM_ID).toString(), is("T012370097"));
		assertThat(insertOperation.getContentValues().get(MediaDbHelper.COLUMN_SIGNATURE).toString(), is("M58 888 140 4"));
		assertThat(insertOperation.getContentValues().get(MediaDbHelper.COLUMN_NO_RENEW_REASON).toString(), is("Nicht gesperrt"));
		assertThat(insertOperation.getContentValues().get(MediaDbHelper.COLUMN_NUM_RENEWS).toString(), is("0"));

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd/MM/yyyy", Locale.GERMAN);
		assertThat((Long) insertOperation.getContentValues().get(MediaDbHelper.COLUMN_LOANDATE), is(dateFormat.parse("20/05/2014").getTime()));
		assertThat((Long) insertOperation.getContentValues().get(MediaDbHelper.COLUMN_DUEDATE), is(dateFormat.parse("17/06/2014").getTime()));
		assertThat((Integer) insertOperation.getContentValues().get(MediaDbHelper.COLUMN_CAN_RENEW), is(1));

		final ShadowContentProviderOperation insertOperation2 = Robolectric
				.shadowOf(operations.get(2));
		assertThat((Integer) insertOperation2.getContentValues().get(MediaDbHelper.COLUMN_CAN_RENEW), is(1));
	}

	@Test
	public void shouldRefreshMediaListWithTwoAccounts() throws Exception {
		// given
		TestUtils.setUserdata(context, new Account("username1", "password1"),
				new Account("user2name", "password2"));

		// when
		libraryService.refreshMediaList(context);

		// then
		@SuppressWarnings("unchecked")
		final ArgumentCaptor<ArrayList<ContentProviderOperation>> capturer = (ArgumentCaptor<ArrayList<ContentProviderOperation>>) (Object) ArgumentCaptor
				.forClass(ArrayList.class);
		verify(contentResolver).applyBatch(eq(MediaContentProvider.AUTHORITY),
				capturer.capture());
		final ArrayList<ContentProviderOperation> operations = capturer
				.getValue();

		assertThat("1 delete, 11 inserts (account1), 19 inserts (account2)",
				operations.size(), is(31));
		ContentProviderOperation operation = operations.get(0);
		final ShadowContentProviderOperation deleteOperation = Robolectric
				.shadowOf(operation);
		assertThat("delete old", deleteOperation.getType() == TYPE_DELETE, is(true));
		assertThat("delete old", operation.getUri(),
				is(equalTo(MediaContentProvider.CONTENT_URI)));

		for (int i = 1; i < 12; i++) {
			ContentProviderOperation operationI = operations.get(i);
			final ShadowContentProviderOperation insertOperation = Robolectric
					.shadowOf(operationI);
			assertThat("insert " + i, insertOperation.getType() == TYPE_INSERT, is(true));
			assertThat("insert " + i, operationI.getUri(),
					is(equalTo(MediaContentProvider.CONTENT_URI)));
			assertThat(
					"insert " + i,
					(String) insertOperation.getContentValues().get(
							MediaDbHelper.COLUMN_ACCOUNT),
					is(equalTo("username1"))
			);
		}
	}

	@Test
	public void shouldFailWithWrongLogin() throws Exception {
		// given
		TestUtils.setUserdata(context, new Account("error", "password"));

		// when
		try {
			libraryService.refreshMediaList(context);
			fail("expected exception");
		} catch (final LoginFailedException expected) {
			// expected
		}
	}

	@Test
	public void shouldRenewMediaItem() throws Exception {
		// given
		Account account = new Account("username", "use rna me", "password1", Account.Appearance.GREEN);
		Account account2 = new Account("user2name", "use r2n ame", "password2", Account.Appearance.BLUE);
		TestUtils.setUserdata(context, account, account2);

		// when
		libraryService.renewMedia(Collections.singleton(new RenewItem(account, "M59 551 458 7")), context);

		// then
		// assert correct sequence of calls
		List<String> uris = httpMock.getCalledUris();
		assertFalse("dont check username, because checkedUsername is known", uris.contains("/app_webuser/WebUserSvc.asmx/CheckBorrower.xml"));
		assertThat("renew item", uris.get(0), is("/app_webuser/WebUserSvc.asmx/RenewItem.xml"));
		assertThat("refresh list user1", uris.get(1), is("/app_webuser/WebUserSvc.asmx/GetBorrowerLoans.xml"));
		assertThat("refresh list user2", uris.get(2), is("/app_webuser/WebUserSvc.asmx/GetBorrowerLoans2.xml"));
	}

	@Test
	public void shouldLoadNotepadList() throws TechnicalException {
		// given
		givenStandardLogin();

		// when
		List<MediaDetails> notepad = libraryService.loadNotepad();

		// then
		assertThat(notepad.size(), is(6));
	}

	@Test
	public void shouldAddToNotepad() throws TechnicalException {
		// given
		givenStandardLogin();

		// when
		libraryService.addToNotepad(new Account("username", "password"), "T1234567890");

		// then
		assertTrue("add to notepad called", httpMock.getCalledUris().contains("/app_ZonesServices/Service1.asmx/NoteRecord.xml"));
	}

	@Test
	public void shouldRemoveFromNotepad() throws TechnicalException {
		// given
		givenStandardLogin();

		// when
		libraryService.removeFromNotepad(new Account("username", "password"), "T1234567890");

		// then
		assertTrue("remove from notepad called", httpMock.getCalledUris().contains("/app_ZonesServices/Service1.asmx/DeleteNote.xml"));
	}

	private void givenStandardLogin() {
		TestUtils.setUserdata(context, new Account("username", "password"));
	}

}
