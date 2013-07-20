package de.vanmar.android.hoebapp.service;

public class TechnicalException extends Exception {

	private static final long serialVersionUID = 1119080653454935673L;

	TechnicalException() {
		super();
	}

	public TechnicalException(Exception cause) {
		super(cause);
	}

	public TechnicalException(String message) {
		super(message);
	}
}
