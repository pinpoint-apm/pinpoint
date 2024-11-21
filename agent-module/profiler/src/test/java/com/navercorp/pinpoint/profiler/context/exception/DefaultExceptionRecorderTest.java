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
package com.navercorp.pinpoint.profiler.context.exception;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.profiler.context.exception.model.DefaultExceptionContext;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionContext;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionContextValue;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionWrapper;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionWrapperFactory;
import com.navercorp.pinpoint.profiler.context.exception.sampler.ExceptionChainSampler;
import com.navercorp.pinpoint.profiler.context.exception.storage.ExceptionStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author intr3p1d
 */
public class DefaultExceptionRecorderTest {

    private final static Logger logger = LogManager.getLogger(DefaultExceptionRecorderTest.class);
    private final static long START_TIME = 1;

    private ExceptionWrapperFactory exceptionWrapperFactory;
    private TestExceptionStorage exceptionStorage;
    private ExceptionContext context;
    private DefaultExceptionRecorder exceptionRecorder;

    @BeforeEach
    void setUp() {
        ExceptionChainSampler exceptionChainSampler = new ExceptionChainSampler(100000);
        exceptionWrapperFactory = new ExceptionWrapperFactory(10, 1048);
        exceptionStorage = new TestExceptionStorage();
        context = new DefaultExceptionContext(exceptionStorage);
        exceptionRecorder = new DefaultExceptionRecorder(
                exceptionChainSampler, exceptionWrapperFactory, context
        );
    }


    static class TestExceptionStorage implements ExceptionStorage {

        List<ExceptionWrapper> wrappers;
        public List<ExceptionWrapper> outputStream;

        public TestExceptionStorage() {
            this.wrappers = new ArrayList<>();
            this.outputStream = new ArrayList<>();
        }

        @Override
        public void store(List<ExceptionWrapper> wrappers) {
            logger.info(wrappers);
            this.wrappers.addAll(wrappers);
        }

        @Override
        public void flush() {
            final List<ExceptionWrapper> copy = new ArrayList<>(wrappers);
            if (CollectionUtils.hasLength(copy)) {
                outputStream.addAll(copy);
            }
            wrappers.clear();
        }

        @Override
        public void close() {
            this.outputStream.clear();
        }

        public List<ExceptionWrapper> getWrappers() {
            return this.wrappers;
        }

        public List<ExceptionWrapper> getOutputStream() {
            return outputStream;
        }
    }


    @Test
    void testRecordNothing() {
        Throwable firstOne = null;
        List<ExceptionWrapper> expected = Collections.emptyList();

        exceptionRecorder.recordThrowable(null, START_TIME);

        exceptionStorage.flush();
        List<ExceptionWrapper> actual = exceptionStorage.getOutputStream();

        Assertions.assertTrue(actual.isEmpty());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testRecordException() {
        Throwable firstOne = new RuntimeException("level 1 Error");
        Throwable secondOne = new RuntimeException("level 2 Error", firstOne);
        Throwable thirdOne = new RuntimeException("level 3 Error", secondOne);

        exceptionRecorder.recordThrowable(firstOne, START_TIME);
        exceptionRecorder.recordThrowable(thirdOne, START_TIME + 1);
        List<ExceptionWrapper> expected = exceptionWrapperFactory.newExceptionWrappers(context);

        exceptionRecorder.close();

        List<ExceptionWrapper> actual = exceptionStorage.getOutputStream();
        Assertions.assertFalse(actual.isEmpty());
        Assertions.assertEquals(3, actual.size());
        Assertions.assertEquals(expected, actual);
    }


    @Test
    void testRecordNotChainedException() {
        Throwable firstOne = new RuntimeException("level 1 Error");
        Throwable secondOne = new RuntimeException("level 2 Error", firstOne);
        Throwable thirdOne = new RuntimeException("level 3 Error", secondOne);
        Throwable notChained = new RuntimeException("This is not chained with level 3 Error");

        exceptionRecorder.recordThrowable(firstOne, START_TIME);
        exceptionRecorder.recordThrowable(thirdOne, START_TIME + 1);
        List<ExceptionWrapper> expected = exceptionWrapperFactory.newExceptionWrappers(context);

        exceptionRecorder.recordThrowable(notChained, START_TIME + 2);
        List<ExceptionWrapper> added = exceptionWrapperFactory.newExceptionWrappers(context);

        List<ExceptionWrapper> actual1 = exceptionStorage.getWrappers();
        Assertions.assertFalse(actual1.isEmpty());
        Assertions.assertEquals(3, actual1.size());
        Assertions.assertEquals(expected, actual1);

        exceptionRecorder.close();

        expected.addAll(added);
        List<ExceptionWrapper> actual2 = exceptionStorage.getOutputStream();
        Assertions.assertFalse(actual2.isEmpty());
        Assertions.assertEquals(4, actual2.size());
        Assertions.assertEquals(expected, actual2);
    }

    @Test
    void testRecordRethrownException() {

        Throwable firstOne = new RuntimeException("level 1 Error");
        Throwable secondOne = new RuntimeException("level 2 Error", firstOne);
        Throwable thirdOne = new RuntimeException("level 3 Error", secondOne);
        Throwable rethrown = thirdOne; // Rethrown Exception

        exceptionRecorder.recordThrowable(firstOne, START_TIME);
        exceptionRecorder.recordThrowable(thirdOne, START_TIME + 1);
        List<ExceptionWrapper> expected = exceptionWrapperFactory.newExceptionWrappers(context);

        exceptionRecorder.recordThrowable(rethrown, START_TIME + 2);

        List<ExceptionWrapper> actual1 = exceptionStorage.getWrappers();
        Assertions.assertTrue(actual1.isEmpty());

        exceptionRecorder.close();

        List<ExceptionWrapper> actual2 = exceptionStorage.getOutputStream();
        Assertions.assertFalse(actual2.isEmpty());
        Assertions.assertEquals(3, actual2.size());
        Assertions.assertEquals(expected, actual2);
    }

}
