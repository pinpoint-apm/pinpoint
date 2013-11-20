package com.nhn.pinpoint.testweb.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ArcusClient;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nhn.pinpoint.testweb.service.CubridService;
import com.nhn.pinpoint.testweb.service.MemberService;
import com.nhn.pinpoint.testweb.util.HttpConnectorOptions;
import com.nhn.pinpoint.testweb.util.HttpInvoker;

@Controller
public class DemoController implements DisposableBean {

	/**
	 * dev-pinpoint-demo002.ncl
	 */
	private static final String HTTP_URL_BACKEND_WEB1 = "http://dev-pinpoint-workload002.ncl:8080/backend1.pinpoint";

	/**
	 * dev-pinpoint-demo003.ncl
	 */
	private static final String HTTP_URL_BACKEND_WEB2 = "http://dev-pinpoint-workload003.ncl:8080/backend2.pinpoint";

	private final ArcusClient arcus;
	private final MemcachedClient memcached;

	public DemoController() throws IOException {
		arcus = ArcusClient.createArcusClient("dev.arcuscloud.nhncorp.com:17288", "dev", new ConnectionFactoryBuilder());
		memcached = new MemcachedClient(AddrUtil.getAddresses("10.25.149.80:11244,10.25.149.80:11211,10.25.149.79:11211"));
	}

	@Autowired
	private MemberService service;

	@Autowired
	private CubridService cubridService;

	/**
	 * FRONT -> BACKEND1 -> CUBRID & MYSQL
	 * 
	 * @return
	 */
	@RequestMapping(value = "/netspider")
	public String demo1() {
		callBackend1();
		return "demo";
	}

	/**
	 * FRONT -> BACKEND1 -> ARCUS & MYSQL
	 * 
	 * @return
	 */
	@RequestMapping(value = "/emeroad")
	public String demo2() {
		callBackend2();
		return "demo";
	}

	/**
	 * FRONT -> MEMCACHED & NAVER
	 * 
	 * @return
	 */
	@RequestMapping(value = "/harebox")
	public String demo3() {
		memcached();
		naver();
		return "demo";
	}

	/**
	 * BACKEND -> MYSQL
	 * 
	 * @return
	 */
	@RequestMapping(value = "/denny")
	public String demo4() {
		mysql();
		return "demo";
	}

	@RequestMapping(value = "/backend1")
	public String backend1() {
		arcus();
		randomSlowMethod();
		mysql();
		return "demo";
	}

	@RequestMapping(value = "/backend2")
	public String backend2() {
		mysql();
		randomSlowMethod();
		cubrid();
		return "demo";
	}

	private void callBackend1() {
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.execute(HTTP_URL_BACKEND_WEB1, new HashMap<String, Object>());
	}

	private void callBackend2() {
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.execute(HTTP_URL_BACKEND_WEB2, new HashMap<String, Object>());
	}

	private void arcus() {
		int rand = new Random().nextInt(100);
		String key = "pinpoint:demo-" + rand;
		Future<Object> getFuture = null;
		try {
			getFuture = arcus.asyncGet(key);
			getFuture.get(1000L, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			if (getFuture != null)
				getFuture.cancel(true);
			throw new RuntimeException(e);
		}
	}

	private void memcached() {
		int rand = new Random().nextInt(100);
		String key = "pinpoint:demo-" + rand;
		Future<Object> getFuture = null;
		try {
			getFuture = memcached.asyncGet(key);
			getFuture.get(1000L, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			if (getFuture != null)
				getFuture.cancel(true);
			throw new RuntimeException(e);
		}
	}

	private void mysql() {
		service.list();
	}

	private void cubrid() {
		cubridService.createStatement();
	}

	private void naver() {
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.execute("http://www.naver.com/", new HashMap<String, Object>());
	}

	/**
	 * sleep 100 ~ 1000ms
	 */
	private void randomSlowMethod() {
		try {
			Thread.sleep(((new Random().nextInt(900)) + 100) * 10L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() throws Exception {
		arcus.shutdown();
		memcached.shutdown();
	}
}
