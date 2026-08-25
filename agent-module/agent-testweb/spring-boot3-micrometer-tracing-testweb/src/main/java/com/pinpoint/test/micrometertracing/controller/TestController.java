package com.pinpoint.test.micrometertracing.controller;

import com.pinpoint.test.micrometertracing.service.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class TestController {

    private final TestService testService;
    private final RestClient restClient;

    public TestController(TestService testService, RestClient.Builder restClientBuilder) {
        this.testService = testService;
        // RestClient.Builder provided by Boot is observation-instrumented -> HTTP client span
        this.restClient = restClientBuilder.build();
    }

    @GetMapping("/helloworld")
    public String helloworld() {
        return "helloworld";
    }

    @GetMapping("/observed")
    public String observed() {
        return "observed " + testService.observedHello();
    }

    @GetMapping("/remote")
    public String remote() {
        // self call: server span -> http client span -> server span (context propagated via W3C traceparent)
        String body = restClient.get()
                .uri("http://localhost:18080/helloworld")
                .retrieve()
                .body(String.class);
        return "remote " + body;
    }

    @GetMapping("/sleep")
    public String sleep() throws InterruptedException {
        Thread.sleep(1000);
        return "sleep 1000ms";
    }

    @GetMapping("/throw")
    public String error() {
        return testService.observedError();
    }
}
