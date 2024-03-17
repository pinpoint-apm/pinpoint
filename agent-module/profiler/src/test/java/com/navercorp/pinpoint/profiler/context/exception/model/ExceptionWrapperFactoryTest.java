package com.navercorp.pinpoint.profiler.context.exception.model;


import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.exception.sampler.ExceptionChainSampler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author intr3p1d
 */
class ExceptionWrapperFactoryTest {

    private final static int MAX_DEPTH = 5;
    private final static int MAX_LENGTH = 10;
    private final static int ANY_INT = 1;
    ExceptionWrapperFactory factory = new ExceptionWrapperFactory(MAX_DEPTH, MAX_LENGTH);
    ExceptionChainSampler sampler = new ExceptionChainSampler(1000);

    @Test
    public void testErrorMessageLimitWorks() {
        String errorMessage = "Message that exceed 10 characters";
        Throwable th = new RuntimeException(errorMessage);

        List<ExceptionWrapper> wrappers = new ArrayList<>();
        factory.addAllExceptionWrappers(wrappers, th, null, ANY_INT, ANY_INT, 0);

        String abbreviated = StringUtils.abbreviate(errorMessage, MAX_LENGTH);
        assertEquals(abbreviated, wrappers.get(0).getExceptionMessage());
        assertEquals(abbreviated.length(), wrappers.get(0).getExceptionMessage().length());
    }

    @Test
    public void testErrorMessageLimitWorks2() {
        String errorMessage = "Message";
        Throwable th = new RuntimeException(errorMessage);

        List<ExceptionWrapper> wrappers = new ArrayList<>();
        factory.addAllExceptionWrappers(wrappers, th, null, ANY_INT, ANY_INT, 0);
        String abbreviated = StringUtils.abbreviate(errorMessage, MAX_LENGTH);
        assertEquals(abbreviated, wrappers.get(0).getExceptionMessage());
        assertEquals(abbreviated.length(), wrappers.get(0).getExceptionMessage().length());
    }

    @Test
    public void testExceptionMaxDepthWorks() {
        Throwable th = new RuntimeException("initial throwable");
        for (int i = 0; i < 10; i++) {
            th = new RuntimeException(th);
        }

        List<ExceptionWrapper> wrappers = new ArrayList<>();
        factory.addAllExceptionWrappers(wrappers, th, null, ANY_INT, ANY_INT, 0);
        assertEquals(MAX_DEPTH, wrappers.size());
    }

    @Test
    public void testTraverseAndWrapMaxDepthWorks() {
        Throwable th = new RuntimeException("initial throwable");
        ExceptionContext context = new DefaultExceptionContext(null);
        ExceptionChainSampler.SamplingState samplingState = sampler.isNewSampled();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                th = new RuntimeException(th);
                context.update(th, ANY_INT, samplingState);
            }
        }

        List<ExceptionWrapper> wrappers = factory.newExceptionWrappers(context);
        assertEquals(MAX_DEPTH, wrappers.size());
    }

    @Test
    public void testDetailedStartTime() {
        Throwable th = new RuntimeException("initial throwable");
        ExceptionContext context = new DefaultExceptionContext(null);
        ExceptionChainSampler.SamplingState samplingState = sampler.isNewSampled();
        for (int i = 0; i < 5; i++) {
            th = new RuntimeException(th);
            context.update(th, ANY_INT + i, samplingState);
        }

        List<ExceptionWrapper> wrappers = factory.newExceptionWrappers(context);
        assertEquals(MAX_DEPTH, wrappers.size());

        for (int i = 0; i < 5; i++) {
            assertEquals(ANY_INT + i, wrappers.get(4 - i).getStartTime());
        }
    }

}