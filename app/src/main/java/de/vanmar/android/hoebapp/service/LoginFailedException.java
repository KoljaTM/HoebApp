package de.vanmar.android.hoebapp.service;

/**
 * thrown when login was not successful
 *
 * @author Kolja
 */
public class LoginFailedException extends RuntimeException {

	private static final long serialVersionUID = 7345380153836324862L;
	private String username;

	public String getUsername() {
		return username;
	}

	public LoginFailedException(String username) {
		this.username = username;
	}
}
