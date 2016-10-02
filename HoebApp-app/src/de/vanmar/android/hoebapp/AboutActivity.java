package de.vanmar.android.hoebapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import org.androidannotations.annotations.EActivity;

@SuppressLint("Registered")
@EActivity
public class AboutActivity extends Activity {
	WebView abouttext;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about);

		abouttext = (WebView) findViewById(R.id.abouttext);
		abouttext.loadUrl("file:///android_asset/html/about.html");
	}

}
