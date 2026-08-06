/*
 * Copyright 2025 NAVER Corp.
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
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.IgnoreMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * The carrier is copied in before(), which is woven in right after the super()/this() call - after() is
 * annotated {@link IgnoreMethod} and never woven in, so every case here drives before().
 */
public class CoreSubscriberConstructorInterceptorTest {

    private final CoreSubscriberConstructorInterceptor interceptor = new CoreSubscriberConstructorInterceptor();

    @Test
    public void laterConstructorRunOverwritesEarlierCopy() {
        // pins last-write-wins: delegating constructors (super()/this()) run this interceptor
        // once per chained constructor and the LAST copy is the one that sticks. A target-first
        // guard here was tried and refuted by the propagation ITs (2026-08-05): base
        // constructors can copy from placeholder arguments (e.g. Operators.EMPTY_SUBSCRIBER)
        // and the subclass constructor's overwrite is what heals that value.
        AsyncContext earlierAsyncContext = mock(AsyncContext.class);
        AsyncContext laterAsyncContext = mock(AsyncContext.class);
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        MockAsyncContextAccessor arg0 = new MockAsyncContextAccessor();

        target._$PINPOINT$_setAsyncContext(earlierAsyncContext);
        arg0._$PINPOINT$_setAsyncContext(laterAsyncContext);
        interceptor.before(target, new Object[]{arg0});

        assertSame(laterAsyncContext, target._$PINPOINT$_getAsyncContext());
    }

    @Test
    public void arg0ContainAsyncContext() {
        AsyncContext actualAsyncContext = mock(AsyncContext.class);
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        MockAsyncContextAccessor arg0 = new MockAsyncContextAccessor();

        arg0._$PINPOINT$_setAsyncContext(actualAsyncContext);
        interceptor.before(target, new Object[]{arg0});

        assertNotNull(target._$PINPOINT$_getAsyncContext());
        assertSame(actualAsyncContext, target._$PINPOINT$_getAsyncContext());
    }

    @Test
    public void arg1ContainAsyncContext() {
        AsyncContext actualAsyncContext = mock(AsyncContext.class);
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        Object arg0 = new Object();
        MockAsyncContextAccessor arg1 = new MockAsyncContextAccessor();

        arg1._$PINPOINT$_setAsyncContext(actualAsyncContext);
        interceptor.before(target, new Object[]{arg0, arg1});

        assertSame(actualAsyncContext, target._$PINPOINT$_getAsyncContext());
    }

    @Test
    public void emptyAccessorBeforeActual_doesNotMaskActualContext() {
        AsyncContext actualAsyncContext = mock(AsyncContext.class);
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        MockAsyncContextAccessor source = new MockAsyncContextAccessor();
        MockAsyncContextAccessor actual = new MockAsyncContextAccessor();
        actual._$PINPOINT$_setAsyncContext(actualAsyncContext);

        interceptor.before(target, new Object[]{source, actual});

        assertSame(actualAsyncContext, target._$PINPOINT$_getAsyncContext());
    }

    @Test
    public void subclassConstructor_doesNotOverwriteContextCopiedByBaseConstructor() {
        AsyncContext actualAsyncContext = mock(AsyncContext.class);
        AsyncContext staleSourceAsyncContext = mock(AsyncContext.class);
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        MockAsyncContextAccessor source = new MockAsyncContextAccessor();
        MockAsyncContextAccessor actual = new MockAsyncContextAccessor();
        source._$PINPOINT$_setAsyncContext(staleSourceAsyncContext);
        actual._$PINPOINT$_setAsyncContext(actualAsyncContext);

        // Instrumented base constructor: super(actual)
        interceptor.before(target, new Object[]{actual});
        // Instrumented subclass constructor: Subscriber(source, actual)
        interceptor.before(target, new Object[]{source, actual});

        assertSame(actualAsyncContext, target._$PINPOINT$_getAsyncContext(),
                "a subclass constructor must not replace the context already copied from actual");
    }

