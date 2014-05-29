package com.nhn.pinpoint.testweb.service.http;

/**
 * 
 * @author netspider
 * 
 */
public interface HttpClientService {

	public abstract String get();

	public abstract String getWithParam();

	public abstract String post();

	public abstract String postWithBody();

	public abstract String postMultipart();
}