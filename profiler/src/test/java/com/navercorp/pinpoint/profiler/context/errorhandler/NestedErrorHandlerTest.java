package com.navercorp.pinpoint.profiler.context.errorhandler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class NestedErrorHandlerTest {

    @Test
    public void handleError() {

        ThrowableMatcher throwableMatcher = new ClassNameThrowableMatcher(new String[]{SQLException.class.getName()});
        IgnoreErrorHandler errorHandler = new DefaultIgnoreErrorHandler("test", throwableMatcher, new AlwaysMessageMatcher());
        IgnoreErrorHandler nestedErrorHandler = new NestedErrorHandler(errorHandler);

        SQLException sqlException = new SQLException("sql error");
        Exception rootException = new RuntimeException("nested error ", sqlException);
        // fail
        Assertions.assertFalse(errorHandler.handleError(rootException));
        // success
        Assertions.assertTrue(nestedErrorHandler.handleError(rootException));

    }

    @Test
    public void handleError_nested_not_error() {

        ThrowableMatcher throwableMatcher = new ClassNameThrowableMatcher(new String[]{"com.pinpoint.MyExcpetion"});
        IgnoreErrorHandler errorHandler = new DefaultIgnoreErrorHandler("test", throwableMatcher, new AlwaysMessageMatcher());
        IgnoreErrorHandler nestedErrorHandler = new NestedErrorHandler(errorHandler);

        SQLException sqlException = new SQLException("sql error");
        Exception rootException = new RuntimeException("nested error ", sqlException);
        // not found
        Assertions.assertFalse(nestedErrorHandler.handleError(rootException));

    }

//    @Test
//    public void handleError_depth_infinite() {
//        Exception root = new RuntimeException();
//        Exception l1 = new RuntimeException(root);
//        Exception l2 = new RuntimeException(l1);
//        root.initCause(l2);
//
//        NestedErrorHandler errorHandler = new NestedErrorHandler(new BypassErrorHandler());
//
//        Assertions.assertFalse(errorHandler.handleError(root));
//        // success
//
//    }
}