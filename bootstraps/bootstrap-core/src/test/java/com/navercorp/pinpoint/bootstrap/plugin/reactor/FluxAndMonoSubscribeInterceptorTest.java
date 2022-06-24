/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

public class FluxAndMonoSubscribeInterceptorTest {
    final TraceContext mockTraceContext = mock(TraceContext.class);
    final MethodDescriptor mockMethodDescriptor = mock(MethodDescriptor.class);
    final ServiceType mockServiceType = mock(ServiceType.class);

    @Test
    public void targetContainReactorContext() {
        AsyncContext mockAsyncContext = mock(AsyncContext.class);
        MockAsyncContextAndReactorContextImpl target = new MockAsyncContextAndReactorContextImpl();
        MockAsyncContextAndReactorContextImpl arg0 = new MockAsyncContextAndReactorContextImpl();
        FluxAndMonoSubscribeInterceptor interceptor = new FluxAndMonoSubscribeInterceptor(mockTraceContext, mockMethodDescriptor, mockServiceType);

        // Set asyncContext to target
        target._$PINPOINT$_setReactorContext(mockAsyncContext);
        // before
        AsyncContext asyncContext = interceptor.getAsyncContext(target, new Object[]{arg0});

        assertNull(asyncContext);
        assertNotNull(arg0._$PINPOINT$_getReactorContext());
        assertEquals(arg0._$PINPOINT$_getReactorContext(), mockAsyncContext);

        // after
        asyncContext = interceptor.getAsyncContext(target, new Object[]{arg0}, new Object(), null);

        assertNull(asyncContext);
    }

    @Test
    public void targetContainAsyncContext() {
        AsyncContext mockAsyncContext = mock(AsyncContext.class);
        MockAsyncContextAndReactorContextImpl target = new MockAsyncContextAndReactorContextImpl();
        MockAsyncContextAndReactorContextImpl arg0 = new MockAsyncContextAndReactorContextImpl();
        FluxAndMonoSubscribeInterceptor interceptor = new FluxAndMonoSubscribeInterceptor(mockTraceContext, mockMethodDescriptor, mockServiceType);

        // Set asyncContext to target
        target._$PINPOINT$_setAsyncContext(mockAsyncContext);
        // before
        AsyncContext asyncContext = interceptor.getAsyncContext(target, new Object[]{arg0});

        assertNotNull(asyncContext);
        assertNotNull(target._$PINPOINT$_getReactorContext());
        assertEquals(target._$PINPOINT$_getReactorContext(), mockAsyncContext);
        assertNotNull(arg0._$PINPOINT$_getReactorContext());
        assertEquals(arg0._$PINPOINT$_getReactorContext(), mockAsyncContext);

        // after
        asyncContext = interceptor.getAsyncContext(target, new Object[]{arg0}, new Object(), null);

        assertNotNull(asyncContext);
        assertEquals(asyncContext, mockAsyncContext);
    }

    @Test
    public void targetNotContainAsyncContext() {
        MockAsyncContextAndReactorContextImpl target = new MockAsyncContextAndReactorContextImpl();
        MockAsyncContextAndReactorContextImpl arg0 = new MockAsyncContextAndReactorContextImpl();
        FluxAndMonoSubscribeInterceptor interceptor = new FluxAndMonoSubscribeInterceptor(mockTraceContext, mockMethodDescriptor, mockServiceType);

        // Not set asyncContext to target
        // before
        AsyncContext asyncContext = interceptor.getAsyncContext(target, new Object[]{arg0});

        assertNull(asyncContext);
        assertNull(target._$PINPOINT$_getAsyncContext());
        assertNull(target._$PINPOINT$_getReactorContext());
        assertNull(arg0._$PINPOINT$_getReactorContext());

        // after
        asyncContext = interceptor.getAsyncContext(target, new Object[]{arg0}, new Object(), null);

        assertNull(asyncContext);
    }

    @Test
    public void arg0ContainReactorContext() {
        AsyncContext mockAsyncContext = mock(AsyncContext.class);
        MockAsyncContextAndReactorContextImpl target = new MockAsyncContextAndReactorContextImpl();
        MockAsyncContextAndReactorContextImpl arg0 = new MockAsyncContextAndReactorContextImpl();
        FluxAndMonoSubscribeInterceptor interceptor = new FluxAndMonoSubscribeInterceptor(mockTraceContext, mockMethodDescriptor, mockServiceType);

        // Set asyncContext to target
        arg0._$PINPOINT$_setReactorContext(mockAsyncContext);
        // before
        AsyncContext asyncContext = interceptor.getAsyncContext(target, new Object[]{arg0});

        assertNull(asyncContext);
        assertNull(target._$PINPOINT$_getAsyncContext());
        assertNotNull(target._$PINPOINT$_getReactorContext());
        assertEquals(target._$PINPOINT$_getReactorContext(), mockAsyncContext);

        // after
        asyncContext = interceptor.getAsyncContext(target, new Object[]{arg0}, new Object(), null);

        assertNull(asyncContext);
    }

    @Test
    public void arg0NotContainReactorContext() {
        AsyncContext mockAsyncContext = mock(AsyncContext.class);
        MockAsyncContextAndReactorContextImpl target = new MockAsyncContextAndReactorContextImpl();
        MockAsyncContextAndReactorContextImpl arg0 = new MockAsyncContextAndReactorContextImpl();
        FluxAndMonoSubscribeInterceptor interceptor = new FluxAndMonoSubscribeInterceptor(mockTraceContext, mockMethodDescriptor, mockServiceType);

        // Not set asyncContext to target
        // before
        AsyncContext asyncContext = interceptor.getAsyncContext(target, new Object[]{arg0});

        assertNull(asyncContext);
        assertNull(target._$PINPOINT$_getAsyncContext());
        assertNull(target._$PINPOINT$_getReactorContext());
        assertNull(arg0._$PINPOINT$_getReactorContext());

        // after
        asyncContext = interceptor.getAsyncContext(target, new Object[]{arg0}, new Object(), null);

        assertNull(asyncContext);
    }
}