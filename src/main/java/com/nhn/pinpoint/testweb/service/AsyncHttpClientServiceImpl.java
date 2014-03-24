package com.nhn.pinpoint.testweb.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.testweb.util.AsyncHttpInvoker;
import com.ning.http.client.Response;

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
		Response r = httpInvoker.requestGet("http://www.naver.com", null, null);
		logger.debug("r={}" + r.toString());
		return r.toString();
	}

	@Override
	@SuppressWarnings("serial")
	public String requestGetWithParam() {
		Map<String, List<String>> params = new HashMap<String, List<String>>();
		params.put("query", new ArrayList<String>() {
			{
				add("naver");
			}
		});
		params.put("where", new ArrayList<String>() {
			{
				add("nexearch");
			}
		});

		Response r = httpInvoker.requestGet("http://search.naver.com/search.naver", params, null);
		logger.debug("r={}" + r.toString());
		return r.toString();
	}

	@Override
	public String requestPost() {
		Response r = httpInvoker.requestPost("http://www.naver.com", null, "");
		logger.debug("r={}" + r.toString());
		return r.toString();
	}

	@Override
	public String requestPostWithBody() {
		Response r = httpInvoker.requestPost("http://www.naver.com", null, "body");
		logger.debug("r={}" + r.toString());
		return r.toString();
	}
}
