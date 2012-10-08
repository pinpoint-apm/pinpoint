package com.nhn.hippo.testweb.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.ArcusClient;
import net.spy.memcached.ConnectionFactoryBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.nhn.hippo.testweb.domain.Member;
import com.nhn.hippo.testweb.service.MemberService;
import com.nhn.hippo.testweb.util.HttpConnectorOptions;
import com.nhn.hippo.testweb.util.HttpInvoker;

@Controller
public class HelloWorldController {

	private static final ArcusClient arcus = ArcusClient.createArcusClient("dev.arcuscloud.nhncorp.com:17288", "dev", new ConnectionFactoryBuilder());

	@Autowired
	private MemberService service;

	/**
	 * DO NOTHING
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/donothing", method = RequestMethod.GET)
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
	@RequestMapping(value = "/arcus", method = RequestMethod.POST)
	public String arcus(Model model) {
		try {
			Future<Boolean> future = arcus.set("hippo:testkey", 10, "Hello, Hippo.");
			future.get(1000L, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "arcus";
	}

	@RequestMapping(value = "/mysql", method = RequestMethod.GET)
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

	@RequestMapping(value = "/invoke_arcus_http", method = RequestMethod.GET)
	public String invoke_arcus_http(Model model) {
		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		client.executeToBloc("http://localhost:9080/arcus.hippo", new HashMap<String, Object>());

		return "http";
	}

	@RequestMapping(value = "/combination", method = RequestMethod.GET)
	public String combination(Model model) {
		donothing(model);
		arcus(model);
		mysql(model);
		invoke_arcus_http(model);

		return "http";
	}

}
