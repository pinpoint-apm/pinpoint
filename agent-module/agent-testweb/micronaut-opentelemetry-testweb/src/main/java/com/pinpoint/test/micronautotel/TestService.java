package com.pinpoint.test.micronautotel;

import io.micronaut.tracing.annotation.NewSpan;
import jakarta.inject.Singleton;

@Singleton
public class TestService {

    @NewSpan("observed-hello")
    public String observedHello() {
        return "hello";
    }
}
