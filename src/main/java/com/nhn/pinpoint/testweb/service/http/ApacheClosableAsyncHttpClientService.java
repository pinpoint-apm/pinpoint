package com.nhn.pinpoint.testweb.service.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 
 * @author netspider
 * 
 */
@Service
public class ApacheClosableAsyncHttpClientService implements HttpClientService {

	private static final Logger logger = LoggerFactory.getLogger(ApacheClosableAsyncHttpClientService.class);

	private final CloseableHttpAsyncClient httpClient;

	public ApacheClosableAsyncHttpClientService() {
		this.httpClient = HttpAsyncClients.custom().useSystemProperties().build();
		this.httpClient.start();
	}

	@Override
	public String get() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWithParam() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String post() {
		try {
			HttpPost httpRequest = new HttpPost("http://dev.pinpoint.nhncorp.com/");

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("param1", "value1"));
			httpRequest.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8.name()));

			Future<HttpResponse> responseFuture = this.httpClient.execute(httpRequest, null);
			HttpResponse response = (HttpResponse) responseFuture.get();

			String result = null;
			if ((response != null) && (response.getEntity() != null)) {
				try {
					InputStream is = response.getEntity().getContent();
					result = IOUtils.toString(is);
				} catch (IOException e) {
				}
			}

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String postWithBody() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String postMultipart() {
		// TODO Auto-generated method stub
		return null;
	}
}