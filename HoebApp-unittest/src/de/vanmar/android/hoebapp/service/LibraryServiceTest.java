package de.vanmar.android.hoebapp.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.vanmar.android.hoebapp.bo.MediaDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.shadows.ShadowContentProviderOperation;

import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.bo.RenewItem;
import de.vanmar.android.hoebapp.db.MediaContentProvider;
import de.vanmar.android.hoebapp.db.MediaDbHelper;
import de.vanmar.android.hoebapp.test.mocking.MockResponses;
import de.vanmar.android.hoebapp.test.mocking.TestUtils;
import de.vanmar.android.hoebapp.util.HtmlMockAnswer;

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
		assertThat("1 delete, 18 inserts", operations.size(), is(19));
		final ShadowContentProviderOperation deleteOperation = Robolectric
				.shadowOf(operations.get(0));
		assertThat("delete old", deleteOperation.isDelete(), is(true));
		assertThat("delete old", deleteOperation.getUri(),
				is(equalTo(MediaContentProvider.CONTENT_URI)));

		for (int i = 1; i < 19; i++) {
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
				"medialist2.html");

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

		assertThat("1 delete, 18 inserts (account1), 10 inserts (account2)",
				operations.size(), is(29));
		final ShadowContentProviderOperation deleteOperation = Robolectric
				.shadowOf(operations.get(0));
		assertThat("delete old", deleteOperation.isDelete(), is(true));
		assertThat("delete old", deleteOperation.getUri(),
				is(equalTo(MediaContentProvider.CONTENT_URI)));

		for (int i = 1; i < 19; i++) {
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
						"https://www.buecherhallen.de/alswww2.dll/Obj_645831354482846\\?Style=Portal3&SubStyle=&Lang=GER&ResponseEncoding=utf-8&Method=Renew&Style=Portal3&Item=111670",
						"renewresult.html");

		// when
		libraryService.renewMedia(Collections.singleton(new RenewItem(
				"username1", "M59 824 795 5")), context);

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

    private void givenStandardLogin() {
        TestUtils.setUserdata(context, new Account("username", "password"));
        MockResponses.reset();
        MockResponses.forRequestDoAnswer(".*fn=Login.*", "loginform.html");
        MockResponses.forRequestDoAnswer(".*alswww2.dll/Obj_567281354477961.*",
                "loginsuccess.html");
    }
}
