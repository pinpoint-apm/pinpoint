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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

public class CoreSubscriberConstructorInterceptorTest {

    @Test
    public void arg0ContainReactorContext() {
        ReactorSubscriber reactorSubscriber = mock(ReactorSubscriber.class);
        MockReactorActualAccessor target = new MockReactorActualAccessor();
        MockReactorSubscriberAccessor arg0 = new MockReactorSubscriberAccessor();
        CoreSubscriberConstructorInterceptor interceptor = new CoreSubscriberConstructorInterceptor();

        // Set asyncContext to target
        arg0._$PINPOINT$_setReactorSubscriber(reactorSubscriber);
        interceptor.after(target, new Object[]{arg0}, new Object(), null);

        assertNotNull(target._$PINPOINT$_getReactorActual());
        assertEquals(target._$PINPOINT$_getReactorActual(), arg0);
    }

    @Test
    public void argNotContainReactorContext() {
        MockReactorActualAccessor target = new MockReactorActualAccessor();
        Object arg0 = new Object();
        CoreSubscriberConstructorInterceptor interceptor = new CoreSubscriberConstructorInterceptor();

        // Not set asyncContext to target
        interceptor.after(target, new Object[]{arg0}, new Object(), null);

        assertNull(target._$PINPOINT$_getReactorActual());
    }

    @Test
    public void arg1ContainReactorContext() {
        ReactorSubscriber reactorSubscriber = mock(ReactorSubscriber.class);
        MockReactorActualAccessor target = new MockReactorActualAccessor();
        Object arg0 = new Object();
        MockReactorSubscriberAccessor arg1 = new MockReactorSubscriberAccessor();
        CoreSubscriberConstructorInterceptor interceptor = new CoreSubscriberConstructorInterceptor();

        // Set asyncContext to target
        arg1._$PINPOINT$_setReactorSubscriber(reactorSubscriber);
        interceptor.after(target, new Object[]{arg0, arg1}, new Object(), null);

        assertNotNull(target._$PINPOINT$_getReactorActual());
        assertEquals(target._$PINPOINT$_getReactorActual(), arg1);
    }

    @Test
    public void throwableIsNotNull() {
        ReactorSubscriber reactorSubscriber = mock(ReactorSubscriber.class);
        MockReactorActualAccessor target = new MockReactorActualAccessor();
        MockReactorSubscriberAccessor arg0 = new MockReactorSubscriberAccessor();
        Throwable throwable = new Throwable("ERROR");
        CoreSubscriberConstructorInterceptor interceptor = new CoreSubscriberConstructorInterceptor();

        arg0._$PINPOINT$_setReactorSubscriber(reactorSubscriber);
        interceptor.after(target, new Object[]{arg0}, new Object(), throwable);

        assertNull(target._$PINPOINT$_getReactorActual());
    }
}