package com.nhn.pinpoint.testweb.controller;

import com.nhn.pinpoint.testweb.service.MySqlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 */
@Controller
public class MySqlController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MySqlService mySqlService;

    @RequestMapping(value = "/mysql/selectOne")
    public String selectOne(Model model) {
        logger.info("selectOne start");

        int i  = mySqlService.selectOne();

        logger.info("selectOne end:{}", i);
        return "donothing";
    }

    @RequestMapping(value = "/mysql/createStatement")
    public String oracleStatement(Model model) {
        logger.info("createStatement start");

        mySqlService.createStatement();

        logger.info("createStatement end:{}");
        return "donothing";
    }
}
