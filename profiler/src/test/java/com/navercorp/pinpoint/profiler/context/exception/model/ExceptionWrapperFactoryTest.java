package com.navercorp.pinpoint.profiler.context.exception.model;


import com.navercorp.pinpoint.common.util.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author intr3p1d
 */
class ExceptionWrapperFactoryTest {

    private final static int MAX_DEPTH = 5;
    private final static int MAX_LENGTH = 10;
    private final static int ANY_INT = 1;
    ExceptionWrapperFactory factory = new ExceptionWrapperFactory(MAX_DEPTH, MAX_LENGTH);

    @Test
    public void testErrorMessageLimitWorks() {
        String errorMessage = "Message that exceed 10 characters";
        Throwable th = new RuntimeException(errorMessage);

        List<ExceptionWrapper> wrappers = factory.newExceptionWrappers(th, ANY_INT, ANY_INT);

        String abbreviated = StringUtils.abbreviate(errorMessage, MAX_LENGTH);
        Assertions.assertEquals(abbreviated, wrappers.get(0).getExceptionMessage());
        Assertions.assertEquals(abbreviated.length(), wrappers.get(0).getExceptionMessage().length());
    }

    @Test
    public void testErrorMessageLimitWorks2() {
        String errorMessage = "Message";
        Throwable th = new RuntimeException(errorMessage);

        List<ExceptionWrapper> wrappers = factory.newExceptionWrappers(th, ANY_INT, ANY_INT);

        String abbreviated = StringUtils.abbreviate(errorMessage, MAX_LENGTH);
        Assertions.assertEquals(abbreviated, wrappers.get(0).getExceptionMessage());
        Assertions.assertEquals(abbreviated.length(), wrappers.get(0).getExceptionMessage().length());
    }

    @Test
    public void testExceptionMaxDepthWorks(){
        Throwable th = new RuntimeException("inital throwable");
        for (int i = 0; i < 10; i++) {
            th = new RuntimeException(th);
        }

        List<ExceptionWrapper> wrappers = factory.newExceptionWrappers(th, ANY_INT, ANY_INT);
        Assertions.assertEquals(MAX_DEPTH, wrappers.size());
    }
}