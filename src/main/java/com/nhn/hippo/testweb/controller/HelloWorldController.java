package com.nhn.hippo.testweb.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.ArcusClient;
import net.spy.memcached.ArcusClientPool;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nhn.hippo.testweb.domain.Member;
import com.nhn.hippo.testweb.service.MemberService;
import com.nhn.hippo.testweb.util.HttpConnectorOptions;
import com.nhn.hippo.testweb.util.HttpInvoker;

@Controller
public class HelloWorldController {

	private static final ArcusClient arcus = ArcusClient.createArcusClient("dev.arcuscloud.nhncorp.com:17288", "dev", new ConnectionFactoryBuilder());

	private static final ArcusClientPool arcusPool = ArcusClient.createArcusClientPool("dev.arcuscloud.nhncorp.com:17288", "dev", new ConnectionFactoryBuilder(), 2);

	private static final MemcachedClient arcus2 = ArcusClient.createArcusClient("dev.arcuscloud.nhncorp.com:17288", "dev1.6", new ConnectionFactoryBuilder());

	@Autowired
	private MemberService service;

	/**
	 * DO NOTHING
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/donothing")
	public String donothing(Model model) {
		System.out.println("do nothing.");
		return "donothing";
	}

	/**
	 * CALL ARCUS
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/arcus")
	public String arcus(Model model) {
		Future<Boolean> future = null;
		try {
			future = arcus.set("hippo:testkey", 10, "Hello, Hippo.");
			future.get(1000L, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			if (future != null)
				future.cancel(true);
		}

		Future<Boolean> future2 = null;
		try {
			future2 = arcus2.set("hippo:testkey", 10, "Hello, Hippo.");
			future2.get(1000L, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			if (future2 != null)
				future2.cancel(true);
		}

		Future<Boolean> future3 = null;
		try {
			future3 = arcusPool.set("hippo:testkey", 10, "Hello, Hippo.");
			future3.get(1000L, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			if (future3 != null)
				future3.cancel(true);
		}

		ConnectionFactoryBuilder cfb = new ConnectionFactoryBuilder();
		cfb.setFrontCacheExpireTime(1000);
		cfb.setMaxFrontCacheElements(1000);
		ArcusClient c = ArcusClient.createArcusClient("dev.arcuscloud.nhncorp.com:17288", "dev", cfb);

		try {
			c.set("hippo:k", 1000, "V").get(500, TimeUnit.MILLISECONDS);
			c.get("hippo:k");
			c.get("hippo:k");
			c.get("hippo:k");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "arcus";
	}

	@RequestMapping(value = "/mysql")
	public String mysql(Model model) {
		int id = (new Random()).nextInt();

		Member member = new Member();
		member.setId(id);
		member.setName("chisu");
		member.setJoined(new Date());

		// add
		service.add(member);

		// list
		service.list();

		// del
		service.delete(id);

		return "mysql";
	}

	@RequestMapping(value = "/remotecombination")
	public String remotecombination(Model model) {
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.executeToBloc("http://localhost:8080/combination.hippo", new HashMap<String, Object>());

		return "remotecombination";
	}

	@RequestMapping(value = "/combination")
	public String combination(Model model) {
		mysql(model);
		arcus(model);

		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.executeToBloc("http://www.naver.com/", new HashMap<String, Object>());
		client.executeToBloc("http://www.naver.com/", new HashMap<String, Object>());

		client.executeToBloc("http://section.cafe.naver.com/", new HashMap<String, Object>());
		client.executeToBloc("http://section.cafe.naver.com/", new HashMap<String, Object>());

		return "combination";
	}

	@RequestMapping(value = "/error500")
	public String error500(Model model) {
		int i = 1 / 0;
		return "error";
	}

	@RequestMapping(value = "/throwexception")
	public String exception(Model model) {
		throw new RuntimeException("Exception test");
	}

	@RequestMapping(value = "/arcustimeout")
	public String arcustimeout(Model model) {
		Future<Boolean> future = null;
		try {
			future = arcus.set("hippo:expect-timeout", 10, "Hello, Timeout.");
			future.get(100L, TimeUnit.MICROSECONDS);
		} catch (Exception e) {
			if (future != null)
				future.cancel(true);
		}
		return "timeout";
	}
}
