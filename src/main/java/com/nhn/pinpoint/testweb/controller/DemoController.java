package com.nhn.pinpoint.testweb.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nhn.pinpoint.testweb.configuration.DemoURLHolder;
import com.nhn.pinpoint.testweb.service.CacheService;
import com.nhn.pinpoint.testweb.service.CubridService;
import com.nhn.pinpoint.testweb.service.MemberService;
import com.nhn.pinpoint.testweb.util.AsyncHttpInvoker;
import com.nhn.pinpoint.testweb.util.HttpConnectorOptions;
import com.nhn.pinpoint.testweb.util.HttpInvoker;

/**
 * 
 * @author netspider
 * 
 */
@Controller
public class DemoController {

	private final DemoURLHolder urls;

	private final Random random = new Random();

	@Autowired
	private CacheService cacheService;

	@Autowired
	private MemberService mysqlService;

	@Autowired
	private CubridService cubridService;

	public DemoController() {
		urls = DemoURLHolder.getHolder();
	}

	@RequestMapping(value = "/netspider")
	public String demo1() {
		accessNaverBlog();
		accessNaverCafe();
		randomSlowMethod();
		callRemote(urls.getBackendApiURL());
		return "demo";
	}

	@RequestMapping(value = "/emeroad")
	public String demo2() {
		randomSlowMethod();
		callRemote(urls.getBackendWebURL());
		return "demo";
	}

	@RequestMapping(value = "/harebox")
	public String demo3() {
		cacheService.memcached();
		accessNaver();
		return "demo";
	}

	@RequestMapping(value = "/denny")
	public String demo4() {
		mysqlService.list();
		randomSlowMethod();
		return "demo";
	}

	@RequestMapping(value = "/backendweb")
	public String backendweb() {
		cacheService.arcus();
		mysqlService.list();
		if (random.nextBoolean()) {
			callRemote(urls.getBackendApiURL());
		}
		return "demo";
	}

	@RequestMapping(value = "/backendapi")
	public String backendapi() {
		mysqlService.list();
		cubrid();
		return "demo";
	}

	private void callRemote(String url) {
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

	private void accessNaver() {
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.execute("http://www.naver.com/", new HashMap<String, Object>());
	}

	private void accessNaverBlog() {
		AsyncHttpInvoker client = new AsyncHttpInvoker();
		client.requestGet("http://blog.naver.com/", null, null);
	}

	private void accessNaverCafe() {
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		try {
			connection = (HttpURLConnection) new URL("http://section.cafe.naver.com/").openConnection();
			connection.connect();

			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private void randomSlowMethod() {
		try {
			Thread.sleep(((new Random().nextInt(90)) + 10) * 10L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
