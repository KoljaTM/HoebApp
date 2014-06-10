package de.vanmar.android.hoebapp.test.mocking;

import de.vanmar.android.hoebapp.service.SoapLibraryService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Kolja on 09.06.2014.
 */
public class SoapMockNanoHTTPD extends NanoHTTPD {
	private static final String USERNAME = "username";
	private static final String USERNAME2 = "user2name";
	private static final String CHECKED_USERNAME = "use rna me";
	private static final String CHECKED_USERNAME2 = "use r2n ame";
	private static SoapMockNanoHTTPD instance;
	private List<String> uris = new LinkedList<String>();

	public SoapMockNanoHTTPD() throws IOException {
		super(8888, new File("../HoebApp-test/assets/mocks"));
	}

	public static SoapMockNanoHTTPD ensureRunningAndSetup() throws IOException {
		if (instance == null) {
			SoapLibraryService.USER_URL = getMockUrl(SoapLibraryService.USER_URL);
			SoapLibraryService.CATALOG_URL = getMockUrl(SoapLibraryService.CATALOG_URL);
			SoapLibraryService.NOTE_URL = getMockUrl(SoapLibraryService.NOTE_URL);
			SoapLibraryService.NOTES_URL = getMockUrl(SoapLibraryService.NOTES_URL);
			instance = new SoapMockNanoHTTPD();
		}
		return instance;
	}

	public static void stopServer() {
		if (instance != null) {
			instance.stop();
			instance = null;
		}
	}

	@Override
	public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
		String param = parms.toString();
		if (param.contains("CheckBorrower")) {
			if (param.contains(USERNAME)) {
				uri = uri + "/CheckBorrower.xml";
			} else if (param.contains(USERNAME2)) {
				uri = uri + "/CheckBorrower2.xml";
			} else {
				uri = uri + "/CheckBorrowerError.xml";
			}
		} else if (param.contains("GetBorrowerLoans")) {
			if (param.contains(CHECKED_USERNAME)) {
				uri = uri + "/GetBorrowerLoans.xml";
			} else if (param.contains(CHECKED_USERNAME2)) {
				uri = uri + "/GetBorrowerLoans2.xml";
			}
		} else if (param.contains("RenewItem")) {
			uri = uri + "/RenewItem.xml";
		} else if (param.contains("NoteRecord")) {
			uri = uri + "/NoteRecord.xml";
		} else if (param.contains("DeleteNote")) {
			uri = uri + "/DeleteNote.xml";
		} else if (param.contains("ReadNotesExt")) {
			uri = uri + "/ReadNotesExt.xml";
		} else if (param.contains("GetCatalogueItems")) {
			uri = uri + "/GetCatalogueItems.xml";
		}
		this.uris.add(uri);

		InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("de/vanmar/android/hoebapp/test/mocking/files" + uri);
		Response response = new Response(HTTP_OK, "text/xml; charset=utf-8", resourceAsStream);
		return response;
	}


	private static String getMockUrl(String url) {
		String replaced = url.replaceAll("(http|https)://[^/]*", "http://localhost:8888");
		//String replaced = url.replaceAll("(http|https)://[^/]*", "http://192.168.178.48:8888");
		return replaced;
	}

	public void clearUris() {
		uris.clear();
	}

	public List<String> getCalledUris() {
		return new ArrayList<String>(uris);
	}
}
