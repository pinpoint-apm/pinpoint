package com.nhn.pinpoint.testweb.connector.apachehttp4;

import java.nio.charset.Charset;

/**
 * 
 * @author netspider
 * 
 */
public class HttpConnectorOptions {
	private String host;
	private int port;
	private int callTimeout;
	private int connectionTimeout;
	private int soTimeout;
	private Charset charset;
	private String modulePath;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getCallTimeout() {
		return callTimeout;
	}

	public void setCallTimeout(int callTimeout) {
		this.callTimeout = callTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public String getModulePath() {
		return modulePath;
	}

	public void setModulePath(String modulePath) {
		this.modulePath = modulePath;
	}
}
