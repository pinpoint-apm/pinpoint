package com.nhn.pinpoint.testweb.controller;

import com.nhn.pinpoint.testweb.connector.apachehttp4.ApacheClosableAsyncHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author netspider
 */
@Controller
public class ApacheClosableAsyncHttpClientController {

    @Autowired
    private ApacheClosableAsyncHttpClient client;

    @RequestMapping(value = "/apacheClosableAsyncHttp/get")
    @ResponseBody
    public String requestGet() {
        return "NOT_IMPLEMENTED";
    }

    @RequestMapping(value = "/apacheClosableAsyncHttp/getWithParam")
    @ResponseBody
    public String requestGetWithParam() {
        return "NOT_IMPLEMENTED";
    }

    @RequestMapping(value = "/apacheClosableAsyncHttp/post")
    @ResponseBody
    public String requestPost() {
        client.post();
        return "OK";
    }

    @RequestMapping(value = "/apacheClosableAsyncHttp/postWithBody")
    @ResponseBody
    public String requestPostWithBody() {
        return "NOT_IMPLEMENTED";
    }

    @RequestMapping(value = "/apacheClosableAsyncHttp/postWithMultipart")
    @ResponseBody
    public String requestPostWithMultipart() {
        return "NOT_IMPLEMENTED";
    }
}