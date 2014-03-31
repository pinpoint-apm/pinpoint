package com.nhn.pinpoint.testweb.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.testweb.util.AsyncHttpInvoker;
import com.ning.http.client.Part;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import com.ning.http.multipart.StringPart;

/**
 * 
 * @author netspider
 * 
 */
@Service
public class AsyncHttpClientServiceImpl implements AsyncHttpClientService {
	private static final Logger logger = LoggerFactory.getLogger(AsyncHttpClientServiceImpl.class);

	private final AsyncHttpInvoker httpInvoker;

	public AsyncHttpClientServiceImpl() {
		httpInvoker = new AsyncHttpInvoker();
	}

	@Override
	public String requestGet() {
		Response r = httpInvoker.requestGet("http://www.naver.com", null, null, null);
		logger.debug("r={}" + r.toString());
		return r.toString();
	}

	@Override
	public String requestGetWithParam() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("query", "naver");
		params.put("ie", "utf8");

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("header1", "header1");
		headers.put("header2", "header2");

		List<Cookie> cookies = new ArrayList<Cookie>();
		cookies.add(new Cookie("cookieName1", "cookieValue1", "cookieRawValue1", "", "/", 10, 10, false, false));
		cookies.add(new Cookie("cookieName2", "cookieValue2", "cookieRawValue2", "", "/", 10, 10, false, false));

		Response r = httpInvoker.requestGet("http://search.naver.com/search.naver?where=nexearch", params, headers, cookies);
		logger.debug("r={}" + r.toString());
		return r.toString();
	}

	@Override
	public String requestPost() {
		Response r = httpInvoker.requestPost("http://www.naver.com", null, null);
		logger.debug("r={}" + r.toString());
		return r.toString();
	}

	@Override
	public String requestPostWithBody() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("header1", "header1");
		headers.put("header2", "header2");

		Response r = httpInvoker.requestPost("http://www.naver.com", headers, "postbody");
		logger.debug("r={}" + r.toString());
		return r.toString();
	}

	@Override
	public String requestMultipart() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("header1", "header1");
		headers.put("header2", "header2");

		List<Part> parts = new ArrayList<Part>();
		try {
			parts.add(new com.ning.http.client.ByteArrayPart("name1", "filename1", "data".getBytes(), "plain/text", "utf-8"));
			parts.add(new com.ning.http.client.FilePart("name2", new File("./test"), "mimeType", "utf-8"));
			parts.add(new com.ning.http.client.StringPart("name3", "value3"));
			parts.add(new com.ning.http.multipart.FilePart("name4", new File("./test")));
			parts.add(new StringPart("name5", "value5"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		Response r = httpInvoker.requestMultipart("http://www.naver.com", headers, parts);
		logger.debug("r={}" + r.toString());
		return r.toString();
	}
}
