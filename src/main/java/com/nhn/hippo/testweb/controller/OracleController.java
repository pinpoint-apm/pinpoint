package com.nhn.hippo.testweb.controller;

import com.nhn.hippo.testweb.service.OracleService;
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
public class OracleController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private OracleService oracleService;

    @RequestMapping(value = "/selectOne")
    public String selectOne(Model model) {
        logger.info("selectOne start");

        int i  = oracleService.selectOne();

        logger.info("selectOne end:{}", i);
        return "donothing";
    }

    @RequestMapping(value = "/oracleStatement")
    public String oracleStatement(Model model) {
        logger.info("oracleStatement start");

        oracleService.oracleStatement();

        logger.info("oracleStatement end:{}");
        return "donothing";
    }
}
