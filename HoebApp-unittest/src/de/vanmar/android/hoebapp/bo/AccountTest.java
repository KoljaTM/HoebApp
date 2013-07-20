package de.vanmar.android.hoebapp.bo;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.vanmar.android.hoebapp.bo.Account.Appearance;

@RunWith(JUnit4.class)
public class AccountTest {

	@Test
	public void shouldTransformToJson() {
		// given
		final List<Account> accounts = new ArrayList<Account>();
		accounts.add(new Account("username1", "password1"));
		accounts.add(new Account("username2", "password2", Appearance.RED));

		// when
		final String jsonString = Account.toString(accounts);

		// then
		assertThat(
				jsonString,
				is(equalTo("[{\"username\":\"username1\",\"appearance\":\"NONE\",\"password\":\"password1\"},{\"username\":\"username2\",\"appearance\":\"RED\",\"password\":\"password2\"}]")));
	}

	@Test
	public void shouldTransformFromJson() {
		// given
		final String jsonString = "[{\"username\":\"username1\",\"password\":\"password1\"},{\"username\":\"username2\",\"password\":\"password2\",\"appearance\":\"RED\"}]";

		// when
		final List<Account> accounts = Account.fromString(jsonString);

		// then
		assertThat(accounts.size(), is(2));
		assertThat(accounts.get(0).getUsername(), is("username1"));
		assertThat(accounts.get(1).getPassword(), is("password2"));
		assertThat(accounts.get(1).getAppearance(), is(Appearance.RED));
	}

	@Test
	public void shouldReturnEmptyListAsDefault() {
		// given
		final String jsonString = "";

		// when
		final List<Account> accounts = Account.fromString(jsonString);

		// then
		assertThat(accounts.size(), is(0));
	}
}
