package de.vanmar.android.hoebapp.util;

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows to create HttpCalls and get the results
 *
 * @author Kolja Markwardt
 */
public class HttpCallBuilder {

	public enum Method {
		GET {
			@Override
			public HttpUriRequest getRequest(final HttpCallBuilder context)
					throws URISyntaxException, IOException {
				final URI uri;
				if (context.parameters.isEmpty()) {
					uri = new URI(context.url);
				} else {
					uri = new URI(context.url
							+ '?'
							+ URLEncodedUtils.format(context.parameters,
							HTTP.UTF_8));
				}
				final HttpGet httpGet = new HttpGet(uri);

				return httpGet;
			}
		},
		POST {
			@Override
			public HttpUriRequest getRequest(final HttpCallBuilder context)
					throws URISyntaxException, IOException {
				final HttpPost httpPost = new HttpPost(context.url);
				httpPost.setEntity(new UrlEncodedFormEntity(context.parameters,
						HTTP.UTF_8));

				return httpPost;
			}
		};

		public abstract HttpUriRequest getRequest(HttpCallBuilder context)
				throws URISyntaxException, IOException;
	}

	private Method method = Method.GET;

	private final List<NameValuePair> parameters = new ArrayList<NameValuePair>();

	private String url;

	private static HttpClient httpClient;

	public HttpCallBuilder toUrl(final String url) {
		this.url = url;
		return this;
	}

	public HttpCallBuilder usingMethod(final Method method) {
		this.method = method;
		return this;
	}

	public HttpCallBuilder withParam(final String paramName,
									 final String paramValue) {
		this.parameters.add(new BasicNameValuePair(paramName, paramValue));
		return this;
	}

	public static HttpCallBuilder anHttpCall() {
		return new HttpCallBuilder();
	}

	public String executeAndGetContent() throws URISyntaxException, IOException {
		Log.w("REQUEST", method.getRequest(this).getURI().toString());
		HttpClient localHttpClient = getHttpClient();
		synchronized (httpClient) {
			final HttpResponse response = localHttpClient.execute(
					method.getRequest(this));
			final InputStream content = response.getEntity().getContent();
			final String result = convertStreamToString(content);

			return result;
		}
	}

	public void executeAndIgnoreContent() throws URISyntaxException,
			IOException {
		Log.w("REQUEST", url);
		HttpClient localHttpClient = getHttpClient();
		synchronized (httpClient) {
			final HttpResponse response = localHttpClient.execute(
					method.getRequest(this));
			// need to consume the content
			response.getEntity().getContent().close();
		}
	}

	/**
	 * convenience method to read a String from an InputStream
	 * <p/>
	 * TODO: move into utility class, maybe use IOUtils?
	 *
	 * @param is Stream to read from
	 * @return
	 * @throws IOException
	 */
	public static String convertStreamToString(final InputStream is)
			throws IOException {
		/*
		 * To convert the InputStream to String we use the Reader.read(char[]
		 * buffer) method. We iterate until the Reader return -1 which means
		 * there's no more data to read. We use the StringWriter class to
		 * produce the string.
		 */
		if (is != null) {
			final Writer writer = new StringWriter();

			final char[] buffer = new char[1024];
			try {
				final Reader reader = new BufferedReader(new InputStreamReader(
						is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	public static void setHttpClient(final HttpClient httpClient) {
		HttpCallBuilder.httpClient = httpClient;
	}

	private static HttpClient getHttpClient() {
		if (httpClient == null) {
			final CookieStore cookieStore = new BasicCookieStore();

			// Create local HTTP context
			final HttpContext localContext = new BasicHttpContext();
			// Bind custom cookie store to the local context
			localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

			httpClient = new DefaultHttpClient();
			httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
					CookiePolicy.NETSCAPE);
		}

		return httpClient;
	}

}