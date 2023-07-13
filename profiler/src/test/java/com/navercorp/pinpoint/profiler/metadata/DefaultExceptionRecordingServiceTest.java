/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.profiler.context.exception.DefaultExceptionRecordingService;
import com.navercorp.pinpoint.profiler.context.exception.model.DefaultExceptionContext;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionContext;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionWrapper;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionWrapperFactory;
import com.navercorp.pinpoint.profiler.context.exception.sampler.ExceptionTraceSampler;
import com.navercorp.pinpoint.profiler.context.exception.storage.ExceptionStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author intr3p1d
 */
public class DefaultExceptionRecordingServiceTest {

    private final static Logger logger = LogManager.getLogger(DefaultExceptionRecordingServiceTest.class);

    ExceptionTraceSampler exceptionTraceSampler = new ExceptionTraceSampler(1000);
    ExceptionWrapperFactory exceptionWrapperFactory = new ExceptionWrapperFactory(10);
    DefaultExceptionRecordingService exceptionRecordingService = new DefaultExceptionRecordingService(
            exceptionTraceSampler, exceptionWrapperFactory
    );

    List<Throwable> exceptions = new ArrayList<>();
    TestExceptionStorage exceptionStorage = new TestExceptionStorage();

    ExceptionContext context = new DefaultExceptionContext(exceptionStorage);

    long START_TIME = 1;

    class TestExceptionStorage implements ExceptionStorage {

        List<ExceptionWrapper> wrappers;

        public TestExceptionStorage() {
            this.wrappers = new ArrayList<>();
        }

        @Override
        public void store(List<ExceptionWrapper> wrappers) {
            this.wrappers.addAll(wrappers);
        }

        @Override
        public void flush() {
            this.wrappers.clear();
        }

        @Override
        public void close() {
        }

        public List<ExceptionWrapper> getWrappers() {
            return this.wrappers;
        }
    }

    private void resetContext() {
        exceptionStorage.flush();
        exceptions.clear();
        context = new DefaultExceptionContext(exceptionStorage);
    }

    private Function<Throwable, Throwable> throwableFunction = (Throwable th) -> {
        record(th);
        exceptions.add(th);
        logger.info(th);
        return th;
    };


    private void record(Throwable throwable) {
        exceptionRecordingService.recordException(context, throwable, START_TIME);
    }

    @Test
    public void testRecordNothing() {
        resetContext();

        exceptionRecordingService.recordException(context, null, 0);

        List<ExceptionWrapper> expected = new ArrayList<>();
        List<ExceptionWrapper> actual = exceptionStorage.getWrappers();

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testRecordException() {
        resetContext();
        List<ExceptionWrapper> expected = null;
        List<ExceptionWrapper> actual = null;

        try {
            level3Error(throwableFunction);
        } catch (Throwable e) {
            expected = exceptionWrapperFactory.newExceptionWrappers(e, START_TIME, context.getExceptionId());
            exceptionRecordingService.recordException(context, e, START_TIME);
            actual = exceptionStorage.getWrappers();
            Assertions.assertTrue(actual.isEmpty());
        }
        exceptionRecordingService.recordException(context, null, START_TIME);
        actual = exceptionStorage.getWrappers();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testRecordNotChainedException() {
        resetContext();
        List<ExceptionWrapper> expected1 = null;
        List<ExceptionWrapper> expected2 = null;
        List<ExceptionWrapper> actual1 = null;
        List<ExceptionWrapper> actual2 = null;

        Throwable throwable = null;

        try {
            notChainedException(throwableFunction);
        } catch (Throwable e) {
            expected1 = exceptionWrapperFactory.newExceptionWrappers(exceptions.get(exceptions.size() - 2), START_TIME, context.getExceptionId());
            exceptionRecordingService.recordException(context, e, START_TIME);
            actual1 = exceptionStorage.getWrappers();
            throwable = e;
            Assertions.assertFalse(actual1.isEmpty());
            Assertions.assertEquals(expected1, actual1);
        }

        exceptionStorage.flush();
        expected2 = exceptionWrapperFactory.newExceptionWrappers(throwable, START_TIME, context.getExceptionId());
        exceptionRecordingService.recordException(context, null, 0);
        actual2 = exceptionStorage.getWrappers();
        Assertions.assertEquals(expected2, actual2);
    }

    @Test
    public void testRecordRethrowGivenException() {
        resetContext();
        List<ExceptionWrapper> expected = null;
        List<ExceptionWrapper> actual = null;

        try {
            rethrowGivenException(throwableFunction);
        } catch (Throwable e) {
            expected = exceptionWrapperFactory.newExceptionWrappers(e, START_TIME, context.getExceptionId());
            exceptionRecordingService.recordException(context, e, START_TIME);
            actual = exceptionStorage.getWrappers();
            Assertions.assertTrue(actual.isEmpty());
        }

        exceptionRecordingService.recordException(context, null, 0);
        actual = exceptionStorage.getWrappers();
        Assertions.assertEquals(expected, actual);
    }


    public void notChainedException(Function<Throwable, Throwable> interceptor) throws Throwable {
        try {
            level3Error(interceptor);
        } catch (Throwable e) {
            throw interceptor.apply(new RuntimeException("Not Chained, Another New Exception"));
        }
    }

    public void rethrowGivenException(Function<Throwable, Throwable> interceptor) throws Throwable {
        try {
            level3Error(interceptor);
        } catch (Exception e) {
            throw interceptor.apply(e);
        }
    }

    public void level3Error(Function<Throwable, Throwable> interceptor) throws Throwable {
        try {
            level2Error(interceptor);
        } catch (Throwable e) {
            throw interceptor.apply(new RuntimeException("Level 3 Error", e));
        }
    }

    public void level2Error(Function<Throwable, Throwable> interceptor) throws Throwable {
        try {
            level1Error(interceptor);
        } catch (Throwable e) {
            throw interceptor.apply(new RuntimeException("Level 2 Error", e));
        }
    }

    public void level1Error(Function<Throwable, Throwable> interceptor) throws Throwable {
        throw interceptor.apply(new RuntimeException("Level 1 Error"));
    }

}
