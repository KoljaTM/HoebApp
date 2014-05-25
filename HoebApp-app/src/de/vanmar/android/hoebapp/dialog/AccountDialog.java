package de.vanmar.android.hoebapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import de.vanmar.android.hoebapp.R;
import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.bo.Account.Appearance;
import de.vanmar.android.hoebapp.util.StringUtils;

public class AccountDialog extends Dialog {

	private static final Appearance[] allAppearances = Appearance.values();

	private Account account;
	private Appearance selectedAppearance;
	private final View dialog;
	private final TextView username;
	private final TextView password;
	private final Button appearanceButton;
	private final Button saveButton;
	private final Button deleteButton;
	private final Button cancelButton;

	public AccountDialog(final Context context, final Account account) {
		super(context, R.style.HoebAppDialogTheme);
		this.account = account;
		this.selectedAppearance = account == null ? Appearance.NONE : account
				.getAppearance();

		setContentView(R.layout.accountdialog);
		dialog = findViewById(R.id.dialog);
		username = (TextView) findViewById(R.id.username);
		password = (TextView) findViewById(R.id.password);
		appearanceButton = (Button) findViewById(R.id.appearance);
		saveButton = (Button) findViewById(R.id.save);
		deleteButton = (Button) findViewById(R.id.delete);
		cancelButton = (Button) findViewById(R.id.cancel);

		initFromAccount(account);
		initAppearanceButton();
		initSaveButton();
		initDeleteButton();
		initCancelButton();
	}

	private void initAppearanceButton() {
		appearanceButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View paramView) {
				selectedAppearance = allAppearances[(selectedAppearance
						.ordinal() + 1) % allAppearances.length];
				dialog.setBackgroundResource(selectedAppearance.getDrawable());
			}
		});
	}

	private void initCancelButton() {
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View paramView) {
				AccountDialog.this.cancel();
			}
		});
	}

	private void initDeleteButton() {
		deleteButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View paramView) {
				account = null;
				AccountDialog.this.dismiss();
			}
		});
	}

	private void initSaveButton() {
		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View paramView) {
				if (StringUtils.isEmpty(username.getText())) {
					makeToast(R.string.username_empty_warning);
					username.requestFocus();
				} else if (StringUtils.isEmpty(password.getText())) {
					makeToast(R.string.password_empty_warning);
					password.requestFocus();
				} else {
					account = new Account(username.getText().toString(), null,
							password.getText().toString(), selectedAppearance);
					AccountDialog.this.dismiss();
				}
			}
		});
	}

	protected void makeToast(final int resourceId) {
		Toast.makeText(getContext(), resourceId, Toast.LENGTH_SHORT).show();
	}

	private void initFromAccount(final Account account) {
		if (account == null) {
			setTitle(R.string.newAccount);
			deleteButton.setVisibility(View.GONE);
			dialog.setBackgroundResource(Appearance.NONE.getDrawable());
		} else {
			setTitle(R.string.editAccount);
			username.setText(account.getUsername());
			password.setText(account.getPassword());
			if (account.getAppearance() != null) {
				dialog.setBackgroundResource(account.getAppearance()
						.getDrawable());
			}
		}
	}

	public Account getAccount() {
		return account;
	}
}
