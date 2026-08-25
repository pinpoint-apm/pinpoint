package com.pinpoint.test.boot4otel.service;

import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    @Observed(name = "test.observed", contextualName = "observed-hello")
    public String observedHello() {
        return "hello";
    }

    @Observed(name = "test.observed.error", contextualName = "observed-error")
    public String observedError() {
        throw new IllegalStateException("observed error");
    }
}
