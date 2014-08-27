package com.nhn.pinpoint.testweb.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nhn.pinpoint.testweb.nimm.mockupserver.NimmInvokerTest;

/**
 * 
 * @author netspider
 * 
 */
@Controller
public class NIMMController implements DisposableBean {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private NimmInvokerTest nimm;

	@RequestMapping(value = "/nimm/1")
	public @ResponseBody
	String npc(Model model) {
		try {
			nimm.testInvoke();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return "OK";
	}

	@Override
	public void destroy() throws Exception {
		try {
			nimm.tearDown();
		} catch (Exception e) {
			logger.warn("tearDown() error Caused:" + e.getMessage(), e);
		}
		nimm.dispose();
	}
}
