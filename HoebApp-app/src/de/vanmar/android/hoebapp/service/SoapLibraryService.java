package de.vanmar.android.hoebapp.service;

import android.appwidget.AppWidgetManager;
import android.content.*;
import android.os.RemoteException;
import android.util.Log;
import de.vanmar.android.hoebapp.HoebAppWidgetProvider_;
import de.vanmar.android.hoebapp.R;
import de.vanmar.android.hoebapp.UpdateService_;
import de.vanmar.android.hoebapp.bo.*;
import de.vanmar.android.hoebapp.db.MediaContentProvider;
import de.vanmar.android.hoebapp.db.MediaDbHelper;
import de.vanmar.android.hoebapp.util.Preferences_;
import de.vanmar.android.hoebapp.util.StringUtils;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Kolja on 21.05.2014.
 */
@EBean
public class SoapLibraryService {

	public static String NOTES_URL = "https://zones.buecherhallen.de/app_webnote/Service1.asmx";
	public static String NOTE_URL = "https://zones.buecherhallen.de/app_ZonesServices/Service1.asmx";
	public static String USER_URL = "https://zones.buecherhallen.de/app_webuser/WebUserSvc.asmx";
	public static String CATALOG_URL = "https://zones.buecherhallen.de/WebCat/WebCatalogueSvc.asmx";
	public static final String NOTES_NAMESPACE = "http://bibliomondo.com/websevices/webcatalogue";
	public static final String NOTE_NAMESPACE = "http://bibliomondo.com/ZoneServices/";
	public static final String USER_NAMESPACE = "http://bibliomondo.com/websevices/webuser";
	public static final String CATALOG_NAMESPACE = "http://bibliomondo.com/websevices/webcatalogue";
	public static final String CATEGORY_KEYWORD = "text_auto:";

	@Pref
	Preferences_ prefs;

	@Bean
	SoapHelper soapHelper;

	public List<MediaDetails> loadNotepad() throws TechnicalException {
		checkUsernames();
		final List<Account> accounts = Account.fromString(prefs.accounts().get());

		LinkedList<MediaDetails> result = new LinkedList<MediaDetails>();
		for (final Account account : accounts) {
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("patronId", account.getCheckedUsername());
			parameters.put("patronPin", account.getPassword());
			SoapObject response = doRequest(NOTES_NAMESPACE, "ReadNotesExt", NOTES_URL, parameters, SoapEnvelope.VER11);
			SoapObject items = (SoapObject) response.getProperty("items");
			for (int i = 0; i < items.getPropertyCount(); i++) {
				SoapObject item = (SoapObject) items.getProperty(i);
				MediaDetails details = new MediaDetails();
				details.setOwner(account);
				details.setTitle(soapHelper.getString(item, "title"));
				details.setAuthor(soapHelper.getString(item, "author"));
				details.setId(soapHelper.getString(item, "catalogueId"));
				String isbn = soapHelper.getString(item, "isbn");
				details.setSignature(isbn);
				details.setImgUrl(getImgUrl(isbn));
				details.setType(MaterialType.valueOf(soapHelper.getString(item, "materialType")));
				result.add(details);
			}
		}
		return result;
	}

	public String getImgUrl(String isbn) {
		String isbn13 = getIsbn13(isbn);
		return "http://cover.ekz.de/" + isbn13 + ".jpg";
	}

	public String getIsbn13(String isbn) {
		StringBuilder isbnAsNumber = new StringBuilder(isbn.replaceAll(" ", "").replaceAll("-", ""));
		if (isbnAsNumber.length() == 13) {
			return isbnAsNumber.toString();
		} else if (isbnAsNumber.length() == 10) {
			isbnAsNumber.insert(0, "978").deleteCharAt(isbnAsNumber.length() - 1);
		} else {
			return null;
		}

		int sum = 0;
		for (int digit = 0; digit < isbnAsNumber.length(); digit++) {
			int multiplier = ((digit % 2) == 0) ? 1 : 3;
			sum += Integer.parseInt(isbnAsNumber.substring(digit, digit + 1)) * multiplier;
		}
		isbnAsNumber.append((1000 - sum) % 10);

		return isbnAsNumber.toString();
	}

