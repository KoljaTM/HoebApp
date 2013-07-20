package de.vanmar.android.hoebapp.test.mocking;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.params.HttpParams;

public class MockHttpResponse implements HttpResponse {

	private final InputStream inputStream;

	public MockHttpResponse(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public void addHeader(Header arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean containsHeader(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Header[] getAllHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Header getFirstHeader(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Header[] getHeaders(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Header getLastHeader(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpParams getParams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProtocolVersion getProtocolVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HeaderIterator headerIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HeaderIterator headerIterator(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeHeader(Header arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeHeaders(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHeader(Header arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHeaders(Header[] arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setParams(HttpParams arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public HttpEntity getEntity() {
		try {
			return new InputStreamEntity(inputStream, inputStream.available());
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StatusLine getStatusLine() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEntity(HttpEntity arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setReasonPhrase(String arg0) throws IllegalStateException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStatusCode(int arg0) throws IllegalStateException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStatusLine(StatusLine arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStatusLine(ProtocolVersion arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStatusLine(ProtocolVersion arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub

	}

}
