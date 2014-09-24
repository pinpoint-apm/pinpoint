package com.nhn.pinpoint.testweb.controller;

import com.nhn.pinpoint.testweb.util.Description;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author netspider
 */
@Controller
public class DoNothingController {
    @Description("아무일도 하지 않음.")
    @RequestMapping(value = "/donothing")
    @ResponseBody
    public String donothing() {
        return "OK";
    }
}