	public void refreshMediaList(Context context) throws TechnicalException {
		checkUsernames();

		try {
			final ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			operations.add(ContentProviderOperation.newDelete(
					MediaContentProvider.CONTENT_URI).build());
			final List<Account> accounts = Account.fromString(prefs.accounts()
					.get());
			for (final Account account : accounts) {
				String sessionId = getSessionId(account);

				HashMap<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("sessionId", sessionId);
				SoapObject response = doRequest(USER_NAMESPACE, "GetBorrowerLoans", USER_URL, parameters, SoapEnvelope.VER11);
				List<SoapObject> loans = soapHelper.getLoans(response);
				for (SoapObject loan : loans) {
					Media media = new Media();
					media.setTitle(soapHelper.getStringFromHtml(loan, "Title"));
					media.setAuthor(soapHelper.getStringFromHtml(loan, "Author"));
					SimpleDateFormat dateFormat = getDateFormat();
					media.setDueDate(dateFormat.parse(soapHelper.getString(loan, "DateDue")));
					media.setLoanDate(dateFormat.parse(soapHelper.getString(loan, "DateIssued")));
					media.setSignature(soapHelper.getString(loan, "ItemNumber"));
					String canRenew = soapHelper.getString(soapHelper.get(loan, "SIP2"), "CanRenew");
					media.setCanRenew("1".equals(canRenew));
					media.setNoRenewReason(soapHelper.getStringFromHtml(soapHelper.get(loan, "PrimaryTrapIdText"), "GER"));
					String renewalCount = soapHelper.getString(loan, "RenewalCount");
					if (!StringUtils.isEmpty(renewalCount)) {
						media.setNumRenews(Integer.parseInt(renewalCount));
					}
					media.setMediumId(soapHelper.getString(loan, "BacNo"));
					media.setType(soapHelper.getStringFromHtml(loan, "MaterialName"));
					String isbn = soapHelper.getString(loan, "ISBN");
					media.setImgUrl(getImgUrl(isbn));
					operations.add(updateMediaInDb(media, account));
				}
			}
			// execute content operations in batch
			context.getContentResolver()
					.applyBatch(
							MediaContentProvider.CONTENT_URI.getAuthority(),
							operations);

			// notify Widget of changes
			notifyWidget(context);

			updateLastAccessDate();
			updateNotifications(context);

		} catch (Exception e) {
			throw new TechnicalException(e);
		}
	}

	private String getSessionId(Account account) throws TechnicalException {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("borrowerNumber", account.getCheckedUsername());
		parameters.put("pin", account.getPassword());
		SoapObject checkBorrowerResult = doRequest(USER_NAMESPACE, "CheckBorrower", USER_URL, parameters, SoapEnvelope.VER12);
		return soapHelper.getSessionId(checkBorrowerResult);
	}

	private ContentProviderOperation updateMediaInDb(
			final Media item, final Account account) throws RemoteException,
			OperationApplicationException {
		// insert new value
		final ContentValues value = new ContentValues();
		value.put(MediaDbHelper.COLUMN_TITLE, item.getTitle());
		value.put(MediaDbHelper.COLUMN_AUTHOR, item.getAuthor());
		value.put(MediaDbHelper.COLUMN_DUEDATE, item.getDueDate().getTime());
		value.put(MediaDbHelper.COLUMN_LOANDATE, item.getLoanDate()
				.getTime());
		value.put(MediaDbHelper.COLUMN_SIGNATURE, item.getSignature());
		value.put(MediaDbHelper.COLUMN_CAN_RENEW, item.isCanRenew() ? 1 : 0);
		value.put(MediaDbHelper.COLUMN_NO_RENEW_REASON,
				item.getNoRenewReason());
		value.put(MediaDbHelper.COLUMN_NUM_RENEWS, item.getNumRenews());
		value.put(MediaDbHelper.COLUMN_MEDIUM_ID, item.getMediumId());
		value.put(MediaDbHelper.COLUMN_TYPE, item.getType());
		value.put(MediaDbHelper.COLUMN_IMG_URL, item.getImgUrl());
		value.put(MediaDbHelper.COLUMN_ACCOUNT, account.getUsername());
		return ContentProviderOperation
				.newInsert(MediaContentProvider.CONTENT_URI)
				.withValues(value).build();
	}

	private void notifyWidget(final Context context) {
		final Intent widgetIntent = new Intent(context,
				HoebAppWidgetProvider_.class);
		widgetIntent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
		widgetIntent.putExtra(
				AppWidgetManager.EXTRA_APPWIDGET_IDS,
				AppWidgetManager.getInstance(context)
						.getAppWidgetIds(
								new ComponentName(context,
										HoebAppWidgetProvider_.class)
						)
		);
		context.sendBroadcast(widgetIntent);
	}

	private void updateLastAccessDate() {
		prefs.lastAccess().put(new Date().getTime());
		prefs.notificationSent().put(0L);
	}

	private void updateNotifications(final Context context) {
		context.sendBroadcast(new Intent(context, UpdateService_.class));
	}

