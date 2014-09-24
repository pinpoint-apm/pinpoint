package com.nhn.pinpoint.testweb.controller;

import com.nhn.pinpoint.testweb.nimm.mockupserver.NimmInvokerTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author netspider
 */
@Controller
public class NIMMController implements DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NimmInvokerTest nimm;

    @RequestMapping(value = "/nimm/1")
    @ResponseBody
    public String npc() {
        try {
            nimm.testInvoke();
        } catch (Exception e) {
            throw new RuntimeException(e);
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
