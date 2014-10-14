package com.nhn.pinpoint.testweb.controller;

import com.nhn.pinpoint.testweb.connector.apachehttp4.ApacheHttpClient4;
import com.nhn.pinpoint.testweb.connector.apachehttp4.HttpConnectorOptions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * @author emeroad
 */
@Controller
public class ExcludeUrlController {
	@RequestMapping(value = "/excludeURL")
	@ResponseBody
	public String excludeUrl(HttpServletRequest request) {

		ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
        client.execute("http://localhost:" + request.getLocalPort() + "/monitor/l7check.html", new HashMap());
		return "OK";
	}

	@RequestMapping(value = "/nonExcludeURL")
	@ResponseBody
	public String nonExcludeURL(HttpServletRequest request) {

		ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
		client.execute("http://localhost:" + request.getLocalPort() + "/", new HashMap());
		return "OK";
	}

}
