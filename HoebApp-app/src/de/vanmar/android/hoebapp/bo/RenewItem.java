package de.vanmar.android.hoebapp.bo;

/**
 * Value object representing a media item to renew for an account
 *
 * @author Kolja
 */
public class RenewItem {

	private final Account account;
	private final String signature;

	public RenewItem(final Account account, final String signature) {
		this.account = account;
		this.signature = signature;
	}

	public Account getAccount() {
		return account;
	}

	public String getSignature() {
		return signature;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RenewItem renewItem = (RenewItem) o;

		if (account != null ? !account.equals(renewItem.account) : renewItem.account != null) return false;
		if (signature != null ? !signature.equals(renewItem.signature) : renewItem.signature != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = account != null ? account.hashCode() : 0;
		result = 31 * result + (signature != null ? signature.hashCode() : 0);
		return result;
	}
}
