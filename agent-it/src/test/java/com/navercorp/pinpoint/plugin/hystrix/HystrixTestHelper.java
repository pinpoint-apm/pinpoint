/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.hystrix;

import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.netflix.hystrix.Hystrix;

import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author HyunGil Jeong
 */
public class HystrixTestHelper {

    public static RuntimeException INTERRUPTED_EXCEPTION_DUE_TO_TIMEOUT = new RuntimeException("expected interrupt");
    public static RuntimeException SHORT_CIRCUIT_EXCEPTION = new RuntimeException("Hystrix circuit short-circuited and is OPEN");

    public static void reset() {
        Hystrix.reset(60, TimeUnit.MILLISECONDS);
    }

    public static void waitForSpanDataFlush() throws InterruptedException {
        waitForSpanDataFlush(0);
    }

    public static void waitForSpanDataFlush(long additionalDelayMs) throws InterruptedException {
        Thread.sleep(100L + additionalDelayMs);
    }

    public static String sayHello(String name) {
        return String.format("Hello %s!", name);
    }

    public static String fallbackHello(String name) {
        return String.format("Fallback to %s", name);
    }

    public enum ExecutionOption {
        NORMAL,
        EXCEPTION,
        TIMEOUT,
        SHORT_CIRCUIT;
    }

    public static void verifyHystrixMetricsInitialization(PluginTestVerifier verifier, String command, String commandGroup) {
        ExpectedAnnotation commandKeyAnnotation = annotation("hystrix.command.key", command);
        ExpectedAnnotation commandGroupKeyAnnotation = annotation("hystrix.command.group.key", commandGroup);
        ExpectedAnnotation threadPoolKeyAnnotation = annotation("hystrix.thread.pool.key", commandGroup);
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "Hystrix Command Metrics Initialization", commandKeyAnnotation, commandGroupKeyAnnotation));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "Hystrix Circuit Breaker Initialization", commandKeyAnnotation, commandGroupKeyAnnotation));
        verifier.verifyTrace(event("HYSTRIX_COMMAND_INTERNAL", "Hystrix ThreadPool Metrics Initialization", threadPoolKeyAnnotation));
    }
}
