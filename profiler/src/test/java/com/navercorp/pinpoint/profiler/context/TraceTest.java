/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.profiler.context.storage.SpanStorage;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.LoggingDataSender;
import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.PinpointClientReconnectEventListener;

import org.apache.thrift.TBase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class TraceTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void trace() {
        TraceId traceId = new DefaultTraceId("agent", 0, 1);
        TraceContext traceContext = getDefaultTraceContext();
        SpanStorage storage = new SpanStorage(LoggingDataSender.DEFAULT_LOGGING_DATA_SENDER);

        Trace trace = new DefaultTrace(traceContext, storage, traceId, 0L, true);
        trace.traceBlockBegin();

        // get data form db
        getDataFromDB(trace);

        // response to client

        trace.traceBlockEnd();
    }


    @Test
    public void popEventTest() {
        TraceId traceId = new DefaultTraceId("agent", 0, 1);
        TraceContext traceContext = getDefaultTraceContext();

        TestDataSender dataSender = new TestDataSender();
        SpanStorage storage = new SpanStorage(LoggingDataSender.DEFAULT_LOGGING_DATA_SENDER);

        Trace trace = new DefaultTrace(traceContext, storage, traceId, 0L, true);

        trace.close();

        logger.debug(String.valueOf(dataSender.event));
    }

    private TraceContext getDefaultTraceContext() {
        ProfilerConfig profilerConfig = new DefaultProfilerConfig();
        return MockTraceContextFactory.newTestTraceContext(profilerConfig);
    }

    public class TestDataSender implements EnhancedDataSender {
        public boolean event;

        @Override
        public boolean send(TBase<?, ?> data) {
            event = true;
            return false;
        }

        @Override
        public void stop() {
        }

        @Override
        public boolean request(TBase<?, ?> data) {
            return send(data);
        }

        @Override
        public boolean request(TBase<?, ?> data, int retry) {
            return send(data);
        }

        @Override
        public boolean request(TBase<?, ?> data, FutureListener<ResponseMessage> listener) {
            return send(data);
        }

        @Override
        public boolean addReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
            return false;
        }

        @Override
        public boolean removeReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
            return false;
        }

    }

    private void getDataFromDB(Trace trace) {
        trace.traceBlockBegin();

        // db server request
        // get a db response
        trace.traceBlockEnd();
    }
}
