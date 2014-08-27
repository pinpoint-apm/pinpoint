package com.nhn.pinpoint.testweb.configuration;

import java.util.Random;

/**
 * 
 * @author netspider
 *
 */
public class DemoURLHolderDev extends DemoURLHolder {

	private final Random random = new Random();
	
	private static final String[] BACKENDWEB_CALL_URL = {
		"http://dev-pinpoint-workload002.ncl:8080/backendweb.pinpoint",
		"http://dev-pinpoint-workload002.ncl:9080/backendweb.pinpoint"
	};
	
	private static final String[] BACKENDAPI_CALL_URL = {
		"http://dev-pinpoint-workload003.ncl:8080/backendapi.pinpoint",
		"http://dev-pinpoint-workload003.ncl:9080/backendapi.pinpoint"
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
