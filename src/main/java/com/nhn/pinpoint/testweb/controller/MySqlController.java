package com.nhn.pinpoint.testweb.controller;

import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nhn.pinpoint.testweb.domain.Member;
import com.nhn.pinpoint.testweb.service.MemberService;
import com.nhn.pinpoint.testweb.service.MySqlService;
import com.nhn.pinpoint.testweb.util.Description;

/**
 *
 */
@Controller
public class MySqlController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private MySqlService mySqlService;

	@Autowired
	@Qualifier("memberService")
	private MemberService service;

	@RequestMapping(value = "/mysql/crud")
	public String crud(Model model) {
		int id = (new Random()).nextInt();

		Member member = new Member();
		member.setId(id);
		member.setName("chisu");
		member.setJoined(new Date());

		service.add(member);
		service.list();
		service.delete(id);

		return "mysql";
	}

	@RequestMapping(value = "/mysql/crudWithStatement")
	public String crudWithStatement(Model model) {
		int id = (new Random()).nextInt();

		Member member = new Member();
		member.setId(id);
		member.setName("chisu");
		member.setJoined(new Date());

		service.addStatement(member);
		service.list();
		service.delete(id);

		return "mysql";
	}

	@Description("preparedStatement 테스트. resultset은 가지고 오지 않음.")
	@RequestMapping(value = "/mysql/selectOne")
	public String selectOne(Model model) {
		logger.info("selectOne start");

		int i = mySqlService.selectOne();

		logger.info("selectOne end:{}", i);
		return "mysql";
	}

	@Description("statement 테스트. resultset은 가지고 오지 않음.")
	@RequestMapping(value = "/mysql/createStatement")
	public String oracleStatement(Model model) {
		logger.info("createStatement start");

		mySqlService.createStatement();

		logger.info("createStatement end:{}");
		return "mysql";
	}
}
