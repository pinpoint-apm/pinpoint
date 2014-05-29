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
public class ApacheClosableAsyncHttpClientController {

	@Autowired
	private HttpClientService apacheClosableAsyncHttpClientService;

	@RequestMapping(value = "/apacheClosableAsyncHttp/get")
	public String requestGet(Model model) {
		apacheClosableAsyncHttpClientService.get();
		return "http";
	}

	@RequestMapping(value = "/apacheClosableAsyncHttp/getWithParam")
	public String requestGetWithParam(Model model) {
		apacheClosableAsyncHttpClientService.getWithParam();
		return "http";
	}

	@RequestMapping(value = "/apacheClosableAsyncHttp/post")
	public String requestPost(Model model) {
		apacheClosableAsyncHttpClientService.post();
		return "http";
	}

	@RequestMapping(value = "/apacheClosableAsyncHttp/postWithBody")
	public String requestPostWithBody(Model model) {
		apacheClosableAsyncHttpClientService.postWithBody();
		return "http";
	}

	@RequestMapping(value = "/apacheClosableAsyncHttp/postWithMultipart")
	public String requestPostWithMultipart(Model model) {
		apacheClosableAsyncHttpClientService.postMultipart();
		return "http";
	}
}