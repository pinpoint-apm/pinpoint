/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.openwhisk.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.plugin.openwhisk.OpenwhiskConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class ContainerPoolBalancerProcessActiveAckInterceptorTest {
    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private SpanRecorder recorder;

    @Mock
    private SpanEventRecorder eventRecorder;

    @Mock
    private ProfilerConfig profilerConfig;

    @Mock
    private Trace trace;

    @Mock
    private TraceId traceId;


    private final String PINPOINT_HEADER_DELIMITIER = "@";
    private final String PINPOINT_HEADER_PREFIX = PINPOINT_HEADER_DELIMITIER + "pinpoint_start" + PINPOINT_HEADER_DELIMITIER;
    private final String PINPOINT_HEADER_POSTFIX = PINPOINT_HEADER_DELIMITIER + "pinpoint_end";

    private ContainerPoolBalancerProcessActiveAckInterceptor interceptor;

    @Before
    public void setUp() throws Exception {
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        doReturn(true).when(trace).canSampled();
        doReturn(traceId).when(traceContext).createTraceId(anyString() , anyLong() , anyLong() , anyShort());
        doReturn(trace).when(traceContext).continueAsyncTraceObject(traceId);
        doReturn(trace).when(traceContext).currentRawTraceObject();
        doReturn(recorder).when(trace).getSpanRecorder();
        doReturn(eventRecorder).when(trace).traceBlockBegin();
        doReturn(eventRecorder).when(trace).currentSpanEventRecorder();
        interceptor = new ContainerPoolBalancerProcessActiveAckInterceptor(traceContext, descriptor);
    }

    @Test
    public void before() {
        interceptor.before("TEST", new Object[]{createPinpointHeader()});
        verify(eventRecorder).recordApi(descriptor);
        verify(eventRecorder).recordServiceType(OpenwhiskConstants.OPENWHISK_INTERNAL);
    }

    @Test
    public void after() {
        interceptor.after("TEST", new Object[]{} , null, null);
        verify(eventRecorder).recordException(null);
    }

    public byte[] createPinpointHeader() {
        byte[] pinpointHeader = new StringBuffer().append(PINPOINT_HEADER_PREFIX)
                .append("^1517980646491").append(PINPOINT_HEADER_DELIMITIER)
                .append("-3022580112884983244").append(PINPOINT_HEADER_DELIMITIER)
                .append("-1918180025536006038").append(PINPOINT_HEADER_DELIMITIER)
                .append("TEST").append(PINPOINT_HEADER_DELIMITIER)
                .append("0").append(PINPOINT_HEADER_DELIMITIER)
                .append("0").append(PINPOINT_HEADER_DELIMITIER)
                .append(PINPOINT_HEADER_POSTFIX).toString().getBytes();
        return pinpointHeader;
    }


}
