package com.nhn.pinpoint.testweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nhn.pinpoint.testweb.service.http.HttpClientService;

/**
 * 
 * @author netspider
 * 
 */
@Controller
public class NingAsyncHTTPClientController {

	@Autowired
	private HttpClientService ningAsyncHttpClientService;

	@RequestMapping(value = "/ningAsyncHttp/get")
	public String requestGet(Model model) {
		ningAsyncHttpClientService.get();
		return "http";
	}

	@RequestMapping(value = "/ningAsyncHttp/getWithParam")
	public String requestGetWithParam(Model model) {
		ningAsyncHttpClientService.getWithParam();
		return "http";
	}

	@RequestMapping(value = "/ningAsyncHttp/post")
	public String requestPost(Model model) {
		ningAsyncHttpClientService.post();
		return "http";
	}

	@RequestMapping(value = "/ningAsyncHttp/postWithBody")
	public String requestPostWithBody(Model model) {
		ningAsyncHttpClientService.postWithBody();
		return "http";
	}
	
	@RequestMapping(value = "/ningAsyncHttp/postWithMultipart")
	public String requestPostWithMultipart(Model model) {
		ningAsyncHttpClientService.postMultipart();
		return "http";
	}
}
