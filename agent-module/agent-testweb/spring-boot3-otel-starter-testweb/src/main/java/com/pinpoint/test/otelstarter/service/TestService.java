package com.pinpoint.test.otelstarter.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    @WithSpan("observed-hello")
    public String observedHello() {
        return "hello";
    }

    @WithSpan("observed-error")
    public String observedError() {
        throw new IllegalStateException("observed error");
    }
}
