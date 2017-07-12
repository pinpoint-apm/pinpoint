/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver;

import com.navercorp.pinpoint.collector.handler.RequestResponseHandler;
import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.thrift.dto.TResult;
import org.apache.thrift.TBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Taejin Koo
 */
public class DispatchHandlerTest {

    private static final int MAX_HANDLER_COUNT = 5;

    private static final TestSimpleHandler TEST_SIMPLE_HANDLER = new TestSimpleHandler();
    private static final TestRequestHandler TEST_REQUEST_HANDLER = new TestRequestHandler();
    @InjectMocks
    TestDispatchHandler testDispatchHandler = new TestDispatchHandler();
    @Mock
    private AcceptedTimeService acceptedTimeService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void throwExceptionTest1() {
        testDispatchHandler.dispatchSendMessage(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void throwExceptionTest2() {
        testDispatchHandler.dispatchRequestMessage(null);
    }

    @Test
    public void dispatchSendMessageTest() {
        testDispatchHandler.dispatchSendMessage(new TResult());

        Assert.assertTrue(TEST_SIMPLE_HANDLER.getExecutedCount() > 0);
    }

    @Test
    public void dispatchRequestMessageTest() {
        testDispatchHandler.dispatchRequestMessage(new TResult());

        Assert.assertTrue(TEST_REQUEST_HANDLER.getExecutedCount() > 0);
    }

    private static class TestDispatchHandler extends AbstractDispatchHandler {

        @Override
        protected List<SimpleHandler> getSimpleHandler(TBase<?, ?> tBase) {
            if (tBase == null) {
                return Collections.emptyList();
            }

            int random = ThreadLocalRandom.current().nextInt(1, MAX_HANDLER_COUNT);
            List<SimpleHandler> handlerList = new ArrayList<>(random);
            for (int i = 0; i < random; i++) {
                handlerList.add(TEST_SIMPLE_HANDLER);
            }

            return handlerList;
        }

        @Override
        protected RequestResponseHandler getRequestResponseHandler(TBase<?, ?> tBase) {
            if (tBase == null) {
                return null;
            }

            return TEST_REQUEST_HANDLER;
        }

    }

    private static class TestSimpleHandler implements SimpleHandler {

        private int executedCount = 0;

        @Override
        public void handleSimple(TBase<?, ?> tbase) {
            executedCount++;
        }

        public int getExecutedCount() {
            return executedCount;
        }

    }

    private static class TestRequestHandler implements RequestResponseHandler {

        private int executedCount = 0;

        @Override
        public TBase<?, ?> handleRequest(TBase<?, ?> tbase) {
            executedCount++;
            return new TResult();
        }

        public int getExecutedCount() {
            return executedCount;
        }

    }

}
