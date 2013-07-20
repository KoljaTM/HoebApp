package de.vanmar.android.hoebapp.bo;

/**
 * Value object representing a media item to renew for an account
 * 
 * @author Kolja
 * 
 */
public class RenewItem {

	private final String username;
	private final String signature;

	public RenewItem(final String username, final String signature) {
		this.username = username;
		this.signature = signature;
	}

	public String getUsername() {
		return username;
	}

	public String getSignature() {
		return signature;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((signature == null) ? 0 : signature.hashCode());
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
		final RenewItem other = (RenewItem) obj;
		if (signature == null) {
			if (other.signature != null) {
				return false;
			}
		} else if (!signature.equals(other.signature)) {
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

}
