/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

public class ReactorAsyncContextResolverTest {

    @Test
    public void nullOrEmptyArgs() {
        assertNull(ReactorAsyncContextResolver.findUnique(null));
        assertNull(ReactorAsyncContextResolver.findUnique(new Object[0]));
    }

    @Test
    public void noAccessorArgs() {
        assertNull(ReactorAsyncContextResolver.findUnique(new Object[]{new Object(), "arg", 100L}));
    }

    @Test
    public void accessorWithoutContext() {
        MockAsyncContextAccessor emptyAccessor = new MockAsyncContextAccessor();

        assertNull(ReactorAsyncContextResolver.findUnique(new Object[]{emptyAccessor}));
    }

    @Test
    public void singleContext_amongPlainArgs() {
        AsyncContext asyncContext = mock(AsyncContext.class);
        MockAsyncContextAccessor carrier = new MockAsyncContextAccessor();
        carrier._$PINPOINT$_setAsyncContext(asyncContext);

        assertSame(asyncContext, ReactorAsyncContextResolver.findUnique(new Object[]{new Object(), carrier, 100L}));
    }

    @Test
    public void emptyAccessorDoesNotMaskContext() {
        AsyncContext asyncContext = mock(AsyncContext.class);
        MockAsyncContextAccessor emptyAccessor = new MockAsyncContextAccessor();
        MockAsyncContextAccessor carrier = new MockAsyncContextAccessor();
        carrier._$PINPOINT$_setAsyncContext(asyncContext);

        assertSame(asyncContext, ReactorAsyncContextResolver.findUnique(new Object[]{emptyAccessor, carrier}));
    }

    @Test
    public void sameContextRepeated() {
        AsyncContext asyncContext = mock(AsyncContext.class);
        MockAsyncContextAccessor first = new MockAsyncContextAccessor();
        MockAsyncContextAccessor second = new MockAsyncContextAccessor();
        first._$PINPOINT$_setAsyncContext(asyncContext);
        second._$PINPOINT$_setAsyncContext(asyncContext);

        assertSame(asyncContext, ReactorAsyncContextResolver.findUnique(new Object[]{first, second}));
    }

    @Test
    public void conflictingContexts_notChosenArbitrarily() {
        MockAsyncContextAccessor first = new MockAsyncContextAccessor();
        MockAsyncContextAccessor second = new MockAsyncContextAccessor();
        first._$PINPOINT$_setAsyncContext(mock(AsyncContext.class));
        second._$PINPOINT$_setAsyncContext(mock(AsyncContext.class));

        assertNull(ReactorAsyncContextResolver.findUnique(new Object[]{first, second}));
    }
}
