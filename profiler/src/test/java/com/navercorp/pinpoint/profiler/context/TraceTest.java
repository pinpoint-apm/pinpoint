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

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.DefaultTrace;
import com.navercorp.pinpoint.profiler.context.DefaultTraceContext;
import com.navercorp.pinpoint.profiler.context.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.storage.SpanStorage;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.LoggingDataSender;
import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.PinpointSocketReconnectEventListener;
import com.navercorp.pinpoint.test.TestAgentInformation;

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
        DefaultTraceId traceID = new DefaultTraceId("agent", 0, 1);
        DefaultTraceContext defaultTraceContext = getDefaultTraceContext();
        DefaultTrace trace = new DefaultTrace(defaultTraceContext , traceID, true);
        trace.setStorage(new SpanStorage(LoggingDataSender.DEFAULT_LOGGING_DATA_SENDER));
        trace.traceBlockBegin();

        // get data form db
        getDataFromDB(trace);

        // response to client

        trace.traceBlockEnd();
    }


    @Test
    public void popEventTest() {
        DefaultTraceId traceID = new DefaultTraceId("agent", 0, 1);
        DefaultTraceContext defaultTraceContext = getDefaultTraceContext();
        DefaultTrace trace = new DefaultTrace(defaultTraceContext, traceID, true);
        TestDataSender dataSender = new TestDataSender();
        trace.setStorage(new SpanStorage(LoggingDataSender.DEFAULT_LOGGING_DATA_SENDER));
        trace.close();

        logger.info(String.valueOf(dataSender.event));
    }

    private DefaultTraceContext getDefaultTraceContext() {
        DefaultTraceContext defaultTraceContext = new DefaultTraceContext(new TestAgentInformation());
        return defaultTraceContext;
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
        public boolean addReconnectEventListener(PinpointSocketReconnectEventListener eventListener) {
            return false;
        }

        @Override
        public boolean removeReconnectEventListener(PinpointSocketReconnectEventListener eventListener) {
            return false;
        }

        @Override
        public boolean isNetworkAvailable() {
            return true;
        }
    }

    private void getDataFromDB(Trace trace) {
        trace.traceBlockBegin();

        // db server request
        // get a db response
        trace.traceBlockEnd();
    }
}
