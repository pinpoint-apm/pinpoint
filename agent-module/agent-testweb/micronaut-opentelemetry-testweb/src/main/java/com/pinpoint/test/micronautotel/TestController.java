package com.pinpoint.test.micronautotel;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

@Controller("/")
@Produces(MediaType.TEXT_PLAIN)
public class TestController {

    private final HttpClient client;
    private final TestService testService;

    public TestController(@Client("http://localhost:18085") HttpClient client, TestService testService) {
        this.client = client;
        this.testService = testService;
    }

    @Get("/helloworld")
    public String helloworld() {
        return "helloworld";
    }

    @Get("/user/{id}")
    public String user(@PathVariable String id) {
        return "user " + id;
    }

    @Get("/observed")
    public String observed() {
        return "observed " + testService.observedHello();
    }

    @Get("/remote")
    @ExecuteOn(TaskExecutors.BLOCKING) // a blocking client call is not allowed on the netty event loop
    public String remote() {
        // self call: server span -> http client span -> server span (context propagated via W3C traceparent)
        return "remote " + client.toBlocking().retrieve("/user/42");
    }

    @Get("/throw")
    public String error() {
        throw new IllegalStateException("observed error");
    }
}
