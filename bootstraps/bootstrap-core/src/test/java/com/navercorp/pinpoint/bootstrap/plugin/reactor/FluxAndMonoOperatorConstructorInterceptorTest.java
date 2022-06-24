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

public class FluxAndMonoOperatorConstructorInterceptorTest {

    @Test
    public void arg0ContainAsyncContext() {
        AsyncContext mockAsyncContext = mock(AsyncContext.class);
        MockAsyncContextAndReactorContextImpl target = new MockAsyncContextAndReactorContextImpl();
        MockAsyncContextAndReactorContextImpl arg0 = new MockAsyncContextAndReactorContextImpl();
        FluxAndMonoOperatorConstructorInterceptor interceptor = new FluxAndMonoOperatorConstructorInterceptor();

        // Set asyncContext to target
        arg0._$PINPOINT$_setAsyncContext(mockAsyncContext);
        interceptor.after(target, new Object[]{arg0}, new Object(), null);

        assertNull(target._$PINPOINT$_getAsyncContext());
        assertNotNull(target._$PINPOINT$_getReactorContext());
        assertEquals(target._$PINPOINT$_getReactorContext(), mockAsyncContext);
    }

    @Test
    public void arg0ContainReactorContext() {
        AsyncContext mockAsyncContext = mock(AsyncContext.class);
        MockAsyncContextAndReactorContextImpl target = new MockAsyncContextAndReactorContextImpl();
        MockAsyncContextAndReactorContextImpl arg0 = new MockAsyncContextAndReactorContextImpl();
        FluxAndMonoOperatorConstructorInterceptor interceptor = new FluxAndMonoOperatorConstructorInterceptor();

        // Set asyncContext to target
        arg0._$PINPOINT$_setReactorContext(mockAsyncContext);
        interceptor.after(target, new Object[]{arg0}, new Object(), null);

        assertNull(target._$PINPOINT$_getAsyncContext());
        assertNotNull(target._$PINPOINT$_getReactorContext());
        assertEquals(target._$PINPOINT$_getReactorContext(), mockAsyncContext);
    }

    @Test
    public void arg0NotContainAsyncContext() {
        MockAsyncContextAndReactorContextImpl target = new MockAsyncContextAndReactorContextImpl();
        MockAsyncContextAndReactorContextImpl arg0 = new MockAsyncContextAndReactorContextImpl();
        FluxAndMonoOperatorConstructorInterceptor interceptor = new FluxAndMonoOperatorConstructorInterceptor();

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
        FluxAndMonoOperatorConstructorInterceptor interceptor = new FluxAndMonoOperatorConstructorInterceptor();

        // Set asyncContext to target
        arg1._$PINPOINT$_setAsyncContext(mockAsyncContext);
        interceptor.after(target, new Object[]{arg0, arg1}, new Object(), null);

        assertNotNull(target._$PINPOINT$_getAsyncContext());
        assertEquals(target._$PINPOINT$_getAsyncContext(), mockAsyncContext);
        assertNotNull(target._$PINPOINT$_getReactorContext());
        assertEquals(target._$PINPOINT$_getReactorContext(), mockAsyncContext);
    }

    @Test
    public void throwableIsNotNull() {
        AsyncContext mockAsyncContext = mock(AsyncContext.class);
        MockAsyncContextAndReactorContextImpl target = new MockAsyncContextAndReactorContextImpl();
        MockAsyncContextAndReactorContextImpl arg0 = new MockAsyncContextAndReactorContextImpl();
        Throwable throwable = new Throwable("ERROR");
        FluxAndMonoOperatorConstructorInterceptor interceptor = new FluxAndMonoOperatorConstructorInterceptor();

        interceptor.after(target, new Object[]{arg0}, new Object(), throwable);

        assertNull(target._$PINPOINT$_getAsyncContext());
        assertNull(target._$PINPOINT$_getReactorContext());
    }
}