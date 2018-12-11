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

package com.navercorp.pinpoint.plugin.common.servlet;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestWrapperAdaptor;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author jaehong.kim
 */
public class ServerRequestRecorderFactoryTest {
    private static final String RPC_NAME = "rpcName";
    private static final String END_POINT = "endPoint";
    private static final String REMOTE_ADDRESS = "remoteAddress";
    private static final String ACCEPTOR_HOST = "acceptorHost";
    private static final String GET_HEADER = "getHeader";

    @Test
    public void record() throws Exception {
        RequestAdaptor<ServerRequestWrapper> requestAdaptor = new ServerRequestWrapperAdaptor();
        final ServerRequestRecorder<ServerRequestWrapper> recorder = new ServerRequestRecorder<ServerRequestWrapper>(requestAdaptor);

        // SpanRecorder
        SpanRecorder spanRecorder = mock(SpanRecorder.class);

        recorder.record(spanRecorder, new MockServerRequestWrapper());
        verify(spanRecorder).recordRpcName(RPC_NAME);
        verify(spanRecorder).recordEndPoint(END_POINT);
        verify(spanRecorder).recordRemoteAddress(REMOTE_ADDRESS);
        verify(spanRecorder).recordAcceptorHost(GET_HEADER);
    }

    private class MockServerRequestWrapper implements ServerRequestWrapper {

        @Override
        public String getRpcName() {
            return RPC_NAME;
        }

        @Override
        public String getEndPoint() {
            return END_POINT;
        }

        @Override
        public String getRemoteAddress() {
            return REMOTE_ADDRESS;
        }

        @Override
        public String getAcceptorHost() {
            return ACCEPTOR_HOST;
        }

        @Override
        public String getHeader(String name) {
            return GET_HEADER;
        }
    }
}