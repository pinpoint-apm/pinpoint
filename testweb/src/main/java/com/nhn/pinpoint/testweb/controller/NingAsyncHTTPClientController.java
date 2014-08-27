package com.nhn.pinpoint.testweb.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nhn.pinpoint.testweb.connector.ningasync.NingAsyncHttpClient;
import com.ning.http.client.Part;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import com.ning.http.multipart.StringPart;

/**
 * 
 * @author netspider
 * 
 */
@Controller
public class NingAsyncHTTPClientController {

	private static final Logger logger = LoggerFactory.getLogger(NingAsyncHTTPClientController.class);

	@Autowired
	private NingAsyncHttpClient ningAsyncHttpClient;

	@Autowired
	private NingAsyncHttpClient httpInvoker;

	@RequestMapping(value = "/ningAsyncHttp/get")
	public @ResponseBody
	String requestGet(Model model) {
		try {
			Response r = httpInvoker.requestGet("http://www.naver.com", null, null, null);
			logger.debug("r={}" + r.toString());
			return "OK";
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return e.getMessage();
		}
	}

	@RequestMapping(value = "/ningAsyncHttp/getWithParam")
	public @ResponseBody
	String requestGetWithParam(Model model) {
		try {
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
			return "OK";
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return e.getMessage();
		}
	}

	@RequestMapping(value = "/ningAsyncHttp/post")
	public @ResponseBody
	String requestPost(Model model) {
		try {
			Response r = httpInvoker.requestPost("http://www.naver.com", null, null);
			logger.debug("r={}" + r.toString());
			return "OK";
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return e.getMessage();
		}
	}

	@RequestMapping(value = "/ningAsyncHttp/postWithBody")
	public @ResponseBody
	String requestPostWithBody(Model model) {
		try {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("header1", "header1");
			headers.put("header2", "header2");

			Response r = httpInvoker.requestPost("http://www.naver.com", headers, "postbody");
			logger.debug("r={}" + r.toString());
			return "OK";
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return e.getMessage();
		}
	}

	@RequestMapping(value = "/ningAsyncHttp/postWithMultipart")
	public @ResponseBody
	String requestPostWithMultipart(Model model) {
		try {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("header1", "header1");
			headers.put("header2", "header2");

			List<Part> parts = new ArrayList<Part>();
			parts.add(new com.ning.http.client.ByteArrayPart("name1", "filename1", "data".getBytes(), "plain/text", "utf-8"));
			parts.add(new com.ning.http.client.FilePart("name2", new File("./test"), "mimeType", "utf-8"));
			parts.add(new com.ning.http.client.StringPart("name3", "value3"));
			parts.add(new com.ning.http.multipart.FilePart("name4", new File("./test")));
			parts.add(new StringPart("name5", "value5"));

			Response r = httpInvoker.requestMultipart("http://www.naver.com", headers, parts);
			logger.debug("r={}" + r.toString());
			return "OK";
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return e.getMessage();
		}
	}
}
