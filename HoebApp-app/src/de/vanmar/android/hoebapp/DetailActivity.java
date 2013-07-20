package de.vanmar.android.hoebapp;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

		String mediumId = getIntent().getExtras().getString(EXTRA_MEDIUM_ID);

		getWindow().setTitle(getString(R.string.detailsLoading));
		loadDetails(mediumId);
	}

	@Background
	void loadDetails(String mediumId) {
		if (networkHelper.networkAvailable()) {
			MediaDetails details;
			try {
				details = libraryService.getMediaDetails(mediumId);
				if (details == null
						|| (details.getTitle() == null && details.getAuthor() == null)) {
					finish();
				}
				displayDetails(details);
			} catch (Exception e) {
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
	void displayDetails(MediaDetails details) {
		getWindow().setTitle(details.getTitle());
		setImage(image, details);
		title.setText(details.getTitle());
		subtitle.setText(details.getSubTitle());
		author.setText(details.getAuthor());
		contents.setText(details.getContents());
		stockTitle.setText(getString(R.string.stockTitle));
		displayStock(details.getStock());
	}

	private void displayStock(List<Stock> stock) {
		StringBuilder sb = new StringBuilder();
		for (Stock stockItem : stock) {
			sb.append('\n').append(stockItem.getLocationName()).append('\n');
			int inStock = stockItem.getInStock();
			if (inStock > 0) {
				if (inStock == 1) {
					sb.append(getString(R.string.availableSingle)).append('\n');
				} else {
					sb.append(getString(R.string.available, inStock)).append(
							'\n');
				}
			}
			int outOfStock = stockItem.getOutOfStock().size();
			if (outOfStock > 0) {
				if (outOfStock == 1) {
					sb.append(getString(R.string.unavailableSingle)).append(
							'\n');
				} else {
					sb.append(getString(R.string.unavailable, outOfStock))
							.append('\n');
				}
				for (Date returnDate : stockItem.getOutOfStock()) {
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

	@Background
	void setImage(ImageView imageView, MediaDetails item) {
		Drawable drawable = null;
		if (item.getImage() != null) {
			// Drawable already loaded
			drawable = item.getImage();
		} else if (item.getImgUrl() != null) {
			try {
				final Drawable drawableFromUrl = Drawable.createFromStream(
						((InputStream) new URL(item.getImgUrl()).getContent()),
						getString(R.string.imageDesc));
				item.setImage(drawableFromUrl);
				drawable = drawableFromUrl;
			} catch (Exception e) {
				Log.w("SearchActivity", "Error loading drawable from " + item,
						e);
				item.setImgUrl(null);
			}
		}
		setDrawable(imageView, drawable);
	}

	@UiThread
	void setDrawable(ImageView imageView, Drawable drawable) {
		imageView.setImageDrawable(drawable);
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
