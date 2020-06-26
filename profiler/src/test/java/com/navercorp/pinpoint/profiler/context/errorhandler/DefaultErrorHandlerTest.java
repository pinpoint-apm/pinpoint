package com.navercorp.pinpoint.profiler.context.errorhandler;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class DefaultErrorHandlerTest {



    @Test
    public void handleError() {
        ThrowableMatcher classNameMatcher = new ClassNameThrowableMatcher(Collections.singletonList("java.lang.RuntimeException"));
        MessageMatcher messageMatcher = new ContainsMessageMatcher(Collections.singletonList("abc"));
        ErrorHandler errorHandler = new DefaultErrorHandler("testHandler", classNameMatcher, messageMatcher);

        Assert.assertTrue(errorHandler.handleError(new RuntimeException("error abc")));

        Assert.assertFalse(errorHandler.handleError(new RuntimeException("success")));

    }
}