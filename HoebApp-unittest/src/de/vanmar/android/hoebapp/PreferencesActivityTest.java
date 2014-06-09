package de.vanmar.android.hoebapp;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.dialog.AccountDialog;
import de.vanmar.android.hoebapp.util.MyRobolectricTestRunner;
import de.vanmar.android.hoebapp.util.Preferences_;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.util.ActivityController;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(MyRobolectricTestRunner.class)
public class PreferencesActivityTest {

	private PreferencesActivity_ activity;
	private Preferences_ prefs;

	private CheckBox doAutoUpdate;
	private CheckBox doAutoUpdateWifiOnly;
	private ListView accountList;
	private Button addAccount;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		ActivityController<PreferencesActivity_> activityController = Robolectric.buildActivity(PreferencesActivity_.class);
		activityController.create();
		activity = activityController.get();

		prefs = activity.prefs;

		doAutoUpdate = (CheckBox) activity.findViewById(R.id.doAutoUpdate);
		doAutoUpdateWifiOnly = (CheckBox) activity
				.findViewById(R.id.doAutoUpdateWifiOnly);
		accountList = (ListView) activity.findViewById(R.id.accountList);
		addAccount = (Button) activity.findViewById(R.id.addAccount);
	}

	@Test
	public void shouldDisplayAutoUpdateFlags() {
		// given
		prefs.doAutoUpdate().put(true);
		prefs.doAutoUpdateWifiOnly().put(true);

		// when
		activity.onResume();

		// then
		assertTrue(doAutoUpdate.isChecked());
		assertTrue(doAutoUpdateWifiOnly.isChecked());
	}

	@Test
	public void shouldDisableAutoUpdateWifiFlag() {
		// given
		prefs.doAutoUpdate().put(false);
		prefs.doAutoUpdateWifiOnly().put(true);

		// when
		activity.onResume();

		// then
		assertFalse(doAutoUpdate.isChecked());
		assertFalse(doAutoUpdateWifiOnly.isEnabled());
		assertFalse(doAutoUpdateWifiOnly.isChecked());
	}

	@Test
	public void shouldDisplayAccounts() {
		// given
		final List<Account> accounts = Collections.singletonList(new Account(
				"username", "password"));
		prefs.accounts().put(Account.toString(accounts));

		// when
		activity.onResume();

		// then
		assertThat(accountList.getAdapter().getCount(),
				is(equalTo(accounts.size())));
		assertThat((Account) accountList.getAdapter().getItem(0),
				is(equalTo(accounts.get(0))));
	}

	@Test
	public void shouldSaveAutoUpdateFlag() {
		// given
		prefs.doAutoUpdate().put(false);
		prefs.doAutoUpdateWifiOnly().put(false);
		activity.onResume();

		// when
		assertFalse(doAutoUpdate.isChecked());
		doAutoUpdate.performClick();

		// then
		assertTrue(prefs.doAutoUpdate().get());
		assertTrue(doAutoUpdateWifiOnly.isEnabled());

		// when
		assertFalse(doAutoUpdateWifiOnly.isChecked());
		doAutoUpdateWifiOnly.performClick();

		// then
		assertTrue(prefs.doAutoUpdateWifiOnly().get());

		// when
		assertTrue(doAutoUpdate.isChecked());
		doAutoUpdate.performClick();

		// then
		assertFalse(prefs.doAutoUpdate().get());
		assertFalse(doAutoUpdateWifiOnly.isEnabled());
		assertFalse(prefs.doAutoUpdateWifiOnly().get());
	}

	@Test
	public void shouldAddAccount() {
		// given
		final List<Account> accounts = Collections.singletonList(new Account(
				"username", "password"));
		prefs.accounts().put(Account.toString(accounts));
		activity.onResume();

		// when
		addAccount.performClick();
		final AccountDialog accountDialog = activity.getAccountDialog();
		((EditText) accountDialog.findViewById(R.id.username))
				.setText("username2");
		((EditText) accountDialog.findViewById(R.id.password))
				.setText("password2");
		accountDialog.findViewById(R.id.save).performClick();

		// then
		assertThat(accountList.getAdapter().getCount(),
				is(equalTo(accounts.size() + 1)));
		assertThat(
				((Account) accountList.getAdapter().getItem(1)).getUsername(),
				is(equalTo("username2")));
		assertThat(
				((Account) accountList.getAdapter().getItem(1)).getPassword(),
				is(equalTo("password2")));
	}

	@Test
	public void shouldNotAddAccount() {
		// given
		final List<Account> accounts = Collections.singletonList(new Account(
				"username", "password"));
		prefs.accounts().put(Account.toString(accounts));
		activity.onResume();

		// when
		addAccount.performClick();
		final AccountDialog accountDialog = activity.getAccountDialog();
		((EditText) accountDialog.findViewById(R.id.username))
				.setText("username2");
		((EditText) accountDialog.findViewById(R.id.password))
				.setText("password2");
		accountDialog.findViewById(R.id.cancel).performClick();

		// then
		assertThat(accountList.getAdapter().getCount(),
				is(equalTo(accounts.size())));
		assertThat(
				((Account) accountList.getAdapter().getItem(0)).getUsername(),
				is(equalTo("username")));
		assertThat(
				((Account) accountList.getAdapter().getItem(0)).getPassword(),
				is(equalTo("password")));
	}

	@Test
	public void shouldEditAccount() {
		// given
		final List<Account> accounts = Collections.singletonList(new Account(
				"username", "password"));
		prefs.accounts().put(Account.toString(accounts));
		activity.onResume();

		// when
		final int position = 0;
		accountList.getAdapter().getView(position, null, null).performClick();
		final AccountDialog accountDialog = activity.getAccountDialog();
		((EditText) accountDialog.findViewById(R.id.username))
				.setText("username2");
		((EditText) accountDialog.findViewById(R.id.password))
				.setText("password2");
		accountDialog.findViewById(R.id.save).performClick();

		// then
		assertThat(accountList.getAdapter().getCount(),
				is(equalTo(accounts.size())));
		assertThat(
				((Account) accountList.getAdapter().getItem(0)).getUsername(),
				is(equalTo("username2")));
		assertThat(
				((Account) accountList.getAdapter().getItem(0)).getPassword(),
				is(equalTo("password2")));
	}

	@Test
	public void shouldNotEditAccount() {
		// given
		final List<Account> accounts = Collections.singletonList(new Account(
				"username", "password"));
		prefs.accounts().put(Account.toString(accounts));
		activity.onResume();

		// when
		final int position = 0;
		accountList.getAdapter().getView(position, null, null).performClick();
		final AccountDialog accountDialog = activity.getAccountDialog();
		((EditText) accountDialog.findViewById(R.id.username))
				.setText("username2");
		((EditText) accountDialog.findViewById(R.id.password))
				.setText("password2");
		accountDialog.findViewById(R.id.cancel).performClick();

		// then
		assertThat(accountList.getAdapter().getCount(),
				is(equalTo(accounts.size())));
		assertThat(
				((Account) accountList.getAdapter().getItem(0)).getUsername(),
				is(equalTo("username")));
		assertThat(
				((Account) accountList.getAdapter().getItem(0)).getPassword(),
				is(equalTo("password")));
	}

	@Test
	public void shouldDeleteAccount() {
		// given
		final List<Account> accounts = Collections.singletonList(new Account(
				"username", "password"));
		prefs.accounts().put(Account.toString(accounts));
		activity.onResume();

		// when
		final int position = 0;
		accountList.getAdapter().getView(position, null, null).performClick();
		final AccountDialog accountDialog = activity.getAccountDialog();
		((EditText) accountDialog.findViewById(R.id.username))
				.setText("username2");
		((EditText) accountDialog.findViewById(R.id.password))
				.setText("password2");
		accountDialog.findViewById(R.id.delete).performClick();

		// then
		assertThat(accountList.getAdapter().getCount(),
				is(equalTo(accounts.size() - 1)));
	}
}
