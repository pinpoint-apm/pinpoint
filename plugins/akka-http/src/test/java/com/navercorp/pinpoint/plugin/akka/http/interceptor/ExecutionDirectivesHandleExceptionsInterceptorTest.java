/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.akka.http.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class ExecutionDirectivesHandleExceptionsInterceptorTest {
    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private SpanEventRecorder recorder;

    @Mock
    private ProfilerConfig profilerConfig;

    @Mock
    private Trace trace;

    private ExecutionDirectivesHandleExceptionsInterceptor interceptor;

    @Before
    public void setUp() throws Exception {
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        doReturn(recorder).when(trace).currentSpanEventRecorder();
        doReturn(true).when(trace).canSampled();
        doReturn(trace).when(traceContext).currentRawTraceObject();
        interceptor = new ExecutionDirectivesHandleExceptionsInterceptor(traceContext, descriptor);
    }

    @Test
    public void after() {
        interceptor.after(null, null, null, null);
        verify(recorder).recordApi(descriptor);
        verify(recorder).recordException(null);
    }
}
