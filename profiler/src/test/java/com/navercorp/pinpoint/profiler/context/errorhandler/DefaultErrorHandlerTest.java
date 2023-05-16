package com.navercorp.pinpoint.profiler.context.errorhandler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DefaultErrorHandlerTest {


    @Test
    public void handleError() {
        ThrowableMatcher classNameMatcher = new ClassNameThrowableMatcher(new String[]{"java.lang.RuntimeException"});
        MessageMatcher messageMatcher = new ContainsMessageMatcher(new String[]{"abc"});
        IgnoreErrorHandler errorHandler = new DefaultIgnoreErrorHandler("testHandler", classNameMatcher, messageMatcher);

        Assertions.assertTrue(errorHandler.handleError(new RuntimeException("error abc")));

        Assertions.assertFalse(errorHandler.handleError(new RuntimeException("success")));

    }
}