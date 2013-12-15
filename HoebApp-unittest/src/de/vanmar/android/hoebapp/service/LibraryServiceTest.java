package de.vanmar.android.hoebapp.service;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.shadows.ShadowContentProviderOperation;
import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.bo.MediaDetails;
import de.vanmar.android.hoebapp.bo.RenewItem;
import de.vanmar.android.hoebapp.db.MediaContentProvider;
import de.vanmar.android.hoebapp.db.MediaDbHelper;
import de.vanmar.android.hoebapp.test.mocking.MockResponses;
import de.vanmar.android.hoebapp.test.mocking.TestUtils;
import de.vanmar.android.hoebapp.util.HtmlMockAnswer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class LibraryServiceTest {

	private LibraryService_ libraryService;
	private Context context;

	@Mock
	private ContentResolver contentResolver;

	@Mock
	private AssetManager assetManager;

	@Before
	public void setUp() throws Exception {
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
		libraryService = LibraryService_.getInstance_(context);

		TestUtils.initMocks(context);
		given(assetManager.open(anyString())).willAnswer(new HtmlMockAnswer());
	}

	@Test
	public void shouldRefreshMediaList() throws Exception {
		// given
		givenStandardLogin();
		MockResponses.forRequestDoAnswer(".*fn=MyLoans.*", "medialist.html");
		MockResponses.forRequestDoAnswer(".*Method=PageDown.*",
				"medialistbottom.html");

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
		final ShadowContentProviderOperation deleteOperation = Robolectric
				.shadowOf(operations.get(0));
		assertThat("delete old", deleteOperation.isDelete(), is(true));
		assertThat("delete old", deleteOperation.getUri(),
				is(equalTo(MediaContentProvider.CONTENT_URI)));

		for (int i = 1; i < 12; i++) {
			final ShadowContentProviderOperation insertOperation = Robolectric
					.shadowOf(operations.get(i));
			assertThat("insert " + i, insertOperation.isInsert(), is(true));
			assertThat("insert " + i, insertOperation.getUri(),
					is(equalTo(MediaContentProvider.CONTENT_URI)));
			assertThat(
					"insert " + i,
					(String) insertOperation.getValues().get(
							MediaDbHelper.COLUMN_ACCOUNT),
					is(equalTo("username")));
		}
		final ShadowContentProviderOperation insertOperation = Robolectric
				.shadowOf(operations.get(1));
		assertThat(insertOperation.getValues().get(MediaDbHelper.COLUMN_AUTHOR).toString(), is("Kein Autor"));
		assertThat(insertOperation.getValues().get(MediaDbHelper.COLUMN_TITLE).toString(), is("Das Ende ist mein Anfang"));
		assertThat(insertOperation.getValues().get(MediaDbHelper.COLUMN_MEDIUM_ID).toString(), is("T014946840"));
		assertThat(insertOperation.getValues().get(MediaDbHelper.COLUMN_SIGNATURE).toString(), is("M60 005 431 3"));
		assertThat(insertOperation.getValues().get(MediaDbHelper.COLUMN_NO_RENEW_REASON).toString(), is("Verl&auml;ngerungslimit erreicht"));
		assertThat(insertOperation.getValues().get(MediaDbHelper.COLUMN_NUM_RENEWS).toString(), is("2"));

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd/MM/yyyy", Locale.GERMAN);
		assertThat((Long) insertOperation.getValues().get(MediaDbHelper.COLUMN_LOANDATE), is(dateFormat.parse("29/11/2013").getTime()));
		assertThat((Long) insertOperation.getValues().get(MediaDbHelper.COLUMN_DUEDATE), is(dateFormat.parse("20/12/2013").getTime()));
		assertThat(insertOperation.getValues().get(MediaDbHelper.COLUMN_RENEW_LINK), is(nullValue()));

		final ShadowContentProviderOperation insertOperation2 = Robolectric
				.shadowOf(operations.get(2));
		assertThat(insertOperation2.getValues().get(MediaDbHelper.COLUMN_RENEW_LINK).toString(), is("Obj_2871387029199?Style=Portal3&SubStyle=&Lang=GER&ResponseEncoding=utf-8&Method=Renew&Style=Portal3&Item=405"));
	}

	@Test
	public void shouldRefreshMediaListWithTwoAccounts() throws Exception {
		// given
		TestUtils.setUserdata(context, new Account("username1", "password1"),
				new Account("username2", "password2"));
		MockResponses.reset();
		MockResponses.forRequestDoAnswer(".*fn=Login.*", "loginform.html");
		MockResponses.forRequestDoAnswer(".*alswww2.dll/Obj_567281354477961.*",
				"loginsuccess.html");
		MockResponses.forRequestDoAnswer(".*fn=MyLoans.*", "medialist.html",
				"medialist2.html");
		MockResponses.forRequestDoAnswer(".*Method=PageDown.*",
				"medialistbottom.html", "medialistbottom.html",
				"medialist2bottom.html", "medialist2bottom.html");

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

		assertThat("1 delete, 11 inserts (account1), 18 inserts (account2)",
				operations.size(), is(30));
		final ShadowContentProviderOperation deleteOperation = Robolectric
				.shadowOf(operations.get(0));
		assertThat("delete old", deleteOperation.isDelete(), is(true));
		assertThat("delete old", deleteOperation.getUri(),
				is(equalTo(MediaContentProvider.CONTENT_URI)));

		for (int i = 1; i < 12; i++) {
			final ShadowContentProviderOperation insertOperation = Robolectric
					.shadowOf(operations.get(i));
			assertThat("insert " + i, insertOperation.isInsert(), is(true));
			assertThat("insert " + i, insertOperation.getUri(),
					is(equalTo(MediaContentProvider.CONTENT_URI)));
			assertThat(
					"insert " + i,
					(String) insertOperation.getValues().get(
							MediaDbHelper.COLUMN_ACCOUNT),
					is(equalTo("username1")));
		}
	}

	@Test
	public void shouldFailWithWrongLogin() throws Exception {
		// given
		TestUtils.setUserdata(context, new Account("username", "password"));
		MockResponses.reset();
		MockResponses.forRequestDoAnswer(".*fn=Login.*", "loginform.html");
		MockResponses.forRequestDoAnswer(".*alswww2.dll/Obj_567281354477961.*",
				"loginfailed.html");

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
		TestUtils.setUserdata(context, new Account("username1", "password1"),
				new Account("username2", "password2"));
		MockResponses.reset();
		MockResponses.forRequestDoAnswer(".*fn=Login.*", "loginform.html");
		MockResponses.forRequestDoAnswer(".*alswww2.dll/Obj_567281354477961.*",
				"loginsuccess.html");
		MockResponses.forRequestDoAnswer(".*fn=MyLoans.*", "medialist.html");
		MockResponses.forRequestDoAnswer(".*Method=PageDown.*",
				"medialistbottom.html");
		MockResponses
				.forRequestDoAnswer(
						"https://www.buecherhallen.de/alswww2.dll/Obj_2871387029199\\?Style=Portal3&SubStyle=&Lang=GER&ResponseEncoding=utf-8&Method=Renew&Style=Portal3&Item=536",
						"renewresult.html");

		// when
		libraryService.renewMedia(Collections.singleton(new RenewItem(
				"username1", "M59 551 458 7")), context);

		// then
		final List<Object> answers = MockResponses.getAnswerLog();
		// assert correct sequence of calls
		assertThat("get login form", (String) answers.get(0),
				is("loginform.html"));
		assertThat("login username1", (String) answers.get(1),
				is("loginsuccess.html"));
		assertThat("medialist", (String) answers.get(2), is("medialist.html"));
		assertThat("continue medialist", (String) answers.get(3),
				is("medialistbottom.html"));
		assertThat("renew item", (String) answers.get(4),
				is("renewresult.html"));
		assertThat("continue medialist (establish bottom)",
				(String) answers.get(5), is("medialistbottom.html"));

		assertThat("get login form", (String) answers.get(6),
				is("loginform.html"));
		assertThat("login username1", (String) answers.get(7),
				is("loginsuccess.html"));
		assertThat("medialist", (String) answers.get(8), is("medialist.html"));
		assertThat("continue medialist", (String) answers.get(9),
				is("medialistbottom.html"));
		assertThat("continue medialist (establish bottom (no renew here))",
				(String) answers.get(10), is("medialistbottom.html"));
	}

	@Test
	public void shouldLoadEmptyNotepadList() throws TechnicalException {
		// given
		givenStandardLogin();
		MockResponses.forRequestDoAnswer(".*ViewNotepad.*", "notepadEmpty.html");
		MockResponses.forRequestDoAnswer(".*LoadingJSON.*", "notepadLoading100.json");
		MockResponses.forRequestDoAnswer(".*ShowNotes.*", "notepadEmpty.html");

		// when
		List<MediaDetails> notepad = libraryService.loadNotepad();

		// then
		assertThat(notepad.size(), is(0));
	}

	@Test
	public void shouldLoadShortNotepadList() throws TechnicalException {
		// given
		givenStandardLogin();
		MockResponses.forRequestDoAnswer(".*ViewNotepad.*", "notepadShort.html");

		// when
		List<MediaDetails> notepad = libraryService.loadNotepad();

		// then
		assertThat(notepad.size(), is(4));
	}

	@Test
	public void shouldLoadMultipageNotepadList() throws TechnicalException {
		// given
		givenStandardLogin();
		MockResponses.forRequestDoAnswer(".*ViewNotepad.*", "notepadNeedsLoading.html");
		MockResponses.forRequestDoAnswer(".*LoadingJSON.*", "notepadLoading100.json");
		MockResponses.forRequestDoAnswer(".*ShowNotes.*", "notepadLongPage1.html");
		MockResponses.forRequestDoAnswer(".*Method=PageDown.*", "notepadLongPage2.html");

		// when
		List<MediaDetails> notepad = libraryService.loadNotepad();

		// then
		assertThat(notepad.size(), is(11));
	}

	@Test
	public void shouldAddToNotepad() throws TechnicalException {
		// given
		givenStandardLogin();
		MockResponses.forRequestDoAnswer(".*Method=NoteItem&id=T1234567890", "addToNotepad.json");

		// when
		libraryService.addToNotepad(new Account("username", "password"), "T1234567890");

		// then
		assertTrue("add to notepad called", MockResponses.getAnswerLog().contains("addToNotepad.json"));
	}

	@Test
	public void shouldRemoveFromNotepad() throws TechnicalException {
		// given
		givenStandardLogin();
		MockResponses.forRequestDoAnswer(".*Method=DeleteNote&id=T1234567890", "removeFromNotepad.json");

		// when
		libraryService.removeFromNotepad(new Account("username", "password"), "T1234567890");

		// then
		assertTrue("add to notepad called", MockResponses.getAnswerLog().contains("removeFromNotepad.json"));
	}

	private void givenStandardLogin() {
		TestUtils.setUserdata(context, new Account("username", "password"));
		MockResponses.reset();
		MockResponses.forRequestDoAnswer(".*fn=Login.*", "loginform.html");
		MockResponses.forRequestDoAnswer(".*alswww2.dll/Obj_567281354477961.*",
				"loginsuccess.html");
	}
}
