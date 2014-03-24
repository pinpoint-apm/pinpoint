package com.nhn.pinpoint.testweb.controller;

import java.util.HashMap;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nhn.pinpoint.testweb.service.CacheService;
import com.nhn.pinpoint.testweb.service.CubridService;
import com.nhn.pinpoint.testweb.service.MemberService;
import com.nhn.pinpoint.testweb.util.HttpConnectorOptions;
import com.nhn.pinpoint.testweb.util.HttpInvoker;

@Controller
public class DemoController {

	/**
	 * dev-pinpoint-demo002.ncl
	 */
	private static final String HTTP_URL_BACKEND_WEB1 = "http://dev-pinpoint-workload002.ncl:8080/backend1.pinpoint";

	private static final String HTTP_URL_BACKEND_WEB11 = "http://dev-pinpoint-workload002.ncl:8080/backend11.pinpoint";

	/**
	 * dev-pinpoint-demo003.ncl
	 */
	private static final String HTTP_URL_BACKEND_WEB2 = "http://dev-pinpoint-workload003.ncl:8080/backend2.pinpoint";

	@Autowired
	private CacheService cacheService;

	@Autowired
	private MemberService mysqlService;

	@Autowired
	private CubridService cubridService;

	@RequestMapping(value = "/netspider")
	public String demo1() {
		randomSlowMethod();
		callBackend(HTTP_URL_BACKEND_WEB2);
		return "demo";
	}

	@RequestMapping(value = "/emeroad")
	public String demo2() {
		randomSlowMethod();
		callBackend(HTTP_URL_BACKEND_WEB1);
		return "demo";
	}

	@RequestMapping(value = "/harebox")
	public String demo3() {
		cacheService.memcached();
		naver();
		return "demo";
	}

	@RequestMapping(value = "/denny")
	public String demo4() {
		mysqlService.list();
		randomSlowMethod();
		return "demo";
	}

	@RequestMapping(value = "/backend1")
	public String backend1() {
		cacheService.arcus();
		mysqlService.list();
		return "demo";
	}

	@RequestMapping(value = "/backend2")
	public String backend2() {
		mysqlService.list();
		cubrid();
		return "demo";
	}

	@RequestMapping(value = "/threetier")
	public String threetier() {
		cacheService.memcached();
		callBackend(HTTP_URL_BACKEND_WEB11);
		return "demo";
	}

	@RequestMapping(value = "/backend11")
	public String backend11() {
		cacheService.arcus();
		mysqlService.list();
		callBackend(HTTP_URL_BACKEND_WEB11);
		return "demo";
	}

	private void callBackend(String url) {
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.execute(url, new HashMap<String, Object>());
	}

	private void cubrid() {
		switch (new Random().nextInt(3)) {
		case 1:
			cubridService.createErrorStatement();
		case 2:
			cubridService.createStatement();
		case 3:
			cubridService.selectOne();
		}
	}

	private void naver() {
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.execute("http://www.naver.com/", new HashMap<String, Object>());
	}

	private void randomSlowMethod() {
		try {
			Thread.sleep(((new Random().nextInt(90)) + 10) * 10L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
