package com.nhn.pinpoint.testweb.configuration;

import java.util.Random;

/**
 * 
 * @author netspider
 *
 */
public class DemoURLHolderLocal extends DemoURLHolder {

	private final Random random = new Random();
	
	private static final String[] BACKENDWEB_CALL_URL = {
		"http://localhost:10080/backendweb.pinpoint",
		"http://localhost:11080/backendweb.pinpoint"
	};
	
	private static final String[] BACKENDAPI_CALL_URL = {
		"http://localhost:12080/backendapi.pinpoint",
		"http://localhost:13080/backendapi.pinpoint"
	};

	@Override
	public String getBackendWebURL() {
		return BACKENDWEB_CALL_URL[random.nextInt(BACKENDWEB_CALL_URL.length)];
	}
	
	@Override
	public String getBackendApiURL() {
		return BACKENDAPI_CALL_URL[random.nextInt(BACKENDAPI_CALL_URL.length)];
	}
}
