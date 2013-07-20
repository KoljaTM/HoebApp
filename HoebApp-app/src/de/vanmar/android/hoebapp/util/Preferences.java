package de.vanmar.android.hoebapp.util;

import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultInt;
import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultLong;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(SharedPref.Scope.APPLICATION_DEFAULT)
public interface Preferences {

	String accounts();

	@DefaultInt(0)
	int acceptedEULA();

	@DefaultLong(0)
	long lastAccess();

	@DefaultLong(0)
	long notificationSent();

	boolean doAutoUpdate();

	boolean doAutoUpdateWifiOnly();
}
