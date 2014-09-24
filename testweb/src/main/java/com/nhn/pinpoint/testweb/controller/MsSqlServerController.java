package com.nhn.pinpoint.testweb.controller;

import com.nhn.pinpoint.testweb.service.MsSqlServerService;
import com.nhn.pinpoint.testweb.util.Description;
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
public class MsSqlServerController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MsSqlServerService msSqlServerService;


    @Description("preparedStatement 테스트. resultset은 가지고 오지 않음.")
    @RequestMapping(value = "/mssqlserver/selectOne")
    @ResponseBody
    public String selectOne() {
        logger.info("selectOne start");

        int i = msSqlServerService.selectOne();

        logger.info("selectOne end:{}", i);
        return "OK";
    }

    @Description("statement 테스트. resultset은 가지고 오지 않음.")
    @RequestMapping(value = "/mssqlserver/createStatement")
    @ResponseBody
    public String createStatement() {
        logger.info("createStatement start");

        msSqlServerService.createStatement();

        logger.info("createStatement end:{}");

        return "OK";
    }
}
