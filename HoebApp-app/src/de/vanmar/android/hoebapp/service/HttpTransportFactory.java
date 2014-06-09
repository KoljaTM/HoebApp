package de.vanmar.android.hoebapp.service;

import org.ksoap2.transport.HttpTransportSE;

/**
 * Created by Kolja on 08.06.2014.
 * <p/>
 * Used to create the HttpTransport for soap requests. Extracted as a separate class to allow injecting of mock responses.
 */
public class HttpTransportFactory {
	public HttpTransportSE getHttpTransport(String url) {
		return new HttpTransportSE(url);
	}
}
