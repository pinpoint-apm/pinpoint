/*
 * @(#)HttpInvokerSupport.java $version 2011. 8. 25.
 *
 * Copyright 2007 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.nhn.pinpoint.testweb.util;


/**
 * @author voyageth
 */
public class HttpInvokerSupport {
//	private final Logger log = LoggerFactory.getLogger(HttpInvokerSupport.class);
//
//	private String uri;
//	private int timeout = 3000;
//	private HttpConnectorOptions connectionOptions = null;
//
//	public String getUri() {
//		return uri;
//	}
//
//	public HttpInvokerSupport(String uri) {
//		this.setUri(uri);
//	}
//
//	// public HttpInvokerSupport() {
//	// }
//
//	public void setUri(String uri) {
//		this.uri = uri;
//		try {
//			connectionOptions = HttpConnectorOptionsFactory.getOptions(uri);
//		} catch (URISyntaxException e) {
//		}
//	}
//
//	public int getTimeout() {
//		return timeout;
//	}
//
//	public void setTimeout(int timeout) {
//		this.timeout = timeout;
//	}
//
//	public Map<String, Object> invoke(String uri, Map<String, Object> params) {
//		String targetUri = "http://" + connectionOptions.getHost() + ":" + connectionOptions.getPort() + "/" + connectionOptions.getModulePath() + uri;
//		if (log.isDebugEnabled()) {
//			log.debug("http invoke : " + targetUri);
//		}
//		return new HttpInvoker(connectionOptions).executeToBloc(targetUri, params);
//	}
//
//	public int invokeWithReturnInt(String uri, Map<String, Object> params) {
//		String targetUri = "http://" + connectionOptions.getHost() + ":" + connectionOptions.getPort() + "/" + connectionOptions.getModulePath() + uri;
//		if (log.isDebugEnabled()) {
//			log.debug("http invoke : " + targetUri);
//		}
//		return new HttpInvoker(connectionOptions).executeToBlocWithReturnInt(targetUri, params);
//	}
}
