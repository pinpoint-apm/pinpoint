package com.navercorp.pinpoint.profiler.context.errorhandler;

import org.junit.Assert;
import org.junit.Test;

public class DefaultErrorHandlerTest {



    @Test
    public void handleError() {
        ThrowableMatcher classNameMatcher = new ClassNameThrowableMatcher(new String[] {"java.lang.RuntimeException"});
        MessageMatcher messageMatcher = new ContainsMessageMatcher(new String[] {"abc"});
        IgnoreErrorHandler errorHandler = new DefaultIgnoreErrorHandler("testHandler", classNameMatcher, messageMatcher);

        Assert.assertTrue(errorHandler.handleError(new RuntimeException("error abc")));

        Assert.assertFalse(errorHandler.handleError(new RuntimeException("success")));

    }
}