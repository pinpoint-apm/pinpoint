package com.nhn.pinpoint.testweb.service.http;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.testweb.connector.apachehttp4.ApacheClosableAsyncHttpClient;

/**
 * 
 * @author netspider
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/applicationContext-testweb.xml", "/servlet-context.xml" })
public class ApacheClosableAsyncHttpClientTest {

	@Autowired
	private ApacheClosableAsyncHttpClient apacheClosableAsyncHttpClient;

	@Test
	public void requestPost() {
		String requestPost = apacheClosableAsyncHttpClient.post();
		System.out.println(requestPost);
	}
}