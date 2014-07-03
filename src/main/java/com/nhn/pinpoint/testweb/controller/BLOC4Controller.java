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
public class BLOC4Controller {

	private final String LOCAL_BLOC4_FORMAT = "http://%s:%s/welcome/test/hello?name=netspider";

	@RequestMapping(value = "/bloc4/callLocal")
	public @ResponseBody
	String requestGet(Model model,
			@RequestParam(required = false, defaultValue = "localhost") String host,
			@RequestParam(required = false, defaultValue = "5001") String port) {
		return HttpUtil.url(String.format(LOCAL_BLOC4_FORMAT, host, port)).method(HttpUtil.Method.GET).connectionTimeout(10000).readTimeout(10000).getContents();
	}
}
