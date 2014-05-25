package de.vanmar.android.hoebapp.service;

import android.appwidget.AppWidgetManager;
import android.content.*;
import android.os.Binder;
import android.os.RemoteException;
import android.text.Html;
import android.util.Log;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;
import de.vanmar.android.hoebapp.HoebAppWidgetProvider_;
import de.vanmar.android.hoebapp.UpdateService_;
import de.vanmar.android.hoebapp.bo.*;
import de.vanmar.android.hoebapp.bo.MediaDetails.Stock;
import de.vanmar.android.hoebapp.db.MediaContentProvider;
import de.vanmar.android.hoebapp.db.MediaDbHelper;
import de.vanmar.android.hoebapp.util.HttpCallBuilder;
import de.vanmar.android.hoebapp.util.HttpCallBuilder.Method;
import de.vanmar.android.hoebapp.util.Preferences_;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EBean
public class LibraryService {
	public static final String CATEGORY_KEYWORD = "kw=";
	private static final String LOGIN_FORM_URL = "https://www.buecherhallen.de/alswww2.dll/APS_ZONES?fn=Login&Style=Portal3&SubStyle=&Lang=GER&ResponseEncoding=utf-8";
	private static final Pattern REGEX_LOGIN_FORM = Pattern.compile(
			"<form[^>]*action=\"(Obj_\\d+)\" name=\"LoginForm\"",
			Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
	private static final Pattern REGEX_LOGIN = Pattern.compile(
			"Ihre Kundendaten", Pattern.MULTILINE);
	private static final Pattern REGEX_WRONG_LOGIN = Pattern.compile(
			".*Die eingegebene Nummer Ihrer Kundenkarte ist nicht korrekt.*",
			Pattern.MULTILINE | Pattern.DOTALL);
	private static final Pattern REGEX_WRONG_PASSWORD = Pattern.compile(
			".*Die eingegebene Geheimnummer ist falsch.*", Pattern.MULTILINE
			| Pattern.DOTALL);

	private static final String MEDIALIST_URL = "https://www.buecherhallen.de/alswww2.dll/APS_ZONES?fn=MyLoans&Style=Portal3&SubStyle=&Lang=GER&ResponseEncoding=utf-8";
	private static final String NOTEPAD_PREFETCH_URL = "https://www.buecherhallen.de/alswww2.dll/APS_ZONES?fn=ViewNotepad&Style=Portal3&SubStyle=&Lang=GER&ResponseEncoding=utf-8";
	private static final String NOTEPAD_POSTFETCH_URL = "https://www.buecherhallen.de/alswww2.dll/APS_INTEREST?Method=ShowNotes&Style=Portal3&SubStyle=&Lang=GER&ResponseEncoding=utf-8&pad=-";
	private static final String SERVICE_URL = "https://www.buecherhallen.de/alswww2.dll/";
	private static final String DETAIL_URL = "https://www.buecherhallen.de/alswww2.dll/APS_PRESENT_BIB";
	private static final String ADD_TO_NOTEPAD_URL = "https://www.buecherhallen.de/alswww2.dll/APS_NOTES2?Style=Portal3&Method=NoteItem&id=";
	private static final String REMOVE_FROM_NOTEPAD_URL = "https://www.buecherhallen.de/alswww2.dll/APS_NOTES2?Style=Portal3&Method=DeleteNote&id=";
	private static final int MAX_RETRIES = 10;

	private final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd/MM/yyyy", Locale.GERMAN);
	private static final Pattern REGEX_REQUEST_OBJECT = Pattern.compile(
			"<META NAME=\"ZonesObjName\"\\s*CONTENT=\"([^\"]*)\">",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern REGEX_ZONES_TEMPLATE = Pattern.compile(
			"<META NAME=\"ZonesTemplate\"\\s*CONTENT=\"([^\"]*)\">",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern REGEX_PAGE_DOWN = Pattern.compile(
			"<a[^>]*Method=PageDown",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
	private static final Pattern REGEX_MEDIA_ITEM = Pattern
			.compile(
					"TEMPLATE PORTAL2 BROWSEENUM.HTML LoanHeading.*?<TABLE(.*?)</TABLE>",
					Pattern.MULTILINE | Pattern.DOTALL
							| Pattern.CASE_INSENSITIVE);

	// $1= MediumId, $2=Title
	private static final Pattern REGEX_MEDIA_DETAILS_MEDIUM_ID_TITLE = Pattern.compile(
			"<td class=\"LoanBrowseFieldDataCell\">\\s*<a[^>]*BACNO=([^&>]*)[^>]*>\\s*([^<]*?)\\s*</a>",
			Pattern.MULTILINE | Pattern.DOTALL);
	private static final Pattern REGEX_MEDIA_DETAILS_AUTHOR = Pattern
			.compile(
					"LoanBrowseFieldNameCell\"><i>Autor</i></td>\\s*<td class=\"LoanBrowseFieldDataCell\">\\s*([^>]*)\\s*</td>",
					Pattern.MULTILINE | Pattern.DOTALL);
	private static final Pattern REGEX_MEDIA_DETAILS_SIGNATURE = Pattern
			.compile(
					"LoanBrowseFieldNameCell\"><i>Mediennummer</i></td>\\s*<td class=\"LoanBrowseFieldDataCell\">\\s*([^>]*)\\s*</td>",
					Pattern.MULTILINE | Pattern.DOTALL);
	private static final Pattern REGEX_MEDIA_DETAILS_DUEDATE = Pattern
			.compile(
					"LoanBrowseFieldNameCell\"><i>f.llig</i></td>\\s*<td class=\"LoanBrowseFieldDataCell\"[^>]*>\\s*<div class=\"LoanDate\">\\s*<b>([^>]*)</b>",
					Pattern.MULTILINE | Pattern.DOTALL);
	private static final Pattern REGEX_MEDIA_DETAILS_LOANDATE = Pattern
			.compile(
					"LoanBrowseFieldNameCell\"><i>ausgeliehen</i></td>\\s*<td class=\"LoanBrowseFieldDataCell\">\\s*([^>]*)\\s*</td>",
					Pattern.MULTILINE | Pattern.DOTALL);
	private static final Pattern REGEX_MEDIA_DETAILS_RENEW = Pattern.compile(
			"javascript:renewItem\\('\\d*','([^']*Method=Renew[^']*)'\\)",
			Pattern.MULTILINE | Pattern.DOTALL);
	private static final Pattern REGEX_MEDIA_DETAILS_NO_RENEW_REASON = Pattern
			.compile("javascript:CannotRenewLoan\\('\\d*','([^']*)'",
					Pattern.MULTILINE | Pattern.DOTALL);
	private static final Pattern REGEX_MEDIA_DETAILS_NUM_RENEWS = Pattern
			.compile(
					"LoanBrowseFieldNameCell\"><font face=\"Arial, Helvetica, sans-serif\"></i>Anz. Verl.ng.</td>\\s*<td class=\"LoanBrowseFieldDataCell\">\\s*(\\d*)\\s*</td>",
					Pattern.MULTILINE | Pattern.DOTALL);

	private static final Pattern REGEX_SEARCHRESULT_ITEM = Pattern.compile(
			"\"SummaryImageCell(.*?)Merkliste", Pattern.MULTILINE
			| Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

	private static final Pattern REGEX_SEARCHRESULT_TITLE = Pattern.compile(
			"SummaryFieldLink\"\\s*title=\"([^\"]*)  .ffnen\"",
			Pattern.MULTILINE | Pattern.DOTALL);

	private static final Pattern REGEX_SEARCHRESULT_AUTHOR = Pattern
			.compile(
					"<td class=\"SummaryFieldLegend\" nowrap=\"nowrap\" >Verfasser</td>\\s*<td class=\"SummaryFieldLegendSep\"> : </td>\\s*<td class=\"SummaryFieldData\">([^<]*)</td>",
					Pattern.MULTILINE | Pattern.DOTALL);

	private static final Pattern REGEX_SEARCHRESULT_SIGNATURE = Pattern
			.compile(
					"<td class=\"SummaryFieldLegend\" nowrap=\"nowrap\" >Signatur</td>\\s*<td class=\"SummaryFieldLegendSep\"> : </td>\\s*<td class=\"SummaryFieldData\">([^<]*)</td>",
					Pattern.MULTILINE | Pattern.DOTALL);

	private static final Pattern REGEX_SEARCHRESULT_TYPE = Pattern.compile(
			"<div class=\"SummaryMaterialTypeField\">\\s*([^<]*)\\s*</div>",
			Pattern.MULTILINE | Pattern.DOTALL);

	private static final Pattern REGEX_SEARCHRESULT_YEAR = Pattern
			.compile(
					"<td class=\"SummaryFieldLegend\" nowrap=\"nowrap\" >Jahr</td>\\s*<td class=\"SummaryFieldLegendSep\"> : </td>\\s*<td class=\"SummaryFieldData\">([^<]*)</td>",
					Pattern.MULTILINE | Pattern.DOTALL);

	private static final Pattern REGEX_SEARCHRESULT_IMGURL = Pattern.compile(
			"(http://cover.ekz.de/\\d*.jpg)", Pattern.MULTILINE
			| Pattern.DOTALL);

	private static final Pattern REGEX_SEARCHRESULT_ID = Pattern
			.compile(
					"APS_PRESENT_BIB\\?Style=Portal3&SubStyle=&Lang=GER&ResponseEncoding=utf-8&amp;no=(T[^&]*)&",
					Pattern.MULTILINE | Pattern.DOTALL);

	private static final Pattern REGEX_DETAILS_TITLE = Pattern
			.compile(
					"<td style=\"width:8em;\" valign=top>\\s*Titel\\s*</td>\\s*<td valign=\"top\">:</td>\\s*<td valign=\"top\" class=\"darkLink\">([^:]*?)(: (.*?))?</td>",
					Pattern.MULTILINE | Pattern.DOTALL);

	private static final Pattern REGEX_DETAILS_AUTHORS = Pattern
			.compile(
					"<td style=\"width:8em;\" valign=top>\\s*Person\\(en\\)\\s*</td>\\s*<td valign=\"top\">:</td>\\s*<td valign=\"top\" class=\"darkLink\">(.*?)</td>",
					Pattern.MULTILINE | Pattern.DOTALL);

	private static final Pattern REGEX_DETAILS_IMGURL = Pattern.compile(
			"(http://cover.ekz.de/\\d*.jpg)", Pattern.MULTILINE
			| Pattern.DOTALL);

	private static final Pattern REGEX_DETAILS_CONTENTS = Pattern
			.compile(
					"<td style=\"width:8em;\" valign=top>\\s*Inhalt\\s*</td>\\s*<td valign=\"top\">:</td>\\s*<td valign=\"top\" class=\"darkLink\">(.*?)</td>",
					Pattern.MULTILINE | Pattern.DOTALL);

	private static final Pattern REGEX_DETAILS_STOCK = Pattern
			.compile(
					"<div style=\"margin-left:\\d+px;\" id=\"stock_header_[^>]*\">\\s*<b>[^<]*</b>\\s*</div>\\s*<div style=\"margin-left:\\d+px;\" id=\"stock_content_[^>]*\">.*?</div>",
					Pattern.MULTILINE | Pattern.DOTALL);

	private static final Pattern REGEX_DETAILS_STOCK_LOCATION = Pattern
			.compile(
					"<div style=\"margin-left:\\d+px;\" id=\"stock_header_([^>]*)\">\\s*<b>([^>]*)</b>",
					Pattern.MULTILINE | Pattern.DOTALL);

	private static final Pattern REGEX_DETAILS_STOCK_ITEM_AVAILABLE = Pattern
			.compile("Exemplar <b>verf.gbar</b>", Pattern.MULTILINE
					| Pattern.DOTALL);

	private static final Pattern REGEX_DETAILS_STOCK_ITEM_UNAVAILABLE = Pattern
			.compile(
					"Exemplar derzeit <b>entliehen</b>.(\\s*<!-- First item truly is on loan -->\\s*F.lligkeitsdatum\\s*(\\d\\d/\\d\\d/\\d\\d\\d\\d))?",
					Pattern.MULTILINE | Pattern.DOTALL);

	@Pref
	Preferences_ prefs;

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalLibraryBinder extends Binder {
		public LibraryService getService() {
			// Return this instance of LiibraryService so clients can call
			// public methods
			return LibraryService.this;
		}
	}

	private void loginToHoeb(final Account account) throws TechnicalException,
			LoginFailedException {
		if (account == null || account.getUsername() == null
				|| account.getPassword() == null) {
			throw new LoginFailedException();
		}

		final String content;
		try {
			// get request object for login call
			final String loginFormContent = HttpCallBuilder.anHttpCall()
					.toUrl(LOGIN_FORM_URL).executeAndGetContent();
			final Matcher loginFormMatcher = REGEX_LOGIN_FORM
					.matcher(loginFormContent);
			if (!loginFormMatcher.find()) {
				throw new TechnicalException("Login form not found");
			}

			// execute the login call
			final String requestObject = loginFormMatcher.group(1);
			content = HttpCallBuilder.anHttpCall()
					.toUrl(SERVICE_URL + requestObject)
					.usingMethod(Method.POST).withParam("Method", "CheckID")
					.withParam("ZonesLogin", "1")
					.withParam("Interlock", requestObject)
					.withParam("BrowseAsHloc", "")
					.withParam("Style", "Portal3").withParam("SubStyle", "")
					.withParam("Lang", "GER")
					.withParam("ResponseEncoding", "utf-8")
					.withParam("BRWR", account.getUsername())
					.withParam("PIN", account.getPassword())
					.executeAndGetContent();
		} catch (final Exception e) {
			throw new TechnicalException(e);
		}

		final Matcher matcher = REGEX_LOGIN.matcher(content);
		if (matcher.find()) {
			return;
		} else if (REGEX_WRONG_LOGIN.matcher(content).matches()
				|| REGEX_WRONG_PASSWORD.matcher(content).matches()) {
			// check if it is a login problem
			throw new LoginFailedException();
		} else {
			throw new TechnicalException(
					"Could not find username in loginresponse");
		}
	}

	public void refreshMediaList(final Context context)
			throws TechnicalException, LoginFailedException {
		refreshAndRenewMediaList(context, Collections.<RenewItem>emptySet());
	}

	public void refreshAndRenewMediaList(final Context context,
										 final Set<RenewItem> itemsToRenew) throws TechnicalException,
			LoginFailedException {

		final ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		operations.add(ContentProviderOperation.newDelete(
				MediaContentProvider.CONTENT_URI).build());
		final List<Account> accounts = Account.fromString(prefs.accounts()
				.get());
		try {
			for (final Account account : accounts) {
				loginToHoeb(account);

				final String content;
				content = HttpCallBuilder.anHttpCall().toUrl(MEDIALIST_URL)
						.usingMethod(Method.GET).executeAndGetContent();
				final List<Media> output = new ArrayList<Media>();

				Matcher m = REGEX_MEDIA_ITEM.matcher(content);
				while (m.find()) {
					final Media foundMedia = findMedia(m.group(1));
					renewIfNeeded(foundMedia, itemsToRenew,
							account.getUsername());
					output.add(foundMedia);
				}

				final String requestObject = findRequestObject(content);

				// Load the incremental list parts
				boolean foundBottom = false;
				int tries = 0;
				while (!foundBottom && tries++ < MAX_RETRIES) {
					String incrementContent;
					incrementContent = HttpCallBuilder.anHttpCall()
							.toUrl(SERVICE_URL + requestObject)
							.usingMethod(Method.GET)
							.withParam("Style", "Portal3")
							.withParam("SubStyle", "").withParam("Lang", "GER")
							.withParam("ResponseEncoding", "utf-8")
							.withParam("Method", "PageDown")
							.withParam("PageSize", "10").executeAndGetContent();
					m = REGEX_MEDIA_ITEM.matcher(incrementContent);
					while (m.find()) {
						final Media foundMedia = findMedia(m.group(1));
						if (output.contains(foundMedia)) {
							foundBottom = true;
						} else {
							renewIfNeeded(foundMedia, itemsToRenew,
									account.getUsername());
							output.add(foundMedia);
						}
					}
				}

				if (output.isEmpty()
						&& (REGEX_WRONG_LOGIN.matcher(content).matches() || REGEX_WRONG_LOGIN
						.matcher(content).matches())) {
					// check if it is a login problem
					throw new LoginFailedException();
				}
				operations
						.addAll(updateMedialistInDb(output, context, account));
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
		} catch (final Exception e) {
			if (e instanceof LoginFailedException) {
				throw (LoginFailedException) e;
			} else if (e instanceof TechnicalException) {
				throw (TechnicalException) e;
			} else {
				throw new TechnicalException(e);
			}
		}
	}

	private void updateNotifications(final Context context) {
		context.sendBroadcast(new Intent(context, UpdateService_.class));
	}

	private void renewIfNeeded(final Media foundMedia,
							   final Set<RenewItem> itemsToRenew, final String username)
			throws URISyntaxException, IOException {
		if (itemsToRenew.contains(new RenewItem(username, foundMedia
				.getSignature()))) {
			final String renewLink = foundMedia.getRenewLink();
			if (renewLink != null) {
				HttpCallBuilder.anHttpCall().toUrl(SERVICE_URL + renewLink)
						.executeAndIgnoreContent();
			}
		}
	}

	private String findRequestObject(final String content)
			throws TechnicalException {
		final Matcher matcher = REGEX_REQUEST_OBJECT.matcher(content);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			throw new TechnicalException("Unable to find request object");
		}
	}

	private void updateLastAccessDate() {
		prefs.lastAccess().put(new Date().getTime());
		prefs.notificationSent().put(0);
	}

	private List<ContentProviderOperation> updateMedialistInDb(
			final List<Media> mediaList, final Context context,
			final Account account) throws RemoteException,
			OperationApplicationException {
		final ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(
				mediaList.size());

		// insert new values
		for (int i = 0; i < mediaList.size(); i++) {
			final Media item = mediaList.get(i);
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
			final ContentProviderOperation insertOperation = ContentProviderOperation
					.newInsert(MediaContentProvider.CONTENT_URI)
					.withValues(value).build();
			operations.add(insertOperation);
		}
		return operations;
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
										HoebAppWidgetProvider_.class)));
		context.sendBroadcast(widgetIntent);
	}

	public void renewMedia(final Set<RenewItem> itemsToRenew,
						   final Context context) throws TechnicalException,
			LoginFailedException {
		// renew requested media
		refreshAndRenewMediaList(context, itemsToRenew);
		// update medialist to get latest renew_links
		refreshMediaList(context);
	}

	/**
	 * Renew all media (if possible)
	 *
	 * @param context
	 * @throws TechnicalException
	 * @throws LoginFailedException
	 */
	public void renewAllMedia(final Context context) throws TechnicalException,
			LoginFailedException {
		final List<Account> accounts = Account.fromString(prefs.accounts()
				.get());
		try {
			for (final Account account : accounts) {
				loginToHoeb(account);

				final String content;
				content = HttpCallBuilder.anHttpCall().toUrl(MEDIALIST_URL)
						.usingMethod(Method.GET).executeAndGetContent();
				final String requestObject = findRequestObject(content);

				HttpCallBuilder.anHttpCall().toUrl(SERVICE_URL + requestObject)
						.usingMethod(Method.GET).withParam("Lang", "GER")
						.withParam("ResponseEncoding", "utf-8")
						.withParam("Method", "BulkRenewAll")
						.withParam("BrowseAsHloc", "")
						.executeAndIgnoreContent();
			}
		} catch (final Exception e) {
			throw new TechnicalException(e);
		}
		refreshMediaList(context);
	}

	private Media findMedia(final String content) {
		final Media item = new Media();

		Matcher m = REGEX_MEDIA_DETAILS_MEDIUM_ID_TITLE.matcher(content);
		if (m.find()) {
			item.setMediumId(Html.fromHtml(m.group(1)).toString());
			item.setTitle(Html.fromHtml(m.group(2)).toString());
		}

		m = REGEX_MEDIA_DETAILS_AUTHOR.matcher(content);
		if (m.find()) {
			item.setAuthor(Html.fromHtml(m.group(1)).toString());
		}

		m = REGEX_MEDIA_DETAILS_SIGNATURE.matcher(content);
		if (m.find()) {
			item.setSignature(Html.fromHtml(m.group(1)).toString());
		}

		m = REGEX_MEDIA_DETAILS_DUEDATE.matcher(content);
		if (m.find()) {
			String dateString = null;
			try {
				dateString = Html.fromHtml(m.group(1)).toString();
				item.setDueDate(dateFormat.parse(dateString));
			} catch (final ParseException e) {
				// Date could not be parsed
				Log.w(getClass().getCanonicalName(),
						"Due Date could not be parsed: " + dateString);
			}
		}
		m = REGEX_MEDIA_DETAILS_LOANDATE.matcher(content);
		if (m.find()) {
			String dateString = null;
			try {
				dateString = Html.fromHtml(m.group(1)).toString();
				item.setLoanDate(dateFormat.parse(dateString));
			} catch (final ParseException e) {
				// Date could not be parsed
				Log.w(getClass().getCanonicalName(),
						"Loan Date could not be parsed: " + dateString);
			}
		}
		m = REGEX_MEDIA_DETAILS_RENEW.matcher(content);
		if (m.find()) {
			// link has mixed "&" and "&amp;"
			item.setRenewLink(m.group(1).replaceAll("&amp;", "&"));
		}
		m = REGEX_MEDIA_DETAILS_NO_RENEW_REASON.matcher(content);
		if (m.find()) {
			item.setNoRenewReason(Html.fromHtml(m.group(1)).toString());
		}
		m = REGEX_MEDIA_DETAILS_NUM_RENEWS.matcher(content);
		if (m.find()) {
			item.setNumRenews(Integer.parseInt(m.group(1)));
		}
		if (item.getTitle() == null || item.getDueDate() == null || item.getLoanDate() == null) {
			throw new RuntimeException("Media not found: " + content);
		}
		return item;
	}

	public List<SearchMedia> searchMedia(final String text,
										 final Context context) throws TechnicalException {
		return searchMedia(context, text, CATEGORY_KEYWORD, "", "", "", "");
	}

	public List<SearchMedia> searchMedia(final Context context,
										 final String text1, final String category1, final String text2,
										 final String category2, final String text3, final String category3)
			throws TechnicalException {
		final LinkedList<SearchMedia> result = new LinkedList<SearchMedia>();

		final String content;
		try {
			content = HttpCallBuilder.anHttpCall().toUrl(SERVICE_URL)
					.usingMethod(Method.GET).withParam("Style", "Portal3")
					.withParam("SubStyle", "").withParam("Lang", "GER")
					.withParam("ResponseEncoding", "utf-8")
					.withParam("Method", "QueryWithLimits")
					.withParam("SearchType", "AdvancedSearch")
					.withParam("TargetSearchType", "AdvancedSearch")
					.withParam("DB", "SearchServer")
					.withParam("q.PageSize", "20")
					.withParam("q.form.t1.term", category1)
					.withParam("q.form.t1.expr", text1)
					.withParam("q.form.t2.logic", " and ")
					.withParam("q.form.t2.term", category2)
					.withParam("q.form.t2.expr", text2)
					.withParam("q.form.t3.logic", " and ")
					.withParam("q.form.t3.term", category3)
					.withParam("q.form.t3.expr", text3).executeAndGetContent();

			final Matcher m = REGEX_SEARCHRESULT_ITEM.matcher(content);
			while (m.find()) {
				final SearchMedia foundMedia = findSearchResult(m.group(1));
				result.add(foundMedia);
			}

			return result;
		} catch (final Exception e) {
			throw new TechnicalException(e);
		}

	}

	public List<MediaDetails> loadNotepad() throws TechnicalException {
		final LinkedList<MediaDetails> result = new LinkedList<MediaDetails>();

		final List<Account> accounts = Account.fromString(prefs.accounts()
				.get());
		try {
			for (final Account account : accounts) {
				loginToHoeb(account);

				String content = HttpCallBuilder.anHttpCall().toUrl(NOTEPAD_PREFETCH_URL)
						.usingMethod(Method.GET).executeAndGetContent();

				// When the notepad has more than a few items, they will be loaded in the background
				if (needsBackgroundLoading(content)) {
					String requestObject = findRequestObject(content);
					content = loadNotepadInBackground(requestObject);
				}

				Matcher m = REGEX_SEARCHRESULT_ITEM.matcher(content);
				while (m.find()) {
					final MediaDetails foundMedia = findMediaDetails(m.group(1));
					foundMedia.setOwner(account);
					result.add(foundMedia);
				}

				String requestObject = findRequestObject(content);

				// Load the incremental list parts
				boolean foundBottom = !findPageDownLink(content);
				int tries = 0;
				while (!foundBottom && tries++ < MAX_RETRIES) {
					String incrementContent;
					incrementContent = HttpCallBuilder.anHttpCall()
							.toUrl(SERVICE_URL + requestObject)
							.usingMethod(Method.GET)
							.withParam("Style", "Portal3")
							.withParam("SubStyle", "").withParam("Lang", "GER")
							.withParam("ResponseEncoding", "utf-8")
							.withParam("Method", "PageDown")
							.withParam("PageSize", "10").executeAndGetContent();
					foundBottom = !findPageDownLink(incrementContent);
					m = REGEX_SEARCHRESULT_ITEM.matcher(incrementContent);
					while (m.find()) {
						final MediaDetails foundMedia = findMediaDetails(m.group(1));
						foundMedia.setOwner(account);
						if (result.contains(foundMedia)) {
							foundBottom = true;
						} else {
							result.add(foundMedia);
						}
					}
				}
			}
			return result;
		} catch (final Exception e) {
			throw new TechnicalException(e);
		}

	}

	private boolean findPageDownLink(String content) {
		Matcher m = REGEX_PAGE_DOWN.matcher(content);
		return m.find();
	}

	private boolean needsBackgroundLoading(String content) {
		Matcher m = REGEX_ZONES_TEMPLATE.matcher(content);
		if (m.find()) {
			return Html.fromHtml(m.group(1)).toString().equalsIgnoreCase("Notepad2");
		}
		return false;
	}

	private String loadNotepadInBackground(String requestObject) throws JSONException, URISyntaxException, IOException {
		// Wait until all objects all loaded
		JSONObject loadingState = new JSONObject();
		int tries = 0;
		while (!loadingState.optString("state").equalsIgnoreCase("Ready")) {
			Log.w("loadingState", loadingState.optString("state"));
			Log.w("loadingPercent", loadingState.optString("percent"));
			loadingState = new JSONObject(HttpCallBuilder.anHttpCall()
					.toUrl(SERVICE_URL + requestObject)
					.usingMethod(Method.GET)
					.withParam("Style", "Portal3")
					.withParam("SubStyle", "").withParam("Lang", "GER")
					.withParam("ResponseEncoding", "utf-8")
					.withParam("Method", "Start")
					.withParam("SubView", "LoadingJSON")
					.executeAndGetContent());
		}

		return HttpCallBuilder.anHttpCall().toUrl(NOTEPAD_POSTFETCH_URL)
				.usingMethod(Method.GET).executeAndGetContent();
	}


	public void addToNotepad(Account account, String mediumId) throws TechnicalException {
		loginToHoeb(account);
		try {
			HttpCallBuilder.anHttpCall()
					.toUrl(ADD_TO_NOTEPAD_URL + mediumId).executeAndIgnoreContent();
		} catch (final Exception e) {
			throw new TechnicalException(e);
		}
	}

	public void removeFromNotepad(Account account, String mediumId) throws TechnicalException {
		loginToHoeb(account);
		try {
			HttpCallBuilder.anHttpCall()
					.toUrl(REMOVE_FROM_NOTEPAD_URL + mediumId).executeAndIgnoreContent();
		} catch (final Exception e) {
			throw new TechnicalException(e);
		}
	}

	private SearchMedia findSearchResult(final String content) {
		final SearchMedia item = new SearchMedia();

		Matcher m = REGEX_SEARCHRESULT_TITLE.matcher(content);
		if (m.find()) {
			item.setTitle(Html.fromHtml(m.group(1)).toString());
		}

		m = REGEX_SEARCHRESULT_AUTHOR.matcher(content);
		if (m.find()) {
			item.setAuthor(Html.fromHtml(m.group(1)).toString());
		}

		m = REGEX_SEARCHRESULT_SIGNATURE.matcher(content);
		if (m.find()) {
			item.setSignature(Html.fromHtml(m.group(1)).toString());
		}

		m = REGEX_SEARCHRESULT_ID.matcher(content);
		if (m.find()) {
			item.setId(Html.fromHtml(m.group(1)).toString());
		}

		m = REGEX_SEARCHRESULT_TYPE.matcher(content);
		if (m.find()) {
			item.setType(Html.fromHtml(m.group(1)).toString());
		}

		m = REGEX_SEARCHRESULT_YEAR.matcher(content);
		if (m.find()) {
			item.setYear(Html.fromHtml(m.group(1)).toString());
		}

		m = REGEX_SEARCHRESULT_IMGURL.matcher(content);
		if (m.find()) {
			item.setImgUrl(Html.fromHtml(m.group(1)).toString());
		}

		return item;
	}

	public MediaDetails findMediaDetails(final String content) {
		MediaDetails item = new MediaDetails(findSearchResult(content));

		return item;
	}

	public MediaDetails getMediaDetails(final String mediumId)
			throws TechnicalException {
		final MediaDetails result = new MediaDetails();

		final String content;
		try {
			content = HttpCallBuilder.anHttpCall().toUrl(DETAIL_URL)
					.usingMethod(Method.GET).withParam("Style", "Portal3")
					.withParam("SubStyle", "").withParam("Lang", "GER")
					.withParam("ResponseEncoding", "utf-8")
					.withParam("no", mediumId).executeAndGetContent();

			Log.w("TITLE", "start");
			Matcher m = REGEX_DETAILS_TITLE.matcher(content);
			if (m.find()) {
				Log.w("TITLE", "found");
				final String titleGroup = m.group(1);
				final String subtitleGroup = m.group(3);
				result.setTitle(extractString(titleGroup));
				if (m.group(3) != null) {
					result.setSubTitle(extractString(subtitleGroup));
				}
			}
			Log.w("TITLE", "end");

			Log.w("Author", "start");
			m = REGEX_DETAILS_AUTHORS.matcher(content);
			if (m.find()) {
				final String group = m.group(1);
				result.setAuthor(extractString(group));
			}

			Log.w("IMGURL", "start");
			m = REGEX_DETAILS_IMGURL.matcher(content);
			if (m.find()) {
				final String group = m.group(1);
				result.setImgUrl(extractString(group));
			}

			Log.w("contents", "start");
			m = REGEX_DETAILS_CONTENTS.matcher(content);
			if (m.find()) {
				final String group = m.group(1);
				result.setContents(extractString(group));
			}

			Log.w("stock", "start");
			m = REGEX_DETAILS_STOCK.matcher(content);
			while (m.find()) {
				final String stockContent = m.group(0);
				Log.w("stocklocation", "start");
				final Matcher loc = REGEX_DETAILS_STOCK_LOCATION
						.matcher(stockContent);
				if (loc.find()) {
					final Stock stock = new Stock();
					stock.setLocationCode(extractString(loc.group(1)));
					stock.setLocationName(extractString(loc.group(2)));
					Log.w("stockavailable", "start");
					final Matcher available = REGEX_DETAILS_STOCK_ITEM_AVAILABLE
							.matcher(stockContent);
					while (available.find()) {
						stock.setInStock(stock.getInStock() + 1);
					}
					Log.w("stockunavailable", "start");
					final Matcher unavailable = REGEX_DETAILS_STOCK_ITEM_UNAVAILABLE
							.matcher(stockContent);
					while (unavailable.find()) {
						if (unavailable.group(2) != null) {
							final String dateString = unavailable.group(2);
							stock.getOutOfStock().add(
									dateFormat.parse(dateString));
						} else {
							stock.getOutOfStock().add(null);
						}
					}
					result.getStock().add(stock);
				}
			}

			return result;
		} catch (final Exception e) {
			throw new TechnicalException(e);
		}
	}

	private String extractString(final String group) {
		return Html.fromHtml(group).toString().replaceAll("\\s+", " ").trim();
	}
}
