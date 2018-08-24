package de.vanmar.android.hoebapp.service;

import android.text.Html;
import org.androidannotations.annotations.EBean;
import org.ksoap2.serialization.SoapObject;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Kolja on 25.05.2014.
 */
@EBean
public class SoapHelper {


	/**
	 * Retrieves the loans from a GetBorrowerLoans soap response
	 *
	 * @return //GetBorrowerLoansResult/record/LoanDetails/LoanDetail
	 */
	public List<SoapObject> getLoans(SoapObject response) {
		return getList(get(get(get(response, "record"), "GetBorrowerLoansResult"), "LoanDetails"));
	}

	public String getCheckedUsername(SoapObject checkBorrowerResult) {
		return getString(get(get(checkBorrowerResult, "record"), "CheckBorrowerResult"), "Brwr");
	}

	public String getSessionId(SoapObject checkBorrowerResult) {
		return getString(get(get(checkBorrowerResult, "record"), "CheckBorrowerResult"), "sessionId");
	}

	public String getSessionIdForNotes(SoapObject loginResult) {
		return getString(loginResult, "sessionid");
	}

	public SoapObject get(SoapObject soapObject, String property) {
		if (soapObject == null) {
			return null;
		} else {
			return (SoapObject) soapObject.getPropertySafely(property, null);
		}
	}

	public String getString(SoapObject soapObject, String property) {
		if (soapObject == null) {
			return null;
		} else {
			return soapObject.getPrimitivePropertySafelyAsString(property);
		}
	}

	public String getStringFromHtml(SoapObject soapObject, String property) {
		if (soapObject == null) {
			return null;
		} else {
			return Html.fromHtml(soapObject.getPrimitivePropertySafelyAsString(property)).toString();
		}
	}

	public List<SoapObject> getList(SoapObject soapObject) {
		if (soapObject == null) {
			return Collections.emptyList();
		} else {
			LinkedList<SoapObject> result = new LinkedList<SoapObject>();
			for (int i = 0; i < soapObject.getPropertyCount(); i++) {
				Object property = soapObject.getProperty(i);
				if (property instanceof SoapObject)
					result.add((SoapObject) property);
			}
			return result;
		}
	}
}
