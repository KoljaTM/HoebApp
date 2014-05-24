package de.vanmar.android.hoebapp.service;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;
import de.vanmar.android.hoebapp.bo.Account;
import de.vanmar.android.hoebapp.bo.Media;
import de.vanmar.android.hoebapp.bo.MediaDetails;
import de.vanmar.android.hoebapp.db.MediaContentProvider;
import de.vanmar.android.hoebapp.db.MediaDbHelper;
import de.vanmar.android.hoebapp.util.Preferences_;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Kolja on 21.05.2014.
 */
@EBean
public class SoapLibraryService {

	public static final String NOTES_NAMESPACE = "http://bibliomondo.com/websevices/webcatalogue";
	public static final String NOTES_URL = "https://www.buecherhallen.de/app_webnote/Service1.asmx";
	public static final String USER_NAMESPACE = "http://bibliomondo.com/websevices/webuser";
	public static final String USER_URL = "https://www.buecherhallen.de/app_webuser/WebUserSvc.asmx";

	@Pref
	Preferences_ prefs;

	public List<MediaDetails> loadNotepad() throws TechnicalException {
		checkUsernames();
		final List<Account> accounts = Account.fromString(prefs.accounts().get());

		LinkedList<MediaDetails> result = new LinkedList<MediaDetails>();
		for (final Account account : accounts) {
			HashMap<String, String> parameters = new HashMap<String, String>();
			parameters.put("patronId", account.getCheckedUsername());
			parameters.put("patronPin", account.getPassword());
			SoapObject response = doRequest(NOTES_NAMESPACE, "ReadNotesExt", NOTES_URL, parameters);
			SoapObject items = (SoapObject) response.getProperty("items");
			for (int i = 0; i < items.getPropertyCount(); i++) {
				SoapObject item = (SoapObject) items.getProperty(i);
				MediaDetails details = new MediaDetails();
				details.setOwner(account);
				details.setTitle(item.getPrimitivePropertySafelyAsString("title"));
				details.setAuthor(item.getPrimitivePropertySafelyAsString("author"));
				details.setId(item.getPrimitivePropertySafelyAsString("catalogueId"));
				String isbn = item.getPrimitivePropertySafelyAsString("isbn");
				details.setSignature(isbn);
				details.setImgUrl(getImgUrl(isbn));
				details.setType(MaterialType.valueOf(item.getPrimitivePropertySafelyAsString("materialType")));
				result.add(details);
			}
		}
		return result;
	}

	private String getImgUrl(String isbn) {
		return "http://cover.ekz.de/" + isbn.replaceAll(" ", "") + ".jpg";
	}

	public void refreshMedialist(Context context) throws TechnicalException {
		checkUsernames();

		try {
			final ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			operations.add(ContentProviderOperation.newDelete(
					MediaContentProvider.CONTENT_URI).build());
			final List<Account> accounts = Account.fromString(prefs.accounts()
					.get());
			for (final Account account : accounts) {
				HashMap<String, String> parameters = new HashMap<String, String>();
				parameters.put("borrowerNumber", account.getCheckedUsername());
				parameters.put("borrowerPin", account.getPassword());
				SoapObject response = doRequest(USER_NAMESPACE, "GetBorrowerLoans", USER_URL, parameters);
				SoapObject loan = (SoapObject) response.getProperty("..."); // TODO: richtigen Pfad verwenden
				Media media = new Media();
				media.setAuthor(loan.getPrimitivePropertySafelyAsString("Author"));
				media.setTitle(loan.getPrimitivePropertySafelyAsString("Title"));
				media.setType(loan.getPrimitivePropertySafelyAsString("MaterialName"));
				media.setMediumId(loan.getPrimitivePropertySafelyAsString("BacNo"));
				media.setSignature(loan.getPrimitivePropertySafelyAsString("ItemNumber"));
				String isbn = loan.getPrimitivePropertySafelyAsString("ISBN");
				media.setImgUrl(getImgUrl(isbn));
				SimpleDateFormat dateFormat = getDateFormat();
				media.setLoanDate(dateFormat.parse(loan.getPrimitivePropertySafelyAsString("DateIssued")));
				media.setDueDate(dateFormat.parse(loan.getPrimitivePropertySafelyAsString("DateDue")));
				operations.add(updateMediaInDb(media, context, account));
			}
		} catch (Exception e) {
			throw new TechnicalException(e);
		}
	}

	private ContentProviderOperation updateMediaInDb(
			final Media item, final Context context, final Account account) throws RemoteException,
			OperationApplicationException {
		// insert new value
		final ContentValues value = new ContentValues();
		value.put(MediaDbHelper.COLUMN_TITLE, item.getTitle());
		value.put(MediaDbHelper.COLUMN_AUTHOR, item.getAuthor());
		value.put(MediaDbHelper.COLUMN_DUEDATE, item.getDueDate().getTime());
		value.put(MediaDbHelper.COLUMN_LOANDATE, item.getLoanDate()
				.getTime());
		value.put(MediaDbHelper.COLUMN_SIGNATURE, item.getSignature());
		value.put(MediaDbHelper.COLUMN_RENEW_LINK, item.getRenewLink());
		value.put(MediaDbHelper.COLUMN_NO_RENEW_REASON,
				item.getNoRenewReason());
		value.put(MediaDbHelper.COLUMN_NUM_RENEWS, item.getNumRenews());
		value.put(MediaDbHelper.COLUMN_MEDIUM_ID, item.getMediumId());
		value.put(MediaDbHelper.COLUMN_ACCOUNT, account.getUsername());
		return ContentProviderOperation
				.newInsert(MediaContentProvider.CONTENT_URI)
				.withValues(value).build();
	}


	private SoapObject doRequest(String namespace, String action, String url, Map<String, String> parameters) throws TechnicalException {
		SoapObject request = new SoapObject(namespace, action);
		for (Map.Entry<String, String> parameter : parameters.entrySet()) {
			request.addProperty(parameter.getKey(), parameter.getValue());
		}
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);

		HttpTransportSE httpTransport = new HttpTransportSE(url);

		try {
			httpTransport.call(namespace + "/" + action, envelope);
			return (SoapObject) envelope.getResponse();
		} catch (Exception exception) {
			throw new TechnicalException(exception);
		}
	}

	private void checkUsernames() throws TechnicalException {
		final List<Account> accounts = Account.fromString(prefs.accounts().get());
		final List<Account> checkedAccounts = Account.fromString(prefs.accounts().get());
		for (Account account : accounts) {
			if (account.getCheckedUsername() != null) {
				checkedAccounts.add(account);
				continue;
			}
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("borrowerNumber", account.getUsername());
			parameters.put("pin", account.getPassword());
			SoapObject checkBorrowerResult = doRequest(USER_NAMESPACE, "CheckBorrower", USER_URL, parameters);
			if (checkBorrowerResult.hasProperty("record")) {
				// TODO: Ganzen Weg checken
				String checkedUsername = "A57 311 458 5";
				checkedAccounts.add(new Account(account.getUsername(), checkedUsername, account.getPassword(), account.getAppearance()));
			} else {
				throw new LoginFailedException(account.getUsername());
			}
		}
		prefs.accounts().put(Account.toString(checkedAccounts));
	}

	private SimpleDateFormat getDateFormat() {
		return new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);
	}
}
