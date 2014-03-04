package com.nhn.pinpoint.testweb.controller;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nhn.pinpoint.testweb.nimm.mockupserver.NimmInvokerTest;

/**
 * 
 * @author netspider
 * 
 */
@Controller
public class NIMMController implements DisposableBean {

	@Autowired
	private NimmInvokerTest nimm;

	@RequestMapping(value = "/nimm1")
	public String npc(Model model) {
		try {
			nimm.testInvoke();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "nimm";
	}

	@Override
	public void destroy() throws Exception {
		nimm.tearDown();
		nimm.dispose();
	}
}
