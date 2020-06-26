package com.navercorp.pinpoint.profiler.context.errorhandler;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;

public class NestedErrorHandlerTest {

    @Test
    public void handleError() {

        ThrowableMatcher throwableMatcher = new ClassNameThrowableMatcher(Collections.singletonList("java.sql.SQLException"));
        ErrorHandler errorHandler = new DefaultErrorHandler("test", throwableMatcher, EmptyMessageMatcher.EMPTY_MESSAGE_MATCHER);
        ErrorHandler nestedErrorHandler = new NestedErrorHandler(errorHandler);

        SQLException sqlException = new SQLException("sql error");
        Exception rootException = new RuntimeException("nested error ", sqlException);
        // fail
        Assert.assertFalse(errorHandler.handleError(rootException));
        // success
        Assert.assertTrue(nestedErrorHandler.handleError(rootException));

    }

    @Test
    public void handleError_nested_not_error() {

        ThrowableMatcher throwableMatcher = new ClassNameThrowableMatcher(Collections.singletonList("com.pinpoint.MyExcpetion"));
        ErrorHandler errorHandler = new DefaultErrorHandler("test", throwableMatcher, EmptyMessageMatcher.EMPTY_MESSAGE_MATCHER);
        ErrorHandler nestedErrorHandler = new NestedErrorHandler(errorHandler);

        SQLException sqlException = new SQLException("sql error");
        Exception rootException = new RuntimeException("nested error ", sqlException);
        // not found
        Assert.assertFalse(nestedErrorHandler.handleError(rootException));

    }
}