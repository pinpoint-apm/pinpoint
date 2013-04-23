package com.nhn.hippo.testweb.util;

import java.net.InetAddress;
import java.util.HashMap;

import org.junit.Test;

public class HTClientTest {

	@Test
	public void test() {
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		String executeToBloc = client.executeToBloc("http://localhost:9080/", new HashMap<String, Object>());
		System.out.println(executeToBloc);

	}
}
