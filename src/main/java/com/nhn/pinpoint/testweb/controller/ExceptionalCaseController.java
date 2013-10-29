package com.nhn.pinpoint.testweb.controller;

import com.nhn.pinpoint.testweb.util.HttpConnectorOptions;
import com.nhn.pinpoint.testweb.util.HttpInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;

/**
 *
 */
@Controller
public class ExceptionalCaseController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 루트의 완성이 지연될 경우 먼저 끝난 rpc콜을 정상적으로 읽을수 있는지 테스트
     */
    @RequestMapping(value = "/exceptionalcase/rootslow")
    public void rootSlow() {
        HttpInvoker client2 = new HttpInvoker(new HttpConnectorOptions());
        client2.execute("http://localhost:8080/donothing.pinpoint", new HashMap<String, Object>());

        try {
            final int sleep = 1000*30;
            logger.info("sleep:{}", sleep);
            Thread.sleep(sleep);
        } catch (InterruptedException e) {

        }
    }
}
