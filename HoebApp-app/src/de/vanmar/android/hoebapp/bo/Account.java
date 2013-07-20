package de.vanmar.android.hoebapp.bo;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import de.vanmar.android.hoebapp.R;
import de.vanmar.android.hoebapp.util.StringUtils;

/**
 * Value object representing a library account
 * 
 * @author Kolja
 * 
 */
public class Account {

	public enum Appearance {
		NONE("NONE", R.drawable.bg_none), //
		BLUE("BLUE", R.drawable.bg_blue), //
		RED("RED", R.drawable.bg_red), //
		GREEN("GREEN", R.drawable.bg_green), //
		PURPLE("PURPLE", R.drawable.bg_purple), //
		YELLOW("YELLOW", R.drawable.bg_yellow), //
		SILVER("SILVER", R.drawable.bg_silver); //

		private Appearance(final String name, final int drawable) {
			this.name = name;
			this.drawable = drawable;
		}

		public int getDrawable() {
			return drawable;
		};

		public String getName() {
			return name;
		}

		private String name;
		private int drawable;
	}

	private final String username;
	private final String password;
	private Appearance appearance = Appearance.NONE;

	public Account(final String username, final String password,
			final Appearance appearance) {
		this.username = username;
		this.password = password;
		this.appearance = appearance;
	}

	public Account(final String username, final String password) {
		this.username = username;
		this.password = password;
	}

	public static List<Account> fromString(final String jsonString) {
		final List<Account> accountsFromJSON = new LinkedList<Account>();
		if (!StringUtils.isEmpty(jsonString)) {
			try {
				final JSONArray jsonArray = new JSONArray(jsonString);
				for (int i = 0; i < jsonArray.length(); i++) {
					final JSONObject json = jsonArray.getJSONObject(i);
					final String username = json.getString("username");
					final String password = json.getString("password");
					final Appearance appearance;
					if (json.has("appearance")) {
						appearance = Appearance.valueOf(Appearance.class,
								json.getString("appearance"));
					} else {
						appearance = Appearance.NONE;
					}
					final Account account = new Account(username, password,
							appearance);
					accountsFromJSON.add(account);
				}
			} catch (final JSONException e) {
				Log.w("Account", "error while converting accounts from JSON", e);
			}
		}
		return accountsFromJSON;
	}

	public static String toString(final List<Account> accounts) {
		final JSONArray jsonAccounts = new JSONArray();
		if (accounts != null) {
			for (final Account account : accounts) {
				try {
					final JSONObject json = new JSONObject();
					json.put("username", account.getUsername());
					json.put("password", account.getPassword());
					json.put("appearance", account.getAppearance());
					jsonAccounts.put(json);
				} catch (final JSONException e) {
					Log.w("Account", "error while converting account to JSON",
							e);
				}
			}
		}
		return jsonAccounts.toString();
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public Appearance getAppearance() {
		return appearance != null ? appearance : Appearance.NONE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((appearance == null) ? 0 : appearance.hashCode());
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Account other = (Account) obj;
		if (appearance != other.appearance) {
			return false;
		}
		if (password == null) {
			if (other.password != null) {
				return false;
			}
		} else if (!password.equals(other.password)) {
			return false;
		}
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!username.equals(other.username)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Account [username=" + username + ", password=" + password
				+ ", appearance=" + appearance + "]";
	}
}
