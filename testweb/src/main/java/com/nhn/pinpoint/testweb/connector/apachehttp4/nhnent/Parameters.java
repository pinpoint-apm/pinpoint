/*
 * @(#)Parameters.java $version 2012. 2. 3.
 *
 * Copyright 2007 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.nhn.pinpoint.testweb.connector.apachehttp4.nhnent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;

import com.nhn.pinpoint.testweb.connector.apachehttp4.nhnent.HttpUtil.Method;

/**
 * HttpUtil에 들어가는 파라메터 클래스 HttpUtil을 보다 쉽게 사용하기 위해서 제공되는 클래스 외부에서 생성할 수 있는 방법은
 * 없고, HttpUtil.url()을 통해서만 생성할 수 있다.
 * 
 * @author Xenos
 */
public class Parameters {
	private static final int READ_BUFFER_SIZE = 4096;

	private String urlValue;
	private Method methodValue = Method.GET; /* default = GET */
	private Charset charsetValue = null; /* default = 페이지 기본값 */
	private boolean passSSLErrorValue = false; /* default = false */
	private int connectionTimeoutValue = 0; /* default = JRE기본값 */
	private int readTimeoutValue = 0; /* default = 무제한 */
	private String jsonEntityValue = null; /* jsonEntity */
	private Map<String, String> header = new HashMap<String, String>(); /*
																		 * 헤더내용
																		 * 추가
																		 */

	Parameters(String theURL) throws HttpUtilException {
		if (theURL == null) {
			throw new HttpUtilException("url have to be not null");
		}
		this.urlValue = theURL;
	}

	/**
	 * 호출할 Method를 선택<br/>
	 * HttpUtil.Method의 값을 전달해야 한다.<br/>
	 * <br/>
	 * default : HttpUtil.Method.GET<br/>
	 * 
	 * @param value
	 *            HttpUtil.Method 값
	 * @return Parameters 클래스
	 */
	public Parameters method(Method value) {
		this.methodValue = value;
		return this;
	}

	/**
	 * 호출할 페이지의 인코딩 방식을 결정한다.<br/>
	 * default : null(페이지 기본값)
	 * 
	 * @param value
	 *            캐릭터셋
	 * @return Parameters 클래스
	 */
	public Parameters charset(Charset value) {
		this.charsetValue = value;
		return this;
	}

	/**
	 * 호출할때에, SSL오류를 무시할지 여부를 결정한다.<br/>
	 * default : false<br/>
	 * <br/>
	 * 참고사항 : 이 값을 true로 설정하고, readTimeout을 함께 설정할 경우, timeout시에도
	 * java.net.SocketTimeoutException: Read timed out 이 발생하지 않고,
	 * javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated 가 발생하는
	 * 문제가 있음<br/>
	 * 
	 * @param value
	 *            true/false 값
	 * @return Parameters 클래스
	 */
	public Parameters passSSLError(boolean value) {
		this.passSSLErrorValue = value;
		return this;
	}

	/**
	 * 접속시 timeout을 설정한다.<br/>
	 * default : 무제한<br/>
	 * 
	 * @param value
	 *            0 이상의 정수
	 * @return Parameters 클래스
	 * @throws HttpUtilException
	 *             설정값이 0 이하인 경우
	 */
	public Parameters connectionTimeout(int value) throws HttpUtilException {
		if (value < 0) {
			throw new HttpUtilException("connectionTimeout");
		}
		this.connectionTimeoutValue = value;
		return this;
	}

	/**
	 * 접속이 이루어진 후, 응답값을 기다리는 최대 시간을 설정한다.<br/>
	 * default : 무제한<br/>
	 * 
	 * @param value
	 *            0 이상의 정수
	 * @return Parameters 클래스
	 * @throws HttpUtilException
	 *             설정값이 0 이하인 경우
	 */
	public Parameters readTimeout(int value) throws HttpUtilException {
		if (value < 0) {
			throw new HttpUtilException("readTimeout");
		}
		this.readTimeoutValue = value;
		return this;
	}

	/**
	 * request에 추가할 헤더 기본적으로 헤더는 없음
	 * 
	 * @param name
	 *            헤더 이름
	 * @param value
	 *            헤더 값
	 * @return Parameters 클래스
	 * @throws HttpUtilException
	 *             name에 값이 없을 경우
	 */
	public Parameters addHeader(String name, String value) throws HttpUtilException {
		if (StringUtils.isEmpty(name)) {
			throw new HttpUtilException("header");
		}
		this.header.put(name, value);
		return this;
	}

	public Parameters jsonEntity(String value) {
		this.jsonEntityValue = value;
		return this;
	}

	/**
	 * 설정된 Parameters로 url을 호출한다.<br/>
	 * 
	 * @return 해당 url의 응답값의 String 형태
	 * @throws HttpException
	 *             http통신중 오류 발생시
	 */
	public String getContents() throws HttpUtilException {
		Writer writer = new StringWriter();
		char[] buffer = new char[READ_BUFFER_SIZE];
		Reader reader = new BufferedReader(getInputStreamReader());
		int readSize;
		try {
			while ((readSize = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, readSize);
			}

			reader.close();
		} catch (IOException e) {
			throw new HttpUtilException("Http read error! " + this.toString(), e);
		}

		return writer.toString();
	}

	/**
	 * 설정된 Parameters로 url을 호출한다.<br/>
	 * inputStreamReader를 모두 읽고 난 뒤에, close해주어야 한다.<br/>
	 * 
	 * @return 해당 url의 응답값의 inputStreamReader
	 * @throws HttpException
	 *             http통신준 오류 발생시
	 */
	public InputStreamReader getInputStreamReader() throws HttpUtilException {
		return HttpUtil.callUrl(urlValue, methodValue, charsetValue, passSSLErrorValue, connectionTimeoutValue, readTimeoutValue, jsonEntityValue, header);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Parameters[");
		sb.append("url=").append(urlValue);
		sb.append(", method=").append(methodValue.toString());
		if (charsetValue != null) {
			sb.append(", charset=").append(charsetValue.toString());
		} else {
			sb.append(", charset=null");
		}
		sb.append(", passSSLError=").append(passSSLErrorValue);
		sb.append(", connectionTimeout=").append(connectionTimeoutValue);
		sb.append(", readTimeout=").append(readTimeoutValue);
		sb.append(", jsonEntityValue=").append(jsonEntityValue);

		return sb.toString();
	}
}