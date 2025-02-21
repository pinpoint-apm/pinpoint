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
        MockReactorSubscriberAccessor arg0 = new MockReactorSubscriberAccessor();
        MockReactorSubscriberAccessor result = new MockReactorSubscriberAccessor();
        FluxAndMonoSubscribeOrReturnInterceptor interceptor = new FluxAndMonoSubscribeOrReturnInterceptor();

        // Set asyncContext to target
        target._$PINPOINT$_setAsyncContext(mockAsyncContext);
        interceptor.before(target, 1, new Object[]{arg0});
        interceptor.after(target, 1, new Object[]{arg0}, result, null);

        assertNotNull(result._$PINPOINT$_getReactorSubscriber());
        assertEquals(result._$PINPOINT$_getReactorSubscriber().getAsyncContext(), mockAsyncContext);
    }

    @Test
    public void targetNotContainAsyncContext() {
        MockAsyncContextImpl target = new MockAsyncContextImpl();
        MockReactorSubscriberAccessor arg0 = new MockReactorSubscriberAccessor();
        MockReactorSubscriberAccessor result = new MockReactorSubscriberAccessor();
        FluxAndMonoSubscribeOrReturnInterceptor interceptor = new FluxAndMonoSubscribeOrReturnInterceptor();

        // Not set asyncContext to target
        interceptor.before(target, 1, new Object[]{arg0});
        interceptor.after(target, 1, new Object[]{arg0}, result, null);

        assertNull(result._$PINPOINT$_getReactorSubscriber());
    }

    @Test
    public void arg0ContainReactorSubscriber() {
        AsyncContext mockAsyncContext = mock(AsyncContext.class);
        ReactorSubscriber mockReactorSubscriber = mock(ReactorSubscriber.class);
        MockAsyncContextImpl target = new MockAsyncContextImpl();
        MockReactorSubscriberAccessor arg0 = new MockReactorSubscriberAccessor();
        MockReactorSubscriberAccessor result = new MockReactorSubscriberAccessor();
        FluxAndMonoSubscribeOrReturnInterceptor interceptor = new FluxAndMonoSubscribeOrReturnInterceptor();

        // Set asyncContext to target
        arg0._$PINPOINT$_setReactorSubscriber(mockReactorSubscriber);
        interceptor.before(target, 1, new Object[]{arg0});
        interceptor.after(target, 1, new Object[]{arg0}, result, null);

        assertNotNull(result._$PINPOINT$_getReactorSubscriber());
    }

    @Test
    public void arg0NotContainReactorSubscriber() {
        AsyncContext mockAsyncContext = mock(AsyncContext.class);
        MockAsyncContextImpl target = new MockAsyncContextImpl();
        MockReactorSubscriberAccessor arg0 = new MockReactorSubscriberAccessor();
        MockReactorSubscriberAccessor result = new MockReactorSubscriberAccessor();
        FluxAndMonoSubscribeOrReturnInterceptor interceptor = new FluxAndMonoSubscribeOrReturnInterceptor();

        // Not set asyncContext to target
        interceptor.before(target, 1, new Object[]{arg0});
        interceptor.after(target, 1, new Object[]{arg0}, result, null);

        assertNull(result._$PINPOINT$_getReactorSubscriber());
    }
}