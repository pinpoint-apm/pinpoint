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
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.id.AsyncIdGenerator;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.recorder.DefaultRecorderFactory;
import com.navercorp.pinpoint.profiler.context.recorder.RecorderFactory;
import com.navercorp.pinpoint.profiler.context.storage.SpanStorage;
import com.navercorp.pinpoint.profiler.logging.Slf4jLoggerBinderInitializer;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataService;
import com.navercorp.pinpoint.profiler.sender.LoggingDataSender;

import org.junit.*;

import static org.mockito.Mockito.mock;

/**
 * @author emeroad
 */
public class DefaultTraceTest {

    @BeforeClass
    public static void before() throws Exception {
        Slf4jLoggerBinderInitializer.beforeClass();
    }

    @AfterClass
    public static void after()  throws Exception {
        Slf4jLoggerBinderInitializer.afterClass();
    }


    @Test
    public void testPushPop() {

        CallStackFactory callStackFactory = new DefaultCallStackFactory(64);
        SpanFactory spanFactory = new DefaultSpanFactory("appName", "agentId", 0, ServiceType.STAND_ALONE);

        StringMetaDataService stringMetaDataService = mock(StringMetaDataService.class);
        SqlMetaDataService sqlMetaDataService = mock(SqlMetaDataService.class);
        RecorderFactory recorderFactory = new DefaultRecorderFactory(stringMetaDataService, sqlMetaDataService);

        AsyncIdGenerator asyncIdGenerator = mock(AsyncIdGenerator.class);
        SpanStorage storage = new SpanStorage(LoggingDataSender.DEFAULT_LOGGING_DATA_SENDER);
        long localTransactionId = 1;
        TraceId traceId = new DefaultTraceId("agentId", System.currentTimeMillis(), localTransactionId);
        Trace trace = new DefaultTrace(callStackFactory, storage, traceId, localTransactionId, asyncIdGenerator, true,
                spanFactory, recorderFactory);
        trace.traceBlockBegin();
        trace.traceBlockBegin();
        trace.traceBlockEnd();
        trace.traceBlockEnd();
        trace.close();
    }
}
