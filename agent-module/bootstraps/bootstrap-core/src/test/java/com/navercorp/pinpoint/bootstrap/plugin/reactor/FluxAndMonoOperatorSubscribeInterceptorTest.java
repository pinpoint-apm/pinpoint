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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

public class FluxAndMonoOperatorSubscribeInterceptorTest {
    final TraceContext mockTraceContext = mock(TraceContext.class);
    final MethodDescriptor mockMethodDescriptor = mock(MethodDescriptor.class);
    final ServiceType mockServiceType = mock(ServiceType.class);

    @Test
    public void targetContainAsyncContext() {
        AsyncContext mockAsyncContext = mock(AsyncContext.class);
        MockAsyncContextImpl target = new MockAsyncContextImpl();
        MockAsyncContextAccessor arg0 = new MockAsyncContextAccessor();
        FluxAndMonoOperatorSubscribeInterceptor interceptor = new FluxAndMonoOperatorSubscribeInterceptor();

        // Set asyncContext to target
        target._$PINPOINT$_setAsyncContext(mockAsyncContext);
        // before (asyncContext is read by the weaver from target and passed in)
        interceptor.before(target, AsyncContextAccessorUtils.getAsyncContext(target), 1, new Object[]{arg0});
        // after
        interceptor.after(target, AsyncContextAccessorUtils.getAsyncContext(target), 1, new Object[]{arg0}, new Object(), null);
        assertNotNull(target._$PINPOINT$_getAsyncContext());
        assertNotNull(arg0._$PINPOINT$_getAsyncContext());
        assertEquals(arg0._$PINPOINT$_getAsyncContext(), mockAsyncContext);
    }

    @Test
    public void targetNotContainAsyncContext() {
        MockAsyncContextImpl target = new MockAsyncContextImpl();
        MockAsyncContextAccessor arg0 = new MockAsyncContextAccessor();
        FluxAndMonoOperatorSubscribeInterceptor interceptor = new FluxAndMonoOperatorSubscribeInterceptor();

        // Not set asyncContext to target
        // before (weaver reads null from target)
        interceptor.before(target, AsyncContextAccessorUtils.getAsyncContext(target), 1, new Object[]{arg0});
        // after
        interceptor.after(target, AsyncContextAccessorUtils.getAsyncContext(target), 1, new Object[]{arg0}, new Object(), null);

        assertNull(target._$PINPOINT$_getAsyncContext());
        assertNull(arg0._$PINPOINT$_getAsyncContext());
    }
}
