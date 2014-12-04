package com.nhn.pinpoint.testweb.controller;

import com.nhn.pinpoint.testweb.service.CubridService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 */
@Controller
public class CubridController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CubridService cubridService;

    @RequestMapping(value = "/cubrid/selectOne")
    @ResponseBody
    public String selectOne() {
        logger.info("selectOne start");

        int i = cubridService.selectOne();

        logger.info("selectOne end:{}", i);
        return "OK";
    }

    @RequestMapping(value = "/cubrid/createStatement")
    @ResponseBody
    public String createStatement() {
        logger.info("createStatement start");

        cubridService.createStatement();

        logger.info("createStatement end:{}");
        return "OK";
    }
}
