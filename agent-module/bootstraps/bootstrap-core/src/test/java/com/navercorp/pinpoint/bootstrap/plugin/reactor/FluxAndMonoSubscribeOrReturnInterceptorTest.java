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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

public class FluxAndMonoSubscribeOrReturnInterceptorTest {

    @Test
    public void targetContainAsyncContext() {
        AsyncContext mockAsyncContext = mock(AsyncContext.class);
        MockAsyncContextImpl target = new MockAsyncContextImpl();
        MockAsyncContextAccessor arg0 = new MockAsyncContextAccessor();
        MockAsyncContextAccessor result = new MockAsyncContextAccessor();
        FluxAndMonoSubscribeOrReturnInterceptor interceptor = new FluxAndMonoSubscribeOrReturnInterceptor();

        // Set asyncContext to target
        target._$PINPOINT$_setAsyncContext(mockAsyncContext);
        interceptor.before(target, AsyncContextAccessorUtils.getAsyncContext(target), 1, new Object[]{arg0});
        interceptor.after(target, AsyncContextAccessorUtils.getAsyncContext(target), 1, new Object[]{arg0}, result, null);

        assertNotNull(result._$PINPOINT$_getAsyncContext());
        assertEquals(result._$PINPOINT$_getAsyncContext(), mockAsyncContext);
    }

    @Test
    public void targetNotContainAsyncContext() {
        MockAsyncContextImpl target = new MockAsyncContextImpl();
        MockAsyncContextAccessor arg0 = new MockAsyncContextAccessor();
        MockAsyncContextAccessor result = new MockAsyncContextAccessor();
        FluxAndMonoSubscribeOrReturnInterceptor interceptor = new FluxAndMonoSubscribeOrReturnInterceptor();

        // Not set asyncContext to target
        interceptor.before(target, AsyncContextAccessorUtils.getAsyncContext(target), 1, new Object[]{arg0});
        interceptor.after(target, AsyncContextAccessorUtils.getAsyncContext(target), 1, new Object[]{arg0}, result, null);

        assertNull(result._$PINPOINT$_getAsyncContext());
    }

    @Test
    public void arg0ContainAsyncContext() {
        AsyncContext mockAsyncContext = mock(AsyncContext.class);
        AsyncContext mockAsyncContext2 = mock(AsyncContext.class);
        MockAsyncContextImpl target = new MockAsyncContextImpl();
        MockAsyncContextAccessor arg0 = new MockAsyncContextAccessor();
        MockAsyncContextAccessor result = new MockAsyncContextAccessor();
        FluxAndMonoSubscribeOrReturnInterceptor interceptor = new FluxAndMonoSubscribeOrReturnInterceptor();

        // Set asyncContext to target
        arg0._$PINPOINT$_setAsyncContext(mockAsyncContext2);
        interceptor.before(target, AsyncContextAccessorUtils.getAsyncContext(target), 1, new Object[]{arg0});
        interceptor.after(target, AsyncContextAccessorUtils.getAsyncContext(target), 1, new Object[]{arg0}, result, null);

        assertNotNull(result._$PINPOINT$_getAsyncContext());
    }

    @Test
    public void relayOverwritesSubscriberContext() {
        // pins last-write-wins: the publisher-side context always overwrites whatever the
        // subscriber already carries. Hop relays (publishOn/subscribeOn/timer) depend on this -
        // a target-first guard here was tried and refuted by the propagation ITs (2026-08-05).
        // Misattribution on shared/cached publishers is the documented trade-off of this rule.
        AsyncContext publisherAsyncContext = mock(AsyncContext.class);
        AsyncContext subscriberAsyncContext = mock(AsyncContext.class);
        MockAsyncContextImpl target = new MockAsyncContextImpl();
        MockAsyncContextAccessor arg0 = new MockAsyncContextAccessor();
        FluxAndMonoSubscribeOrReturnInterceptor interceptor = new FluxAndMonoSubscribeOrReturnInterceptor();

        target._$PINPOINT$_setAsyncContext(publisherAsyncContext);
        arg0._$PINPOINT$_setAsyncContext(subscriberAsyncContext);
        interceptor.before(target, AsyncContextAccessorUtils.getAsyncContext(target), 1, new Object[]{arg0});

        assertEquals(publisherAsyncContext, arg0._$PINPOINT$_getAsyncContext());
    }

    @Test
    public void arg0NotContainAsyncContext() {
        AsyncContext mockAsyncContext = mock(AsyncContext.class);
        MockAsyncContextImpl target = new MockAsyncContextImpl();
        MockAsyncContextAccessor arg0 = new MockAsyncContextAccessor();
        MockAsyncContextAccessor result = new MockAsyncContextAccessor();
        FluxAndMonoSubscribeOrReturnInterceptor interceptor = new FluxAndMonoSubscribeOrReturnInterceptor();

        // Not set asyncContext to target
        interceptor.before(target, AsyncContextAccessorUtils.getAsyncContext(target), 1, new Object[]{arg0});
        interceptor.after(target, AsyncContextAccessorUtils.getAsyncContext(target), 1, new Object[]{arg0}, result, null);

        assertNull(result._$PINPOINT$_getAsyncContext());
    }
}
