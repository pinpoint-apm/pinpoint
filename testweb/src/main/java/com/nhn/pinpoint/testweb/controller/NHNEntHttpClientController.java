package com.nhn.pinpoint.testweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nhn.pinpoint.testweb.connector.apachehttp4.nhnent.HttpUtil;

/**
 * 
 * @author netspider
 * 
 */
@Controller
public class NHNEntHttpClientController {

	@RequestMapping(value = "/nhnent/get")
	public @ResponseBody
	String requestGet(Model model, @RequestParam(required = false, defaultValue = "http") String protocol) {
		return HttpUtil.url(protocol + "://www.naver.com").method(HttpUtil.Method.GET).connectionTimeout(10000).readTimeout(10000).getContents();
	}

	@RequestMapping(value = "/nhnent/getWithParam")
	public @ResponseBody
	String requestGetWithParam(Model model) {
		return "NOT_IMPLEMENTED";
	}

	@RequestMapping(value = "/nhnent/post")
	public @ResponseBody
	String requestPost(Model model, @RequestParam(required = false, defaultValue = "http") String protocol) {
		return HttpUtil.url(protocol + "://www.naver.com").method(HttpUtil.Method.POST).connectionTimeout(10000).readTimeout(10000).getContents();
	}

	@RequestMapping(value = "/nhnent/postWithBody")
	public @ResponseBody
	String requestPostWithBody(Model model) {
		return "NOT_IMPLEMENTED";
	}

	@RequestMapping(value = "/nhnent/postWithMultipart")
	public @ResponseBody
	String requestPostWithMultipart(Model model) {
		return "NOT_IMPLEMENTED";
	}
}