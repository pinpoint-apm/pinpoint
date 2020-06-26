package com.navercorp.pinpoint.profiler.context.errorhandler;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collections;

public class ErrorHandlerBuilderTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void build_nested_parent() {

        IgnoreErrorHandler errorHandler = newErrorHandler(RuntimeException.class.getName(), true, true);

        // nested & parent
        Exception exception = new Exception(new NullPointerException());
        Assert.assertTrue(errorHandler.handleError(exception));
    }


    @Test
    public void build_nested_parent_not_error() {

        IgnoreErrorHandler errorHandler = newErrorHandler(RuntimeException.class.getName(), true, true);

        // nested & parent
        Exception exception = new Exception(new Exception());
        Assert.assertFalse(errorHandler.handleError(exception));
    }

    @Test
    public void build_nested() {

        IgnoreErrorHandler errorHandler = newErrorHandler(RuntimeException.class.getName(), true, false);

        // nested
        Exception exception = new Exception(new RuntimeException());
        Assert.assertTrue(errorHandler.handleError(exception));
    }

    @Test
    public void build_parent() {

        IgnoreErrorHandler errorHandler = newErrorHandler(RuntimeException.class.getName(), false, true);

        // parent
        Exception exception = new IllegalArgumentException(new Exception());
        Assert.assertTrue(errorHandler.handleError(exception));
    }

    @Test
    public void build_parent2() {

        IgnoreErrorHandler errorHandler = newErrorHandler(RuntimeException.class.getName(), false, true);

        // parent
        Exception exception = new SQLException(new Exception());
        Assert.assertFalse(errorHandler.handleError(exception));
    }


    private IgnoreErrorHandler newErrorHandler(String exceptionClass, boolean nested, boolean parent) {
        Descriptor descriptor = new Descriptor("testHandler", Collections.singletonList(exceptionClass), Collections.<String>emptyList(), nested, parent);
        ErrorHandlerBuilder errorHandlerBuilder = new ErrorHandlerBuilder(Collections.singletonList(descriptor));
        return errorHandlerBuilder.build();
    }

}