package com.nhn.pinpoint.testweb.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nhn.pinpoint.testweb.util.AsyncHttpInvoker;
import com.ning.http.client.Response;

/**
 * 
 * @author netspider
 * 
 */
@Controller
public class AsyncHTTPClientController {

	private static final Logger logger = LoggerFactory.getLogger(AsyncHTTPClientController.class);

	private final AsyncHttpInvoker httpInvoker;

	public AsyncHTTPClientController() {
		httpInvoker = new AsyncHttpInvoker();
	}

	@RequestMapping(value = "/asynchttp1")
	public String asynchttp(Model model) {
		Response r1 = httpInvoker.requestGet("http://www.naver.com", null, null);
		logger.debug("r1={}" + r1.toString());
		Response r2 = httpInvoker.requestPost("http://www.naver.com", null, "");
		logger.debug("r2={}" + r2.toString());

		Response r3 = httpInvoker.requestGet("http://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&ie=utf8&query=naver&x=0&y=0", null, null);
		logger.debug("r3={}" + r3.toString());

		Map<String, String> params = new HashMap<String, String>();
		params.put("query", "naver");
		params.put("where", "nexearch");
		Response r4 = httpInvoker.requestGet("http://search.naver.com/search.naver", null, null);
		logger.debug("r4={}" + r4.toString());

		Response r5 = httpInvoker.requestGet("http://localhost:10080/allInOne2.pinpoint", null, null);
		logger.debug("r5={}", r5.toString());

		return "http";
	}
}
