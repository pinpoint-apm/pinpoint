package com.nhn.pinpoint.testweb.controller;

import com.nhn.pinpoint.testweb.service.OracleService;
import com.nhn.pinpoint.testweb.util.Description;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 */
@Controller
public class OracleController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private OracleService oracleService;

	@Description("preparedStatement 테스트. resultset은 가지고 오지 않음.")
	@RequestMapping(value = "/oracle/selectOne")
	public @ResponseBody
	String selectOne(Model model) {
		logger.info("selectOne start");

		int i = oracleService.selectOne();

		logger.info("selectOne end:{}", i);
		return "OK";
	}

	@Description("statement 테스트. resultset은 가지고 오지 않음")
	@RequestMapping(value = "/oracle/createStatement")
	public @ResponseBody
	String oracleStatement(Model model) {
		logger.info("createStatement start");

		oracleService.createStatement();

		logger.info("createStatement end:{}");
		return "OK";
	}
}
