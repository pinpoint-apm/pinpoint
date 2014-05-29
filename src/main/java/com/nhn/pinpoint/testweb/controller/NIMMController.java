package com.nhn.pinpoint.testweb.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private NimmInvokerTest nimm;

	@RequestMapping(value = "/nimm/1")
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
        System.out.println("-----------------------------");
        try {
            nimm.tearDown();
        } catch (Exception e) {
            logger.warn("tearDown() error Caused:" + e.getMessage(), e);
        }
        nimm.dispose();
	}
}
