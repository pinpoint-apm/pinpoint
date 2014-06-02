package com.nhn.pinpoint.testweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nhn.pinpoint.testweb.connector.apachehttp4.ApacheClosableAsyncHttpClient;

/**
 * 
 * @author netspider
 * 
 */
@Controller
public class ApacheClosableAsyncHttpClientController {

	@Autowired
	private ApacheClosableAsyncHttpClient client;

	@RequestMapping(value = "/apacheClosableAsyncHttp/get")
	public @ResponseBody
	String requestGet(Model model) {
		return "NOT_IMPLEMENTED";
	}

	@RequestMapping(value = "/apacheClosableAsyncHttp/getWithParam")
	public @ResponseBody
	String requestGetWithParam(Model model) {
		return "NOT_IMPLEMENTED";
	}

	@RequestMapping(value = "/apacheClosableAsyncHttp/post")
	public @ResponseBody
	String requestPost(Model model) {
		client.post();
		return "OK";
	}

	@RequestMapping(value = "/apacheClosableAsyncHttp/postWithBody")
	public @ResponseBody
	String requestPostWithBody(Model model) {
		return "NOT_IMPLEMENTED";
	}

	@RequestMapping(value = "/apacheClosableAsyncHttp/postWithMultipart")
	public @ResponseBody
	String requestPostWithMultipart(Model model) {
		return "NOT_IMPLEMENTED";
	}
}