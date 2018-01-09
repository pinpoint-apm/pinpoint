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

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHandle;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.id.DefaultTransactionIdEncoder;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TransactionIdEncoder;
import com.navercorp.pinpoint.profiler.context.recorder.DefaultSpanRecorder;
import com.navercorp.pinpoint.profiler.context.recorder.WrappedSpanEventRecorder;
import com.navercorp.pinpoint.profiler.context.storage.SpanStorage;
import com.navercorp.pinpoint.profiler.logging.Slf4jLoggerBinderInitializer;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataService;
import com.navercorp.pinpoint.profiler.sender.LoggingDataSender;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

/**
 * @author emeroad
 */
@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class DefaultTraceTest {

    private final String agentId = "agentId";
    private final long agentStartTime = System.currentTimeMillis();
    private final TransactionIdEncoder encoder = new DefaultTransactionIdEncoder(agentId, agentStartTime);

    @Mock
    private TraceRoot traceRoot;
    @Mock
    private Shared shared;
    @Mock
    private StringMetaDataService stringMetaDataService;
    @Mock
    private SqlMetaDataService sqlMetaDataService;
    @Mock
    private AsyncContextFactory asyncContextFactory;

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
        when(traceRoot.getShared()).thenReturn(shared);

        TraceId traceId = new DefaultTraceId(agentId, System.currentTimeMillis(), 0);
        when(traceRoot.getTraceId()).thenReturn(traceId);

        CallStackFactory callStackFactory = new CallStackFactoryV1(64);
        CallStack callStack = callStackFactory.newCallStack(traceRoot);

        SpanFactory spanFactory = new DefaultSpanFactory("appName", agentId, 0, ServiceType.STAND_ALONE, encoder);

        SpanStorage storage = new SpanStorage(traceRoot, LoggingDataSender.DEFAULT_LOGGING_DATA_SENDER);

        final Span span = spanFactory.newSpan(traceRoot);
        final boolean root = span.getTraceRoot().getTraceId().isRoot();
        final SpanRecorder spanRecorder = new DefaultSpanRecorder(span, root, true, stringMetaDataService, sqlMetaDataService);
        final WrappedSpanEventRecorder wrappedSpanEventRecorder = new WrappedSpanEventRecorder(asyncContextFactory, stringMetaDataService, sqlMetaDataService, null);

        Trace trace = new DefaultTrace(span, callStack, storage, asyncContextFactory, true, spanRecorder, wrappedSpanEventRecorder, ActiveTraceHandle.EMPTY_HANDLE);
        trace.traceBlockBegin();
        trace.traceBlockBegin();
        trace.traceBlockEnd();
        trace.traceBlockEnd();
        trace.close();
    }
}
