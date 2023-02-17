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

import akka.http.scaladsl.marshalling.ToResponseMarshallable;
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
public class RequestContextImplCompleteInterceptorTest {
    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private SpanEventRecorder recorder;

    @Mock
    private ToResponseMarshallable marshallable;

    private RequestContextImplCompleteInterceptor interceptor;

    @BeforeEach
    public void setUp() throws Exception {
        interceptor = new RequestContextImplCompleteInterceptor(traceContext, descriptor);
    }

    @Test
    public void doInBeforeTrace() {
        interceptor.doInBeforeTrace(recorder, null, new Object[]{marshallable});
    }

    @Test
    public void doInAfterTrace() {
        interceptor.doInAfterTrace(recorder, null, null, null, null);
        verify(recorder).recordApi(descriptor);
        verify(recorder).recordServiceType(AkkaHttpConstants.AKKA_HTTP_SERVER_INTERNAL);
        verify(recorder).recordException(null);
    }
}
