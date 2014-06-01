package de.vanmar.android.hoebapp.bo;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import de.vanmar.android.hoebapp.R;
import de.vanmar.android.hoebapp.bo.Account.Appearance;
import de.vanmar.android.hoebapp.dialog.AccountDialog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class AccountDialogTest {

	private AccountDialog dialog;

	private Context context;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		context = new Activity();
	}

	private void createDialog(final Account account) {
		dialog = new AccountDialog(context, account);
		dialog.show();
	}

	private void editData(final CharSequence newUsername,
			final CharSequence newPassword) {
		((EditText) dialog.findViewById(R.id.username)).setText(newUsername);
		((EditText) dialog.findViewById(R.id.password)).setText(newPassword);
	}

	private void changeAppearance() {
		((Button) dialog.findViewById(R.id.appearance)).performClick();
	}

	@Test
	public void shouldShowInitiallyEmpty() {
		// given
		createDialog(null);

		// then
		assertThat(((EditText) dialog.findViewById(R.id.username)).getText()
				.toString(), is(""));
		assertThat(((EditText) dialog.findViewById(R.id.password)).getText()
				.toString(), is(""));
	}

	@Test
	public void shouldShowInitialValues() {
		// given
		createDialog(new Account("user", "user", "pass", Appearance.GREEN));

		// then
		assertThat(((EditText) dialog.findViewById(R.id.username)).getText()
				.toString(), is("user"));
		assertThat(((EditText) dialog.findViewById(R.id.password)).getText()
				.toString(), is("pass"));
	}

	@Test
	public void shouldHideDeleteButtonOnNewAccount() {
		// given
		createDialog(null);

		// then
		assertThat(dialog.findViewById(R.id.delete).getVisibility(),
				is(View.GONE));
	}

	@Test
	public void shouldShowDeleteButtonOnEditAccount() {
		// given
		createDialog(new Account("username", "password"));

		// then
		assertThat(dialog.findViewById(R.id.delete).getVisibility(),
				is(View.VISIBLE));
	}

	@Test
	public void shouldReturnNullOnCancelWhenStartedWithNull() {
		// given
		createDialog(null);

		// when
		dialog.findViewById(R.id.cancel).performClick();

		// then
		assertThat(dialog.isShowing(), is((false)));
		assertThat(dialog.getAccount(), is(nullValue()));
	}

	@Test
	public void shouldReturnUnchangedOnCancelWhenStartedWithAccount() {
		// given
		final Account account = new Account("aa", "aa", "bb", Appearance.BLUE);
		createDialog(account);

		// when
		editData("cc", "dd");
		changeAppearance();
		dialog.findViewById(R.id.cancel).performClick();

		// then
		assertThat(dialog.isShowing(), is((false)));
		assertThat(dialog.getAccount(), is(sameInstance(account)));
	}

	@Test
	public void shouldUseDialogDataOnSaveForNewAccount() {
		// given
		createDialog(null);

		editData("newUsername", "newPassword");
		changeAppearance();
		dialog.findViewById(R.id.save).performClick();

		// then
		assertThat(dialog.isShowing(), is((false)));
		assertThat(dialog.getAccount(), is(equalTo(new Account("newUsername", null,
				"newPassword", Appearance.BLUE))));

	}

	@Test
	public void shouldUseDialogDataOnSaveForEditAccount() {
		// given
		createDialog(new Account("oldUsername", "oldUsername", "oldPassword", Appearance.BLUE));

		editData("newUsername", "newPassword");
		changeAppearance();
		dialog.findViewById(R.id.save).performClick();

		// then
		assertThat(dialog.isShowing(), is((false)));
		assertThat(dialog.getAccount(), is(equalTo(new Account("newUsername", null,
				"newPassword", Appearance.RED))));
	}

	@Test
	public void shouldReturnNullOnDeleteAccount() {
		// given
		final Account account = new Account("aa", "bb");
		createDialog(account);

		// when
		editData("cc", "dd");
		dialog.findViewById(R.id.delete).performClick();

		// then
		assertThat(dialog.isShowing(), is((false)));
		assertThat(dialog.getAccount(), is(nullValue()));
	}

	@Test
	public void shouldFocusUsernameIfEmptyOnSave() {
		// given
		createDialog(new Account("oldUsername", "oldPassword"));

		editData("", "");
		dialog.findViewById(R.id.save).performClick();

		// then
		assertThat(dialog.isShowing(), is((true)));
		assertTrue(dialog.findViewById(R.id.username).isFocused());
	}

	@Test
	public void shouldFocusPasswordIfEmptyOnSave() {
		// given
		createDialog(new Account("oldUsername", "oldPassword"));

		editData("newUsername", "");
		dialog.findViewById(R.id.save).performClick();

		// then
		assertThat(dialog.isShowing(), is((true)));
		assertTrue(dialog.findViewById(R.id.password).isFocused());
	}

	@Test
	public void shouldCycleAppearances() {
		// given
		createDialog(new Account("username", "username", "password", Appearance.NONE));

		// when
		changeAppearance();
		dialog.findViewById(R.id.save).performClick();

		// then
		assertThat(dialog.getAccount().getAppearance(), is(Appearance.BLUE));

		// when
		changeAppearance();
		dialog.findViewById(R.id.save).performClick();

		// then
		assertThat(dialog.getAccount().getAppearance(), is(Appearance.RED));

		// when
		changeAppearance();
		dialog.findViewById(R.id.save).performClick();

		// then
		assertThat(dialog.getAccount().getAppearance(), is(Appearance.GREEN));

		// when
		changeAppearance();
		dialog.findViewById(R.id.save).performClick();

		// then
		assertThat(dialog.getAccount().getAppearance(), is(Appearance.PURPLE));

		// when
		changeAppearance();
		dialog.findViewById(R.id.save).performClick();

		// then
		assertThat(dialog.getAccount().getAppearance(), is(Appearance.YELLOW));

		// when
		changeAppearance();
		dialog.findViewById(R.id.save).performClick();

		// then
		assertThat(dialog.getAccount().getAppearance(), is(Appearance.SILVER));

		// when
		changeAppearance();
		dialog.findViewById(R.id.save).performClick();

		// then
		assertThat(dialog.getAccount().getAppearance(), is(Appearance.NONE));
	}
}
