package de.vanmar.android.hoebapp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;

import de.vanmar.android.hoebapp.bo.MediaDetails;
import de.vanmar.android.hoebapp.bo.MediaDetails.Stock;
import de.vanmar.android.hoebapp.service.LibraryService;
import de.vanmar.android.hoebapp.service.LoginFailedException;
import de.vanmar.android.hoebapp.util.NetworkHelper;

@SuppressLint("Registered")
@EActivity(R.layout.detail)
public class DetailActivity extends Activity {

	public static final String EXTRA_MEDIUM_ID = "mediumId";

	private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat(
			"dd.MM.yyyy");

	@Bean
	LibraryService libraryService;

	@Bean
	NetworkHelper networkHelper;

	@ViewById(R.id.image)
	ImageView image;

	@ViewById(R.id.title)
	TextView title;

	@ViewById(R.id.subtitle)
	TextView subtitle;

	@ViewById(R.id.author)
	TextView author;

	@ViewById(R.id.contents)
	TextView contents;

	@ViewById(R.id.stock)
	TextView stockText;

	@ViewById(R.id.stockTitle)
	TextView stockTitle;

	@Override
	protected void onResume() {
		super.onResume();

		final String mediumId = getIntent().getExtras().getString(
				EXTRA_MEDIUM_ID);

		getWindow().setTitle(getString(R.string.detailsLoading));
		loadDetails(mediumId);
	}

	@Background
	void loadDetails(final String mediumId) {
		if (networkHelper.networkAvailable()) {
			MediaDetails details;
			try {
				details = libraryService.getMediaDetails(mediumId);
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
		// setImage(image, details);
		title.setText(details.getTitle());
		subtitle.setText(details.getSubTitle());
		author.setText(details.getAuthor());
		contents.setText(details.getContents());
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
										DISPLAY_DATE_FORMAT.format(returnDate)))
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
}
