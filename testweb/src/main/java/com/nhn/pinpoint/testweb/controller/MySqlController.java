package com.nhn.pinpoint.testweb.controller;

import com.nhn.pinpoint.testweb.domain.Member;
import com.nhn.pinpoint.testweb.service.MemberService;
import com.nhn.pinpoint.testweb.service.MySqlService;
import com.nhn.pinpoint.testweb.util.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.Random;

/**
 *
 */
@Controller
public class MySqlController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MySqlService mySqlService;

    @Autowired
    @Qualifier("memberService")
    private MemberService service;

    @RequestMapping(value = "/mysql/crud")
    @ResponseBody
    public String crud() {
        int id = (new Random()).nextInt();

        Member member = new Member();
        member.setId(id);
        member.setName("pinpoint_user");
        member.setJoined(new Date());

        service.add(member);
        service.list();
        service.delete(id);

        return "OK";
    }

    @RequestMapping(value = "/mysql/crudWithStatement")
    @ResponseBody
    public String crudWithStatement() {

        int id = (new Random()).nextInt();

        Member member = new Member();
        member.setId(id);
        member.setName("pinpoint_user");
        member.setJoined(new Date());

        service.addStatement(member);
        service.list();
        service.delete(id);

        return "OK";
    }

    @Description("preparedStatement 테스트. resultset은 가지고 오지 않음.")
    @RequestMapping(value = "/mysql/selectOne")
    @ResponseBody
    public String selectOne() {
        logger.info("selectOne start");

        int i = mySqlService.selectOne();

        logger.info("selectOne end:{}", i);
        return "OK";

    }

    @Description("statement 테스트. resultset은 가지고 오지 않음.")
    @RequestMapping(value = "/mysql/createStatement")
    @ResponseBody
    public String createStatement() {
        logger.info("createStatement start");

        mySqlService.createStatement();

        logger.info("createStatement end:{}");
        return "OK";

    }
}