    @Test
    public void conflictingArguments_withoutExistingContext_areNotChosenArbitrarily() {
        AsyncContext sourceAsyncContext = mock(AsyncContext.class);
        AsyncContext actualAsyncContext = mock(AsyncContext.class);
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        MockAsyncContextAccessor source = new MockAsyncContextAccessor();
        MockAsyncContextAccessor actual = new MockAsyncContextAccessor();
        source._$PINPOINT$_setAsyncContext(sourceAsyncContext);
        actual._$PINPOINT$_setAsyncContext(actualAsyncContext);

        interceptor.before(target, new Object[]{source, actual});

        assertNull(target._$PINPOINT$_getAsyncContext(),
                "an ambiguous constructor must defer propagation instead of selecting a possibly stale context");
    }

    @Test
    public void multipleArgumentsWithSameContext_copyThatContext() {
        AsyncContext asyncContext = mock(AsyncContext.class);
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        MockAsyncContextAccessor first = new MockAsyncContextAccessor();
        MockAsyncContextAccessor second = new MockAsyncContextAccessor();
        first._$PINPOINT$_setAsyncContext(asyncContext);
        second._$PINPOINT$_setAsyncContext(asyncContext);

        interceptor.before(target, new Object[]{first, second});

        assertSame(asyncContext, target._$PINPOINT$_getAsyncContext());
    }

    @Test
    public void argContainAccessorWithoutAsyncContext() {
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        MockAsyncContextAccessor arg0 = new MockAsyncContextAccessor();

        interceptor.before(target, new Object[]{arg0});

        assertNull(target._$PINPOINT$_getAsyncContext());
    }

    @Test
    public void argNotContainAccessor() {
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        Object arg0 = new Object();

        interceptor.before(target, new Object[]{arg0});

        assertNull(target._$PINPOINT$_getAsyncContext());
    }

    @Test
    public void noArgsOrForeignTarget_doesNotThrow() {
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();

        interceptor.before(target, new Object[0]);
        interceptor.before(target, null);
        interceptor.before(new Object(), new Object[]{new MockAsyncContextAccessor()});

        assertNull(target._$PINPOINT$_getAsyncContext());
    }

    /**
     * Reproduces the ordering that used to require a deferred lookup through the actual:
     * {@code FluxConcatMap$ConcatMapImmediate} creates {@code ConcatMapInner(this)} inside its own
     * constructor, so the inner's actual is an enclosing subscriber that is still being constructed.
     * Because the enclosing subscriber is seeded in before(), the inner already finds the carrier.
     */
    @Test
    public void enclosingSeededInBefore_isVisibleToAnInnerCreatedInTheBody() {
        AsyncContext asyncContext = mock(AsyncContext.class);
        MockAsyncContextAccessor downstream = new MockAsyncContextAccessor();
        MockAsyncContextAccessor enclosing = new MockAsyncContextAccessor();
        MockAsyncContextAccessor inner = new MockAsyncContextAccessor();
        downstream._$PINPOINT$_setAsyncContext(asyncContext);

        // new Enclosing(downstream) - before() runs right after super()
        interceptor.before(enclosing, new Object[]{downstream});
        // ... constructor body: new Inner(this)
        interceptor.before(inner, new Object[]{enclosing});

        assertSame(asyncContext, enclosing._$PINPOINT$_getAsyncContext());
        assertSame(asyncContext, inner._$PINPOINT$_getAsyncContext(), "inner must not be left empty");
    }

    /**
     * after() carries {@link IgnoreMethod}, so the profiler does not weave a call to it. Asserting the
     * annotation is what pins that down - the empty body alone would not tell a reader that the call
     * site is gone rather than just a no-op.
     */
    @Test
    public void afterIsNotWovenIn() throws Exception {
        assertTrue(CoreSubscriberConstructorInterceptor.class
                .getMethod("after", Object.class, Object[].class, Object.class, Throwable.class)
                .isAnnotationPresent(IgnoreMethod.class));
    }
}
