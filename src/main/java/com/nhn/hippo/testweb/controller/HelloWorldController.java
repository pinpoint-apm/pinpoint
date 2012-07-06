package com.nhn.hippo.testweb.controller;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.nhn.hippo.testweb.domain.Member;
import com.nhn.hippo.testweb.service.MemberService;

@Controller
public class HelloWorldController {

	@Autowired
	private MemberService service;

	@RequestMapping(value = "/helloworld", method = RequestMethod.GET)
	public String example(Model model) {
		Member member = new Member();
		member.setId((new Random()).nextInt());
		member.setName("chisu");
		member.setJoined(new Date());

		service.add(member);

		List<Member> list = service.list();

		System.out.println(list);

		return "helloworld";
	}

}
