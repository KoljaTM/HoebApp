package de.vanmar.android.hoebapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.googlecode.androidannotations.annotations.EActivity;

@SuppressLint("Registered")
@EActivity
public class HelpActivity extends Activity {

	WebView helptext;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.help);

		helptext = (WebView) findViewById(R.id.helptext);
		helptext.loadUrl("file:///android_asset/html/help.html");
	}

}
