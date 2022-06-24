/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.thrift.dto.TResult;
import org.apache.thrift.TBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void throwExceptionTest1() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            ServerRequest<TBase<?, ?>> request = mock(ServerRequest.class);
            when(request.getData()).thenReturn(null);
            testDispatchHandler.dispatchSendMessage(request);
        });
    }

    @Test
    public void dispatchSendMessageTest() {
        ServerRequest<TBase<?, ?>> serverRequest = mock(ServerRequest.class);
        when(serverRequest.getData()).thenReturn((TBase) new TResult());
        testDispatchHandler.dispatchSendMessage(serverRequest);

        Assertions.assertTrue(TEST_SIMPLE_HANDLER.getExecutedCount() > 0);
    }

    @Test
    public void dispatchRequestMessageTest() {
        ServerRequest<TBase<?, ?>> request = mock(ServerRequest.class);
        when(request.getData()).thenReturn((TBase) new TResult());

        ServerResponse<TBase<?, ?>> response = mock(ServerResponse.class);
        testDispatchHandler.dispatchRequestMessage(request, response);

        Assertions.assertTrue(TEST_REQUEST_HANDLER.getExecutedCount() > 0);
    }

    private static class TestDispatchHandler implements DispatchHandler<TBase<?, ?>, TBase<?, ?>> {

        @Override
        public void dispatchSendMessage(ServerRequest<TBase<?, ?>> serverRequest) {
            SimpleHandler<TBase<?, ?>> simpleHandler = getSimpleHandler(serverRequest);
            simpleHandler.handleSimple(serverRequest);
        }


        @Override
        public void dispatchRequestMessage(ServerRequest<TBase<?, ?>> serverRequest,
                                           ServerResponse<TBase<?, ?>> serverResponse) {
            RequestResponseHandler<TBase<?, ?>, TBase<?, ?>> requestResponseHandler = getRequestResponseHandler(serverRequest);
            requestResponseHandler.handleRequest(serverRequest, serverResponse);
        }

        private RequestResponseHandler<TBase<?, ?>, TBase<?, ?>> getRequestResponseHandler(ServerRequest<? extends TBase<?, ?>> serverRequest) {
            return TEST_REQUEST_HANDLER;
        }

        private SimpleHandler<TBase<?, ?>> getSimpleHandler(ServerRequest<? extends TBase<?, ?>> serverRequest) {
            final TBase<?, ?> data = serverRequest.getData();
            if (data instanceof TBase<?, ?>) {
                return getSimpleHandler(data);
            }

            throw new UnsupportedOperationException("data is not support type : " + data);
        }

        private SimpleHandler<TBase<?, ?>> getSimpleHandler(TBase<?, ?> tBase) {
            if (tBase == null) {
                return null;
            }

            return TEST_SIMPLE_HANDLER;
        }

    }

    private static class TestSimpleHandler implements SimpleHandler<TBase<?, ?>> {

        private int executedCount = 0;


        @Override
        public void handleSimple(ServerRequest<TBase<?, ?>> serverRequest) {
            final TBase<?, ?> data = serverRequest.getData();
            executedCount++;
        }

        public int getExecutedCount() {
            return executedCount;
        }

    }

    private static class TestRequestHandler implements RequestResponseHandler<TBase<?, ?>, TBase<?, ?>> {

        private int executedCount = 0;

        @Override
        public void handleRequest(ServerRequest<TBase<?, ?>> serverRequest, ServerResponse<TBase<?, ?>> serverResponse) {
            executedCount++;

            serverResponse.write(new TResult());
        }


        public int getExecutedCount() {
            return executedCount;
        }


    }

}
