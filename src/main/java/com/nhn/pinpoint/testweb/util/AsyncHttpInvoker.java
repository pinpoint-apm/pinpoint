package com.nhn.pinpoint.testweb.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;

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

	public Response requestPost(String url, List<Entry<String, String>> headers, String body) {
		if (url == null) {
			return null;
		}
		BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost(url);
		try {
			requestBuilder = this.addHeader(requestBuilder, headers);
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

	public Response requestGet(String url, Map<String, List<String>> queries, List<Entry<String, String>> headers) {
		if (url == null) {
			return null;
		}

		BoundRequestBuilder requestBuilder = asyncHttpClient.prepareGet(url + queriesToQueryString(queries));

		requestBuilder = this.addHeader(requestBuilder, headers);

		try {
			Future<Response> f = requestBuilder.execute();
			Response response = f.get(500L, TimeUnit.MILLISECONDS);

			logger.debug("\n\t [GET] url \t: " + url + "\n\t headers \t: " + headers + "\n\t queries \t: " + queries + "\n\t reponse \t: " + response.toString());

			return response;
		} catch (Exception e) {
			logger.debug("request read-timeout : url \t: " + url + "\n\t headers \t: " + headers + "\n\t queries \t: " + queries);
			throw new RuntimeException(e);
		}
	}

	private String queriesToQueryString(Map<String, List<String>> queries) {
		if (queries == null) {
			return StringUtils.EMPTY;
		}

		StringBuilder sb = new StringBuilder();
		// not implemented haha.
		return sb.toString();
	}

	private BoundRequestBuilder addHeader(BoundRequestBuilder requestBuilder, List<Entry<String, String>> headers) {
		if (headers == null) {
			return requestBuilder;
		}

		for (Entry<String, String> entry : headers) {
			if (requestBuilder != null) {
				if (!entry.getKey().equals("User-Agent")) {
					requestBuilder.addHeader(entry.getKey(), entry.getValue());
				}
			}
		}

		// rewrite user-agent
		if (requestBuilder != null) {
			requestBuilder.addHeader("User-Agent", this.defaultUserAgent);
		}

		return requestBuilder;
	}
}
