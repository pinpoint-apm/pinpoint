package com.nhn.pinpoint.testweb.service.http;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 
 * @author netspider
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/root-context.xml", "/servlet-context.xml" })
public class ApacheClosableAsyncHttpClientTest {

	@Autowired
	private ApacheClosableAsyncHttpClientService apacheClosableAsyncHttpClientService;

	@Test
	public void requestPost() {
		String requestPost = apacheClosableAsyncHttpClientService.post();
		System.out.println(requestPost);
	}
}