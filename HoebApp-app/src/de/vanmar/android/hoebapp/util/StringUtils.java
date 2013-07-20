package de.vanmar.android.hoebapp.util;

public final class StringUtils {

	private StringUtils() {
		// prevent instantiation
	}

	public static boolean isEmpty(final CharSequence... strings) {
		for (final CharSequence string : strings) {
			if (string != null && string.length() > 0) {
				return false;
			}
		}
		return true;
	}
}
