package com.nhn.pinpoint.testweb.connector.apachehttp4.nhnent;

import org.junit.Test;

/**
 * 
 * @author netspider
 * 
 */
public class HttpUtilTest {

	private final String URL = "http://www.naver.com/";

	@Test
	public void callUrl() {
		String response = null;
		try {
			response = HttpUtil.url(URL).method(HttpUtil.Method.POST).connectionTimeout(10000).readTimeout(10000).getContents();
		} catch (HttpUtilException e) {
		}

		System.out.println(response);
	}

}
