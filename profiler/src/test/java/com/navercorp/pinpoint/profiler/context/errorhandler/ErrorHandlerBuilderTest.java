package com.navercorp.pinpoint.profiler.context.errorhandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;

public class ErrorHandlerBuilderTest {
    private final Logger logger = LogManager.getLogger(getClass());

    @Test
    public void build_nested_parent() {

        IgnoreErrorHandler errorHandler = newErrorHandler(RuntimeException.class.getName(), true, true);

        // nested & parent
        Exception exception = new Exception(new NullPointerException());
        Assertions.assertTrue(errorHandler.handleError(exception));
    }


    @Test
    public void build_nested_parent_not_error() {

        IgnoreErrorHandler errorHandler = newErrorHandler(RuntimeException.class.getName(), true, true);

        // nested & parent
        Exception exception = new Exception(new Exception());
        Assertions.assertFalse(errorHandler.handleError(exception));
    }

    @Test
    public void build_nested() {

        IgnoreErrorHandler errorHandler = newErrorHandler(RuntimeException.class.getName(), true, false);

        // nested
        Exception exception = new Exception(new RuntimeException());
        Assertions.assertTrue(errorHandler.handleError(exception));
    }

    @Test
    public void build_parent() {

        IgnoreErrorHandler errorHandler = newErrorHandler(RuntimeException.class.getName(), false, true);

        // parent
        Exception exception = new IllegalArgumentException(new Exception());
        Assertions.assertTrue(errorHandler.handleError(exception));
    }

    @Test
    public void build_parent2() {

        IgnoreErrorHandler errorHandler = newErrorHandler(RuntimeException.class.getName(), false, true);

        // parent
        Exception exception = new SQLException(new Exception());
        Assertions.assertFalse(errorHandler.handleError(exception));
    }


    private IgnoreErrorHandler newErrorHandler(String exceptionClass, boolean nested, boolean parent) {
        Descriptor descriptor = new Descriptor("testHandler", Collections.singletonList(exceptionClass), Collections.emptyList(), nested, parent);
        ErrorHandlerBuilder errorHandlerBuilder = new ErrorHandlerBuilder(Collections.singletonList(descriptor));
        return errorHandlerBuilder.build();
    }

}