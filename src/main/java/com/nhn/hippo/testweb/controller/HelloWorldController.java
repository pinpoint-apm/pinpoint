package com.nhn.hippo.testweb.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.hippo.testweb.domain.Member;
import com.nhn.hippo.testweb.service.MemberService;
import com.nhn.hippo.testweb.util.HttpConnectorOptions;
import com.nhn.hippo.testweb.util.HttpInvoker;

@Controller
public class HelloWorldController {

	@Autowired
	private MemberService service;

	@RequestMapping(value = "/arcus", method = RequestMethod.POST)
	public String arcus(Model model, @RequestParam("id") int id) {
		System.out.println("\n\n\n\n/arcus.hippo");

		// delete
		service.delete(id);

		return "arcus";
	}

	@RequestMapping(value = "/helloworld", method = RequestMethod.GET)
	public String mysql(Model model) {
		int id = (new Random()).nextInt();

		Member member = new Member();
		member.setId(id);
		member.setName("chisu");
		member.setJoined(new Date());

		// add
		service.add(member);

		// get list
		List<Member> list = service.list();

		System.out.println(list);

		HttpInvoker client = new HttpInvoker(new HttpConnectorOptions());
		String executeToBloc = client.executeToBloc("http://localhost:9080/arcus.hippo?id=" + id, new HashMap<String, Object>());
		System.out.println(executeToBloc);

		return "helloworld";
	}
}
