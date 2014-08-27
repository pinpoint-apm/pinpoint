package com.nhn.pinpoint.testweb.httpclient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.Assert;

import org.junit.Test;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Part;
import com.ning.http.client.Response;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
import com.ning.http.multipart.StringPart;

public class AsyncHTTPClientTest {

	@Test
	public void asyncHttpClient() {
		AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder().build();
		AsyncHttpClient client = new AsyncHttpClient(new NettyAsyncHttpProvider(config), config);

		try {
			ListenableFuture<Response> future = client.prepareGet("http://www.naver.com").execute(new AsyncCompletionHandler<Response>() {
				@Override
				public Response onCompleted(Response response) throws Exception {
					// do something
					return response;
				}
			});

			future.get(300L, TimeUnit.MILLISECONDS);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void bodyPart() {
		try {
			AsyncHttpClient asyncHttpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setAllowPoolingConnection(true).setCompressionEnabled(true).build());
			BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost("http://dev.pinpoint.nhncorp.com");

			requestBuilder.addBodyPart(new StringPart("name1", "value1"));
			requestBuilder.addBodyPart(new StringPart("name2", "value2"));

			ListenableFuture<Response> f = requestBuilder.execute();
			f.get(500L, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void multiPart() {
		try {
			AsyncHttpClient asyncHttpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setAllowPoolingConnection(true).setCompressionEnabled(true).build());
			BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost("http://dev.pinpoint.nhncorp.com");

			Map<String, String> headers = new HashMap<String, String>();
			headers.put("header1", "header1");
			headers.put("header2", "header2");

			requestBuilder.addBodyPart(new com.ning.http.client.ByteArrayPart("name1", "filename1", "data".getBytes(), "plain/text", "utf-8"));
			requestBuilder.addBodyPart(new com.ning.http.client.FilePart("name2", new File("pom.xml"), "mimeType", "utf-8"));
			requestBuilder.addBodyPart(new com.ning.http.client.StringPart("name3", "value3"));
			requestBuilder.addBodyPart(new com.ning.http.multipart.FilePart("name4", new File("pom.xml")));
			requestBuilder.addBodyPart(new StringPart("name5", "value5"));

			ListenableFuture<Response> f = requestBuilder.execute();
			f.get(500L, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
}
