package de.vanmar.android.hoebapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.androidquery.AQuery;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import org.androidannotations.annotations.*;
import org.androidannotations.annotations.sharedpreferences.Pref;
import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.bo.MediaDetails;
import de.vanmar.android.hoebapp.bo.MediaDetails.Stock;
import de.vanmar.android.hoebapp.service.LoginFailedException;
import de.vanmar.android.hoebapp.service.SoapLibraryService;
import de.vanmar.android.hoebapp.service.TechnicalException;
import de.vanmar.android.hoebapp.util.NetworkHelper;
import de.vanmar.android.hoebapp.util.Preferences_;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SuppressLint("Registered")
@EActivity(R.layout.detail)
@OptionsMenu(R.menu.mediadetailmenu)
public class DetailActivity extends Activity {

	public static final String EXTRA_MEDIUM_ID = "mediumId";

	private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat(
			"dd.MM.yyyy");

	@Bean
	SoapLibraryService soapLibraryService;

	@Bean
	NetworkHelper networkHelper;

	@Pref
	Preferences_ prefs;

	@ViewById(R.id.image)
	ImageView image;

	@ViewById(R.id.title)
	TextView title;

	@ViewById(R.id.signature)
	TextView signature;

	@ViewById(R.id.author)
	TextView author;

	@ViewById(R.id.stock)
	TextView stockText;

	@ViewById(R.id.stockTitle)
	TextView stockTitle;

	@Override
	protected void onStart() {
		super.onStart();

		loadAds();
	}

	private void loadAds() {
		AdView adView = (AdView) this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
	}

	@Override
	protected void onResume() {
		super.onResume();

		final String mediumId = getMediumId();

		getWindow().setTitle(getString(R.string.detailsLoading));
		loadDetails(mediumId);
	}

	private String getMediumId() {
		return getIntent().getExtras().getString(
				EXTRA_MEDIUM_ID);
	}

	@Background
	void loadDetails(final String mediumId) {
		if (networkHelper.networkAvailable()) {
			MediaDetails details;
			try {
				details = soapLibraryService.getMediaDetails(mediumId);
				if (details == null
						|| (details.getTitle() == null && details.getAuthor() == null)) {
					finish();
				}
				displayDetails(details);
			} catch (final Exception e) {
				displayError(e);
				finish();
			}
		} else {
			Toast.makeText(this, R.string.networkNotAvailable,
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@UiThread
	void displayDetails(final MediaDetails details) {
		getWindow().setTitle(details.getTitle());
		new AQuery(this).id(image).image(details.getImgUrl());
		title.setText(details.getTitle());
		signature.setText(details.getSignature());
		author.setText(details.getAuthor());
		stockTitle.setText(getString(R.string.stockTitle));
		displayStock(details.getStock());
	}

	private void displayStock(final List<Stock> stock) {
		final StringBuilder sb = new StringBuilder();
		for (final Stock stockItem : stock) {
			sb.append('\n').append(stockItem.getLocationName()).append('\n');
			final int inStock = stockItem.getInStock();
			if (inStock > 0) {
				if (inStock == 1) {
					sb.append(getString(R.string.availableSingle)).append('\n');
				} else {
					sb.append(getString(R.string.available, inStock)).append(
							'\n');
				}
			}
			final int outOfStock = stockItem.getOutOfStock().size();
			if (outOfStock > 0) {
				if (outOfStock == 1) {
					sb.append(getString(R.string.unavailableSingle)).append(
							'\n');
				} else {
					sb.append(getString(R.string.unavailable, outOfStock))
							.append('\n');
				}
				for (final Date returnDate : stockItem.getOutOfStock()) {
					if (returnDate != null) {
						sb.append(
								getString(R.string.availableAt,
										DISPLAY_DATE_FORMAT.format(returnDate))
						)
								.append('\n');
					} else {
						sb.append(getString(R.string.unavailableUnknown))
								.append('\n');
					}
				}
			}
		}
		stockText.setText(sb);
	}

	@UiThread
	void displayError(final Exception exception) {
		if (exception instanceof LoginFailedException) {
			Toast.makeText(this, R.string.loginfailed, Toast.LENGTH_SHORT)
					.show();
			Log.w(getClass().getCanonicalName(), "LoginFailedException");
		} else {
			Toast.makeText(this, R.string.technicalError, Toast.LENGTH_SHORT)
					.show();
			Log.e(getClass().getCanonicalName(), "TechnicalException: "
					+ exception.getClass() + exception.getMessage());
		}
	}

	@OptionsItem(R.id.addToNotepad)
	void doAddToNotepad() {
		final List<Account> accounts = Account.fromString(prefs.accounts()
				.get());
		if (accounts.isEmpty()) {
			displayMessage(R.string.usernameNotSet);
			this.startActivity(new Intent(this, PreferencesActivity_.class));
			return;
		}
		// TODO: Choose account
		addToNotepad(accounts.get(0));
	}

	@Background
	void addToNotepad(Account account) {
		try {
			soapLibraryService.addToNotepad(account, getMediumId());
			displayMessage(R.string.addedToNotepad);
		} catch (TechnicalException e) {
			displayError(e);
		}
	}

	@UiThread
	void displayMessage(int resId) {
		Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
	}

}
