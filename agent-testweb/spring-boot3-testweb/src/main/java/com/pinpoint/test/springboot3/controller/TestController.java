package com.pinpoint.test.springboot3.controller;

import com.pinpoint.test.springboot3.service.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping(value = "/helloworld")
    public String helloworld() {
        return "helloworld";
    }


    @GetMapping(value = "/async")
    public String async() {
        return "async " + testService.getHello() + " world";
    }
}
