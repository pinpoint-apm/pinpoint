package com.nhn.hippo.testweb.controller;

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
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.hippo.testweb.service.MemberService;
import com.nhn.hippo.testweb.util.HttpConnectorOptions;
import com.nhn.hippo.testweb.util.HttpInvoker;

@Controller
public class HelloWorldController {

	private static final ArcusClient arcus = ArcusClient.createArcusClient("dev.arcuscloud.nhncorp.com:17288", "dev", new ConnectionFactoryBuilder());

	@Autowired
	private MemberService service;

	@RequestMapping(value = "/arcus", method = RequestMethod.POST)
	public String arcus(Model model, @RequestParam("id") int id) {
//		// delete
//		System.out.println("before service.delete");
//		service.delete(id);
//		System.out.println("after service.delete");

		arcusSet(model);
		arcusSet(model);

		return "arcus";
	}

	@RequestMapping(value = "/arcustest", method = RequestMethod.GET)
	public String arcusSet(Model model) {
		setDataIntoArcus();
		return "arcus";
	}

	private void setDataIntoArcus() {
		try {
			System.out.println("before arcus.set");
			Future<Boolean> future = arcus.set("hippo:testkey", 10, "Hello, Hippo.");
			//future.get(1000L, TimeUnit.MILLISECONDS);
			System.out.println("after arcus.set");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/helloworld", method = RequestMethod.GET)
	public String mysql(Model model) {
		int id = (new Random()).nextInt();

//		Member member = new Member();
//		member.setId(id);
//		member.setName("chisu");
//		member.setJoined(new Date());
//
//		// add
//		System.out.println("before service.add");
//		service.add(member);
//		System.out.println("after service.add");
//
//		// list
//		System.out.println("before service.list");
//		List<Member> list = service.list();
//		System.out.println("after service.list");

		// invoke http
//		System.out.println("before invoke.http");
//		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
//		String executeToBloc = client.executeToBloc("http://localhost:9080/arcus.hippo?id=" + id, new HashMap<String, Object>());
//		System.out.println("after invoke.http");

		setDataIntoArcus();
		setDataIntoArcus();
		
		return "helloworld";
	}
}
