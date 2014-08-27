package com.nhn.pinpoint.testweb.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nhn.pinpoint.testweb.service.CubridService;

/**
 *
 */
@Controller
public class CubridController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CubridService cubridService;

	@RequestMapping(value = "/cubrid/selectOne")
	public @ResponseBody
	String selectOne(Model model) {
		try {
			logger.info("selectOne start");

			int i = cubridService.selectOne();

			logger.info("selectOne end:{}", i);
			return "OK";
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return e.getMessage();
		}
	}

	@RequestMapping(value = "/cubrid/createStatement")
	public @ResponseBody
	String oracleStatement(Model model) {
		try {
			logger.info("createStatement start");
			
			cubridService.createStatement();

			logger.info("createStatement end:{}");
			return "OK";
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return e.getMessage();
		}
	}
}
