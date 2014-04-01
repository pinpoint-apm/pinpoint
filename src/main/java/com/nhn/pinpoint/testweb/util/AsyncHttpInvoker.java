package com.nhn.pinpoint.testweb.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Part;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;

/**
 * 
 * @author netspider
 * 
 */
public class AsyncHttpInvoker {

	private static final Logger logger = LoggerFactory.getLogger(AsyncHttpInvoker.class);

	private final AsyncHttpClient asyncHttpClient;
	private String defaultUserAgent;

	public AsyncHttpInvoker() {
		asyncHttpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setAllowPoolingConnection(true).setCompressionEnabled(true).build());
		defaultUserAgent = "pinpoint/test";
		logger.debug("init HttpClient : defaultAgent={}", defaultUserAgent);
	}

	public Response requestPost(String url, Map<String, String> headers, String body) {
		if (url == null) {
			return null;
		}
		BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost(url);

		try {
			if (headers != null) {
				for (Entry<String, String> entry : headers.entrySet()) {
					requestBuilder.addHeader(entry.getKey(), entry.getValue());
				}
			}

			requestBuilder.setBody(body).setBodyEncoding("UTF-8");

			Future<Response> f = requestBuilder.execute();
			Response response = f.get(500L, TimeUnit.MILLISECONDS);

			logger.debug("\n\t [POST] url \t: " + url + "\n\t headers \t: " + headers + "\n\t body \t\t: " + body + "\n\t reponse \t: " + response.toString());
			return response;
		} catch (Exception e) {
			logger.debug("request read-timeout : url \t: " + url + "\n\t headers \t: " + headers + "\n\t body \t: " + body);
			throw new RuntimeException(e);
		}
	}

	public Response requestMultipart(String url, Map<String, String> headers, List<Part> parts) {
		if (url == null) {
			return null;
		}
		BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost(url);

		try {
			if (headers != null) {
				for (Entry<String, String> entry : headers.entrySet()) {
					requestBuilder.addHeader(entry.getKey(), entry.getValue());
				}
			}

			if (parts != null) {
				for (Part part : parts) {
					requestBuilder.addBodyPart(part);
				}
			}

			Future<Response> f = requestBuilder.execute();
			Response response = f.get(500L, TimeUnit.MILLISECONDS);

			logger.debug("\n\t [POST] url \t: " + url + "\n\t headers \t: " + headers + "\n\t parts \t\t: " + parts + "\n\t reponse \t: " + response.toString());
			return response;
		} catch (Exception e) {
			logger.debug("request read-timeout : url \t: " + url + "\n\t headers \t: " + headers + "\n\t parts \t: " + parts);
			throw new RuntimeException(e);
		}
	}

	public Response requestGet(String url, Map<String, String> queries, Map<String, String> headers, List<Cookie> cookies) {
		if (url == null) {
			return null;
		}

		BoundRequestBuilder requestBuilder = asyncHttpClient.prepareGet(url);

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				requestBuilder.addCookie(cookie);
			}
		}

		if (queries != null) {
			for (Entry<String, String> entry : queries.entrySet()) {
				requestBuilder.addParameter(entry.getKey(), entry.getValue());
			}
		}

		if (headers != null) {
			for (Entry<String, String> entry : headers.entrySet()) {
				requestBuilder.addHeader(entry.getKey(), entry.getValue());
			}
		}

		try {
			Future<Response> f = requestBuilder.execute();
			Response response = f.get(60000L, TimeUnit.MILLISECONDS);

			logger.debug("\n\t [GET] url \t: " + url + "\n\t headers \t: " + headers + "\n\t queries \t: " + queries + "\n\t reponse \t: " + response.toString());

			return response;
		} catch (Exception e) {
			logger.debug("request read-timeout : url \t: " + url + "\n\t headers \t: " + headers + "\n\t queries \t: " + queries);
			throw new RuntimeException(e);
		}
	}

	public static Map<String, String> getDummyParams() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("query", "naver");
		params.put("ie", "utf8");
		return params;
	}

	public static Map<String, String> getDummyHeaders() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("header1", "header1");
		headers.put("header2", "header2");
		return headers;
	}

	public static List<Cookie> getDummyCookies() {
		List<Cookie> cookies = new ArrayList<Cookie>();
		cookies.add(new Cookie("cookieName1", "cookieValue1", "cookieRawValue1", "", "/", 10, 10, false, false));
		cookies.add(new Cookie("cookieName2", "cookieValue2", "cookieRawValue2", "", "/", 10, 10, false, false));
		return cookies;
	}
}
