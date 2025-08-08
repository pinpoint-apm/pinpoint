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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.akka.http.AkkaHttpConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RequestContextImplFailInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private SpanEventRecorder recorder;

    private Exception e = new Exception("Test");

    RequestContextImplFailInterceptor interceptor;

    @BeforeEach
    public void setUp() {
        interceptor = new RequestContextImplFailInterceptor(traceContext);
    }

    @Test
    public void doInBeforeTrace() {
        int apiId = 1;
        interceptor.doInBeforeTrace(recorder, null, apiId, new Object[]{e});
        verify(recorder).recordServiceType(AkkaHttpConstants.AKKA_HTTP_SERVER_INTERNAL);
        verify(recorder).recordException(e);
    }

    @Test
    public void doInAfterTrace() {
        int apiId = 1;
        interceptor.doInAfterTrace(recorder, null, apiId,null, null, null);
    }
}
