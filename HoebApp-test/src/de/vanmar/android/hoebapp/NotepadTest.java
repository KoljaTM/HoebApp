package de.vanmar.android.hoebapp;

import android.test.ActivityInstrumentationTestCase2;
import com.jayway.android.robotium.solo.Solo;
import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.test.mocking.MockResponses;
import de.vanmar.android.hoebapp.test.mocking.TestUtils;

public class NotepadTest extends
		ActivityInstrumentationTestCase2<NotepadActivity_> {

	private Solo solo;

	public NotepadTest() {
		super(NotepadActivity_.class);
	}

	@Override
	public void setUp() throws Exception {
		TestUtils.initMocks(getInstrumentation().getContext());

	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	public void testLongNotepad() throws Exception {
        // given
        TestUtils.setUserdata(getInstrumentation().getTargetContext(), new Account("username", "password"));
        MockResponses.reset();
        MockResponses.forRequestDoAnswer(".*fn=Login.*", "loginform.html");
        MockResponses.forRequestDoAnswer(".*alswww2.dll/Obj_567281354477961.*",
                "loginsuccess.html");
        MockResponses.forRequestDoAnswer(".*ViewNotepad.*", "notepadNeedsLoading.html");
        MockResponses.forRequestDoAnswer(".*LoadingJSON.*", "notepadLoading100.json");
        MockResponses.forRequestDoAnswer(".*ShowNotes.*", "notepadLongPage1.html");
        MockResponses.forRequestDoAnswer(".*Method=PageDown.*", "notepadLongPage2.html");

        // when
        solo = new Solo(getInstrumentation(), getActivity());

        // then
        assertTrue(solo.searchText("Louis, Dirk"));
        assertTrue(solo.searchText("Jetzt lerne ich Android-4-Programmierung"));
        assertTrue(solo.searchText("Buch Erwachsene"));
        assertTrue(solo.searchText("Jk 0#LOUI"));
    }

}
