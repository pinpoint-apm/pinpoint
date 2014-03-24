package com.nhn.pinpoint.testweb.service;

public interface AsyncHttpClientService {

	public abstract String requestGet();

	public abstract String requestGetWithParam();

	public abstract String requestPost();

	public abstract String requestPostWithBody();

}