	private SoapPrimitive doRequestForPrimitive(String namespace, String action, String url, HashMap<String, Object> parameters, int soapEnvelopeVersion) throws TechnicalException {
		return doRequestInternal(namespace, action, url, parameters, SoapPrimitive.class, soapEnvelopeVersion);
	}

	private SoapObject doRequest(String namespace, String action, String url, HashMap<String, Object> parameters, int soapEnvelopeVersion) throws TechnicalException {
		return doRequestInternal(namespace, action, url, parameters, SoapObject.class, soapEnvelopeVersion);
	}

	private <TYPE> TYPE doRequestInternal(String namespace, String action, String url, HashMap<String, Object> parameters, Class<TYPE> clazz, int soapEnvelopeVersion) throws TechnicalException {
		SoapObject request = new SoapObject(namespace, action);
		for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
			request.addProperty(parameter.getKey(), parameter.getValue());
		}
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(soapEnvelopeVersion);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);

		HttpTransportSE httpTransport = new HttpTransportSE(url);

		try {
			if (!namespace.endsWith("/")) {
				namespace += "/";
			}
			httpTransport.call(namespace + action, envelope);
			return (TYPE) envelope.getResponse();
		} catch (Exception exception) {
			throw new TechnicalException(exception);
		}
	}

	private void checkUsernames() throws TechnicalException {
		final List<Account> accounts = Account.fromString(prefs.accounts().get());
		final List<Account> checkedAccounts = new LinkedList<Account>();
		for (Account account : accounts) {
			if (!StringUtils.isEmpty(account.getCheckedUsername())) {
				checkedAccounts.add(account);
				continue;
			}
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("borrowerNumber", account.getUsername());
			parameters.put("pin", account.getPassword());
			SoapObject checkBorrowerResult = doRequest(USER_NAMESPACE, "CheckBorrower", USER_URL, parameters, SoapEnvelope.VER12);
			String checkedUsername = soapHelper.getCheckedUsername(checkBorrowerResult);
			if (!StringUtils.isEmpty(checkedUsername)) {
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

	public void renewMedia(Set<RenewItem> renewList, Context context) throws TechnicalException {
		for (RenewItem item : renewList) {
			String sessionId = getSessionId(item.getAccount());

			HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("sessionId", sessionId);
			parameters.put("itemNumber", item.getSignature());
			doRequest(USER_NAMESPACE, "RenewItem", USER_URL, parameters, SoapEnvelope.VER11);
		}
		refreshMediaList(context);
	}

	public List<SearchMedia> searchMedia(final Context context,
										 final String text1, final String category1, final String text2,
										 final String category2, final String text3, final String category3, int offset, int pageSize) {
		LinkedList<SearchMedia> resultList = new LinkedList<SearchMedia>();
		try {
			StringBuilder query = new StringBuilder();
			addToQuery(query, text1, category1);
			addToQuery(query, text2, category2);
			addToQuery(query, text3, category3);
			String searchUrl = String.format(context.getString(R.string.searchUrl), URLEncoder.encode(query.toString(), "UTF-8"), offset, pageSize);
			Log.i("Search", searchUrl);
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			HttpResponse response = new DefaultHttpClient().execute(new HttpGet(searchUrl));
			Document document = documentBuilderFactory.newDocumentBuilder().parse(response.getEntity().getContent());
			NodeList results = (NodeList) XPathFactory.newInstance().newXPath().compile("//response/result/doc").evaluate(document, XPathConstants.NODESET);

			XPathExpression xpathTitle = XPathFactory.newInstance().newXPath().compile("arr[@name=\"Title\"]");
			XPathExpression xpathAuthor = XPathFactory.newInstance().newXPath().compile("arr[@name=\"Author\"]/str[1]");
			XPathExpression xpathISBN = XPathFactory.newInstance().newXPath().compile("arr[@name=\"ISBN\"]/str");
			XPathExpression xpathId = XPathFactory.newInstance().newXPath().compile("str[@name=\"id\"]");
			XPathExpression xpathType = XPathFactory.newInstance().newXPath().compile("arr[@name=\"MaterialType\"]/str[2]");

			for (int i = 0; i < results.getLength(); i++) {
				Node item = results.item(i);
				String title = xpathTitle.evaluate(item);
				String author = xpathAuthor.evaluate(item);
				String isbn = xpathISBN.evaluate(item);
				if (isbn.contains(" ")) {
					isbn = isbn.substring(0, isbn.indexOf(" "));
				}
				String id = xpathId.evaluate(item);
				String type = xpathType.evaluate(item);
				if (!StringUtils.isEmpty(id) && !StringUtils.isEmpty(title)) {
					SearchMedia searchMedia = new SearchMedia();
					searchMedia.setAuthor(author);
					searchMedia.setTitle(title);
					searchMedia.setId(id);
					searchMedia.setImgUrl(getImgUrl(isbn));
					searchMedia.setType(type);
					resultList.add(searchMedia);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultList;
	}

	private void addToQuery(StringBuilder query, String text, String category) {
		if (!StringUtils.isEmpty(text) && !StringUtils.isEmpty(category)) {
			if (query.length() != 0) {
				query.append(" AND ");
			}
			if (CATEGORY_KEYWORD.equals(category)) {
				category = ""; // keyword search is just the plain text as query
			}
			query.append(category).append('"').append(text).append('"');
		}
	}

	public MediaDetails getMediaDetails(String mediumId) throws TechnicalException {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("CatalogueNumber", mediumId);
		SoapObject response = doRequest(CATALOG_NAMESPACE, "GetCatalogueItems", CATALOG_URL, parameters, SoapEnvelope.VER11);
		MediaDetails mediaDetails = new MediaDetails();
		SoapObject item = soapHelper.get(soapHelper.get(soapHelper.get(soapHelper.get(response, "xmlDoc"), "GetCatalogueItemsResult"), "SoapActionResult"), "Items");

		mediaDetails.setAuthor(soapHelper.getString(item, "Author"));
		mediaDetails.setTitle(soapHelper.getString(item, "Title"));
		mediaDetails.setSignature(soapHelper.getString(item, "ClassMark"));
		mediaDetails.setType(MaterialType.valueOf(soapHelper.getString(item, "MaterialType")));
		String isbn = soapHelper.getString(item, "ISBN");
		mediaDetails.setImgUrl(getImgUrl(isbn));

		SortedMap<Location, MediaDetails.Stock> stockByLocation = new TreeMap<Location, MediaDetails.Stock>();

		for (SoapObject stockItem : soapHelper.getList(item)) {
			String currentStatus = soapHelper.getString(stockItem, "CurrentStatus");
			if (!StringUtils.isEmpty(currentStatus)) {
				String owner = soapHelper.getString(stockItem, "Owner");
				Location location = Location.get(owner);
				MediaDetails.Stock stock = stockByLocation.get(location);
				if (stock == null) {
					stock = new MediaDetails.Stock();
					stock.setLocationCode(location.getCode());
					stock.setLocationName(location.getName());
					stockByLocation.put(location, stock);
				}
				if ("0".equals(currentStatus)) {
					stock.setInStock(stock.getInStock() + 1);
				} else {
					String statusChangeDate = soapHelper.getString(stockItem, "StatusChangeDate");
					if (StringUtils.isEmpty(statusChangeDate)) {
						stock.getOutOfStock().add(null);
					} else {
						try {
							stock.getOutOfStock().add(getDateFormat().parse(statusChangeDate));
						} catch (ParseException e) {
							stock.getOutOfStock().add(null);
						}
					}
				}
			}
		}
		for (MediaDetails.Stock stock : stockByLocation.values()) {
			mediaDetails.getStock().add(stock);
		}
		return mediaDetails;
	}

	public void addToNotepad(Account account, String mediumId) throws TechnicalException {
		// checkUsernames(); TODO: richtigen Account bestimmen
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("patronid", account.getCheckedUsername());
		parameters.put("patronpin", account.getPassword());
		parameters.put("padName", "-");
		mediumId = trim(mediumId);
		parameters.put("catalogueId", mediumId);
		SoapObject details = new SoapObject("", "");
		details.addProperty("r", "");
		parameters.put("details", details);
		SoapObject response = doRequest(NOTE_NAMESPACE, "NoteRecord", NOTE_URL, parameters, SoapEnvelope.VER11);

		Log.i("Response:", response.toString());
	}

	private String trim(String mediumId) {
		if (mediumId == null)
			return null;
		if (mediumId.startsWith("T")) {
			mediumId = mediumId.substring(1);
		}
		while (mediumId.startsWith("0")) {
			mediumId = mediumId.substring(1);
		}
		mediumId = mediumId.substring(0, mediumId.length() - 1);
		return mediumId;
	}

	public void removeFromNotepad(Account account, String mediumId) throws TechnicalException {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("patronid", account.getCheckedUsername());
		parameters.put("patronpin", account.getPassword());
		parameters.put("padName", "-");
		parameters.put("catalogueId", mediumId);
		SoapObject details = new SoapObject("", "");
		details.addProperty("r", "");
		parameters.put("details", details);
		SoapPrimitive response = doRequestForPrimitive(NOTE_NAMESPACE, "DeleteNote", NOTE_URL, parameters, SoapEnvelope.VER11);

		Log.i("Response:", response.toString());
	}
}