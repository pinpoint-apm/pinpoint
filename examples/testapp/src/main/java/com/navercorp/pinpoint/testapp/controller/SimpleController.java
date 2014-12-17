package com.navercorp.pinpoint.testapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.testapp.util.Description;

@Controller
public class SimpleController {

    @RequestMapping("/getCurrentTimestamp")
    @Description("Returns the current timestamp of the server (in ms).")
    @ResponseBody
    public Long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
}
