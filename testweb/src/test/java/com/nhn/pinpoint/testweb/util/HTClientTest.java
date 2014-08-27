package com.nhn.pinpoint.testweb.util;

import java.util.HashMap;

import org.junit.Test;

import com.nhn.pinpoint.testweb.connector.apachehttp4.HttpConnectorOptions;
import com.nhn.pinpoint.testweb.connector.apachehttp4.ApacheHttpClient4;

public class HTClientTest {

	@Test
	public void test() {
		ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
		String executeToBloc = client.execute("http://localhost:9080/", new HashMap<String, Object>());
		System.out.println(executeToBloc);

	}
}
