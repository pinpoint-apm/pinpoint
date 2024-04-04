package com.pinpoint.test.springboot3.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    @Async
    public String getHello() {
        return "hello";
    }
}
