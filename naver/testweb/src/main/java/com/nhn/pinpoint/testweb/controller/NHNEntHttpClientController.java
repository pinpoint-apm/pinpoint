package com.nhn.pinpoint.testweb.controller;

import com.nhn.pinpoint.testweb.connector.apachehttp4.nhnent.HttpUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author netspider
 */
@Controller
public class NHNEntHttpClientController {

    @RequestMapping(value = "/nhnent/get")
    @ResponseBody
    public String requestGet(Model model, @RequestParam(required = false, defaultValue = "http") String protocol) {
        return HttpUtil.url(protocol + "://www.naver.com").method(HttpUtil.Method.GET).connectionTimeout(10000).readTimeout(10000).getContents();
    }

    @RequestMapping(value = "/nhnent/getWithParam")
    @ResponseBody
    public String requestGetWithParam() {
        return "NOT_IMPLEMENTED";
    }

    @RequestMapping(value = "/nhnent/post")
    @ResponseBody
    public String requestPost(Model model, @RequestParam(required = false, defaultValue = "http") String protocol) {
        return HttpUtil.url(protocol + "://www.naver.com").method(HttpUtil.Method.POST).connectionTimeout(10000).readTimeout(10000).getContents();
    }

    @RequestMapping(value = "/nhnent/postWithBody")
    @ResponseBody
    public String requestPostWithBody() {
        return "NOT_IMPLEMENTED";
    }

    @RequestMapping(value = "/nhnent/postWithMultipart")
    public String requestPostWithMultipart() {
        return "NOT_IMPLEMENTED";
    }
}