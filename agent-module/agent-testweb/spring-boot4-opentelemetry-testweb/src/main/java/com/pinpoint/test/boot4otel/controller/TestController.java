package com.pinpoint.test.boot4otel.controller;

import com.pinpoint.test.boot4otel.service.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class TestController {

    private final TestService testService;
    private final RestClient restClient;

    public TestController(TestService testService, RestClient.Builder restClientBuilder) {
        this.testService = testService;
        this.restClient = restClientBuilder.build();
    }

    @GetMapping("/helloworld")
    public String helloworld() {
        return "helloworld";
    }

    @GetMapping("/user/{id}")
    public String user(@PathVariable("id") String id) {
        return "user " + id;
    }

    @GetMapping("/observed")
    public String observed() {
        return "observed " + testService.observedHello();
    }

    @GetMapping("/remote")
    public String remote() {
        // self call: server span -> http client span -> server span (context propagated via W3C traceparent)
        String body = restClient.get()
                .uri("http://localhost:18083/user/{id}", 42)
                .retrieve()
                .body(String.class);
        return "remote " + body;
    }

    @GetMapping("/throw")
    public String error() {
        return testService.observedError();
    }
}
