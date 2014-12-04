package com.nhn.pinpoint.testweb.util;

import java.util.HashMap;

import org.junit.Test;

import com.nhn.pinpoint.testweb.connector.apachehttp4.HttpConnectorOptions;
import com.nhn.pinpoint.testweb.connector.apachehttp4.ApacheHttpClient4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTClientTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


	@Test
	public void test() {
		ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
		String executeToBloc = client.execute("http://localhost:9080/", new HashMap<String, Object>());
		logger.debug(executeToBloc);

	}
}
