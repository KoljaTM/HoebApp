package de.vanmar.android.hoebapp;

import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.dialog.AccountDialog;
import de.vanmar.android.hoebapp.util.Preferences_;

@EActivity(R.layout.preferences)
public class PreferencesActivity extends Activity {

	@ViewById(R.id.accountList)
	ListView accountList;

	@ViewById(R.id.addAccount)
	Button addAccount;

	@ViewById(R.id.doAutoUpdate)
	CheckBox doAutoUpdate;

	@ViewById(R.id.doAutoUpdateWifiOnly)
	CheckBox doAutoUpdateWifiOnly;

	@Pref
	Preferences_ prefs;

	List<Account> accounts;

	private ArrayAdapter<Account> accountAdapter;

	private AccountDialog dialog;

	@Override
	protected void onResume() {
		super.onResume();

		initViewsFromPreferences();
	}

	private void initViewsFromPreferences() {
		accounts = Account.fromString(prefs.accounts().get());
		displayAccountList();

		final boolean autoUpdatePref = prefs.doAutoUpdate().get();
		final boolean autoUpdateWifiOnlyPref = prefs.doAutoUpdateWifiOnly()
				.get();
		doAutoUpdate.setChecked(autoUpdatePref);
		if (autoUpdatePref) {
			doAutoUpdateWifiOnly.setEnabled(true);
			doAutoUpdateWifiOnly.setChecked(autoUpdateWifiOnlyPref);
		} else {
			doAutoUpdateWifiOnly.setEnabled(false);
			doAutoUpdateWifiOnly.setChecked(false);
		}
	}

	private void displayAccountList() {
		accountAdapter.clear();
		for (final Account account : accounts) {
			accountAdapter.add(account);
		}
	}

	private void updateAccounts() {
		prefs.accounts().put(Account.toString(accounts));
		displayAccountList();
	}

	@AfterViews
	public void afterViews() {
		accountAdapter = new ArrayAdapter<Account>(this,
				R.layout.accountlist_item) {
			@Override
			public View getView(final int position, final View convertView,
					final ViewGroup parent) {
				final View view = getLayoutInflater().inflate(
						R.layout.accountlist_item, null);
				final Account account = getItem(position);
				((TextView) view.findViewById(R.id.username)).setText(account
						.getUsername());
				view.findViewById(R.id.item).setBackgroundResource(
						account.getAppearance().getDrawable());
				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(final View v) {
						dialog = new AccountDialog(PreferencesActivity.this,
								account);

						dialog.setOnDismissListener(new OnDismissListener() {

							@Override
							public void onDismiss(
									final DialogInterface paramDialogInterface) {
								final Account editedAccount = dialog
										.getAccount();
								if (editedAccount == null) {
									accounts.remove(account);
								} else {
									accounts.set(position, editedAccount);
								}
								updateAccounts();
							}
						});
						dialog.show();
					}
				});

				return view;
			}

		};
		accountList.setAdapter(accountAdapter);
	}

	@Click(R.id.addAccount)
	public void addAccount() {
		dialog = new AccountDialog(this, null);

		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(final DialogInterface paramDialogInterface) {
				final Account account = dialog.getAccount();
				if (account != null) {
					accounts.add(account);
				}
				updateAccounts();
			}
		});
		dialog.show();
	}

	@Click(R.id.doAutoUpdate)
	public void doAutoUpdateSelected() {
		final boolean autoUpdateChecked = doAutoUpdate.isChecked();
		prefs.doAutoUpdate().put(autoUpdateChecked);
		doAutoUpdateWifiOnly.setEnabled(autoUpdateChecked);
		if (!autoUpdateChecked) {
			doAutoUpdateWifiOnly.setChecked(false);
			prefs.doAutoUpdateWifiOnly().put(false);
		}
	}

	@Click(R.id.doAutoUpdateWifiOnly)
	public void doAutoUpdateWifiOnlySelected() {
		prefs.doAutoUpdateWifiOnly().put(doAutoUpdateWifiOnly.isChecked());
	}

	protected AccountDialog getAccountDialog() {
		return dialog;
	}
}
