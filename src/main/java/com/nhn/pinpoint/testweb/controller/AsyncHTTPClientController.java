package com.nhn.pinpoint.testweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nhn.pinpoint.testweb.service.AsyncHttpClientService;

/**
 * 
 * @author netspider
 * 
 */
@Controller
public class AsyncHTTPClientController {

	@Autowired
	private AsyncHttpClientService asyncHttpClientService;

	@RequestMapping(value = "/asynchttp/get")
	public String requestGet(Model model) {
		asyncHttpClientService.requestGet();
		return "http";
	}

	@RequestMapping(value = "/asynchttp/getWithParam")
	public String requestGetWithParam(Model model) {
		asyncHttpClientService.requestGetWithParam();
		return "http";
	}

	@RequestMapping(value = "/asynchttp/post")
	public String requestPost(Model model) {
		asyncHttpClientService.requestPost();
		return "http";
	}

	@RequestMapping(value = "/asynchttp/postWithBody")
	public String requestPostWithBody(Model model) {
		asyncHttpClientService.requestPostWithBody();
		return "http";
	}
}
