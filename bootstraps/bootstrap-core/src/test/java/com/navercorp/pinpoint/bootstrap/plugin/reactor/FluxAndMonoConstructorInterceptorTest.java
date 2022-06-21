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
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class FluxAndMonoConstructorInterceptorTest {

    @Test
    public void arg0ContainAsyncContext() {
        AsyncContext mockAsyncContext = mock(AsyncContext.class);
        MockAsyncContextAndReactorContextImpl target = new MockAsyncContextAndReactorContextImpl();
        MockAsyncContextAndReactorContextImpl arg0 = new MockAsyncContextAndReactorContextImpl();
        FluxAndMonoConstructorInterceptor interceptor = new FluxAndMonoConstructorInterceptor();

        // Set asyncContext to target
        arg0._$PINPOINT$_setAsyncContext(mockAsyncContext);
        interceptor.after(target, new Object[]{arg0}, new Object(), null);

        assertNotNull(target._$PINPOINT$_getAsyncContext());
        assertEquals(target._$PINPOINT$_getAsyncContext(), arg0._$PINPOINT$_getAsyncContext());
        assertNotNull(target._$PINPOINT$_getReactorContext());
        assertEquals(target._$PINPOINT$_getReactorContext(), arg0._$PINPOINT$_getAsyncContext());
    }

    @Test
    public void arg0NotContainAsyncContext() {
        MockAsyncContextAndReactorContextImpl target = new MockAsyncContextAndReactorContextImpl();
        MockAsyncContextAndReactorContextImpl arg0 = new MockAsyncContextAndReactorContextImpl();
        FluxAndMonoConstructorInterceptor interceptor = new FluxAndMonoConstructorInterceptor();

        // Not set asyncContext to target
        interceptor.after(target, new Object[]{arg0}, new Object(), null);

        assertNull(target._$PINPOINT$_getAsyncContext());
        assertNull(target._$PINPOINT$_getReactorContext());
    }

    @Test
    public void arg1ContainAsyncContext() {
        AsyncContext mockAsyncContext = mock(AsyncContext.class);
        MockAsyncContextAndReactorContextImpl target = new MockAsyncContextAndReactorContextImpl();
        MockAsyncContextAndReactorContextImpl arg0 = new MockAsyncContextAndReactorContextImpl();
        MockAsyncContextAndReactorContextImpl arg1 = new MockAsyncContextAndReactorContextImpl();
        FluxAndMonoConstructorInterceptor interceptor = new FluxAndMonoConstructorInterceptor();

        // Set asyncContext to target
        arg1._$PINPOINT$_setAsyncContext(mockAsyncContext);
        interceptor.after(target, new Object[]{arg0, arg1}, new Object(), null);

        assertNotNull(target._$PINPOINT$_getAsyncContext());
        assertEquals(target._$PINPOINT$_getAsyncContext(), arg1._$PINPOINT$_getAsyncContext());
        assertNotNull(target._$PINPOINT$_getReactorContext());
        assertEquals(target._$PINPOINT$_getReactorContext(), arg1._$PINPOINT$_getAsyncContext());
    }

    @Test
    public void throwableIsNotNull() {
        AsyncContext mockAsyncContext = mock(AsyncContext.class);
        MockAsyncContextAndReactorContextImpl target = new MockAsyncContextAndReactorContextImpl();
        MockAsyncContextAndReactorContextImpl arg0 = new MockAsyncContextAndReactorContextImpl();
        Throwable throwable = new Throwable("ERROR");
        FluxAndMonoConstructorInterceptor interceptor = new FluxAndMonoConstructorInterceptor();

        interceptor.after(target, new Object[]{arg0}, new Object(), throwable);

        assertNull(target._$PINPOINT$_getAsyncContext());
        assertNull(target._$PINPOINT$_getReactorContext());
    }
}