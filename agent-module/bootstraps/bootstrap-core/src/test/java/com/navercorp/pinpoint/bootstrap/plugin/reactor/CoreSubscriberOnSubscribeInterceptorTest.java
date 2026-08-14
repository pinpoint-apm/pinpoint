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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

/**
 * The AsyncContext of a subscriber can arrive either from the subscriber itself - copied from its
 * actual while the chain was built - or from the upstream Subscription (args[0]), and onSubscribe is
 * what reconciles the two. These tests pin down which source wins and who gets the context shared with
 * them, neither of which is observable from an integration test.
 */
public class CoreSubscriberOnSubscribeInterceptorTest {

    private final CoreSubscriberOnSubscribeInterceptor interceptor = new CoreSubscriberOnSubscribeInterceptor();

    /**
     * Invokes the interceptor the way the weave site does: the target's own AsyncContext is read from
     * its injected accessor field and passed in as an argument (a target without the field gets null).
     */
    private void onSubscribe(Object target, Object[] args) {
        final AsyncContext own = (target instanceof AsyncContextAccessor)
                ? ((AsyncContextAccessor) target)._$PINPOINT$_getAsyncContext() : null;
        interceptor.before(target, own, 0, args);
    }

    // ------------------------------------------------------------------
    // a single source holds the context
    // ------------------------------------------------------------------

    @Test
    public void targetOnly_sharesWithSubscription() {
        AsyncContext own = mock(AsyncContext.class);
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        MockAsyncContextAccessor subscription = new MockAsyncContextAccessor();
        target._$PINPOINT$_setAsyncContext(own);

        onSubscribe(target, new Object[]{subscription});

        assertSame(own, target._$PINPOINT$_getAsyncContext());
        assertSame(own, subscription._$PINPOINT$_getAsyncContext(), "subscription should be topped up");
    }

    @Test
    public void subscriptionOnly_flowsToTarget() {
        AsyncContext fromSubscription = mock(AsyncContext.class);
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        MockAsyncContextAccessor subscription = new MockAsyncContextAccessor();
        subscription._$PINPOINT$_setAsyncContext(fromSubscription);

        onSubscribe(target, new Object[]{subscription});

        assertSame(fromSubscription, target._$PINPOINT$_getAsyncContext());
        assertSame(fromSubscription, subscription._$PINPOINT$_getAsyncContext(), "should be left as is");
    }

    @Test
    public void noSourceHoldsContext_doesNothing() {
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        MockAsyncContextAccessor subscription = new MockAsyncContextAccessor();

        onSubscribe(target, new Object[]{subscription});

        assertNull(target._$PINPOINT$_getAsyncContext());
        assertNull(subscription._$PINPOINT$_getAsyncContext());
    }

    // ------------------------------------------------------------------
    // relation to the constructor copy
    // ------------------------------------------------------------------

    /**
     * The carrier normally arrives through the constructor, and onSubscribe then only tops up the
     * upstream Subscription. A later change on the actual is deliberately not picked up - the value was
     * copied, not referenced.
     */
    @Test
    public void constructionCopy_makesOnSubscribeTakeTheFastPath() {
        AsyncContext early = mock(AsyncContext.class);
        AsyncContext laterOnActual = mock(AsyncContext.class);
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        MockAsyncContextAccessor subscription = new MockAsyncContextAccessor();
        MockAsyncContextAccessor actual = new MockAsyncContextAccessor();
        actual._$PINPOINT$_setAsyncContext(early);

        new CoreSubscriberConstructorInterceptor().before(target, new Object[]{actual});
        assertSame(early, target._$PINPOINT$_getAsyncContext());

        actual._$PINPOINT$_setAsyncContext(laterOnActual);
        onSubscribe(target, new Object[]{subscription});

        assertSame(early, target._$PINPOINT$_getAsyncContext());
        assertSame(early, subscription._$PINPOINT$_getAsyncContext());
    }

    /**
     * Nothing is pushed down to the downstream actual from here. It does not need to be: the actual
     * receives this subscriber as its own Subscription immediately after, so it picks the carrier up
     * through {@link #subscriptionOnly_flowsToTarget()}.
     */
    @Test
    public void subscriptionContext_reachesTheActualThroughItsOwnOnSubscribe() {
        AsyncContext fromSubscription = mock(AsyncContext.class);
        MockAsyncContextAccessor actual = new MockAsyncContextAccessor();
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        MockAsyncContextAccessor subscription = new MockAsyncContextAccessor();
        subscription._$PINPOINT$_setAsyncContext(fromSubscription);

        // upstream hands the Subscription to this subscriber ...
        onSubscribe(target, new Object[]{subscription});
        // ... which then hands itself to its actual
        onSubscribe(actual, new Object[]{target});

        assertSame(fromSubscription, target._$PINPOINT$_getAsyncContext());
        assertSame(fromSubscription, actual._$PINPOINT$_getAsyncContext());
    }

    // ------------------------------------------------------------------
    // conflicts - documents the current contract, which is "first source wins, no reconciliation"
    // ------------------------------------------------------------------

    /**
     * Subscriber and Subscription hold different contexts. The interceptor does not detect the conflict:
     * the subscriber keeps its own and the Subscription is left alone. Asserting it here so a
     * refactoring cannot change the precedence silently - not a statement that this is the desired
     * behaviour.
     */
    @Test
    public void conflictingContexts_targetWinsAndSubscriptionIsLeftAlone() {
        AsyncContext onTarget = mock(AsyncContext.class);
        AsyncContext onSubscription = mock(AsyncContext.class);
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        MockAsyncContextAccessor subscription = new MockAsyncContextAccessor();
        target._$PINPOINT$_setAsyncContext(onTarget);
        subscription._$PINPOINT$_setAsyncContext(onSubscription);

        onSubscribe(target, new Object[]{subscription});

        assertSame(onTarget, target._$PINPOINT$_getAsyncContext());
        assertSame(onSubscription, subscription._$PINPOINT$_getAsyncContext());
    }

    // ------------------------------------------------------------------
    // robustness
    // ------------------------------------------------------------------

    @Test
    public void argsWithoutAccessor_doesNotThrow() {
        AsyncContext own = mock(AsyncContext.class);
        MockAsyncContextAccessor target = new MockAsyncContextAccessor();
        target._$PINPOINT$_setAsyncContext(own);

        onSubscribe(target, new Object[]{new Object()});
        onSubscribe(target, new Object[0]);
        onSubscribe(target, null);

        assertSame(own, target._$PINPOINT$_getAsyncContext());
    }

    @Test
    public void targetWithoutAccessor_doesNotThrow() {
        MockAsyncContextAccessor subscription = new MockAsyncContextAccessor();
        subscription._$PINPOINT$_setAsyncContext(mock(AsyncContext.class));

        onSubscribe(new Object(), new Object[]{subscription});
    }
}
