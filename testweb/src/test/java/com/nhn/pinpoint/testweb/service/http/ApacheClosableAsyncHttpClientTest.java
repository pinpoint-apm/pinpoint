package com.nhn.pinpoint.testweb.service.http;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ApacheClosableAsyncHttpClient apacheClosableAsyncHttpClient;

	@Test
	public void requestPost() {
		String requestPost = apacheClosableAsyncHttpClient.post();
		logger.debug(requestPost);
	}
}