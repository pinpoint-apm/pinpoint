/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.reactorsupport;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

import java.util.function.Function;

/**
 * Wraps a {@link Mono}/{@link Flux} returned from an I/O seam so that each subscription gets its
 * own {@link TracedSubscriber} carrying the seam's {@link AsyncContext} — "own instead of inject":
 * no field write on the reactor object, and no cross-subscription leak when the application reuses
 * the publisher.
 * <p>
 * {@code Operators.lift} does the wrapping so the vendor code handles the publisher subtype zoo
 * and {@code Scannable} delegation. Guards, in order:
 * <ul>
 * <li>{@link Fuseable.ScalarCallable} ({@code Mono.just/empty/error}) is left alone — wrapping
 * would destroy the scalar fast path (OTel excludes these as well).</li>
 * <li>Only plain {@code Mono}/{@code Flux} are wrapped. {@code ConnectableFlux}/{@code GroupedFlux}
 * are skipped: reactor 3.1.x lift has no variants for them, so wrapping would lose the subtype.</li>
 * </ul>
 * Skipping is POLICY, not a gap left for a later fallback: the injection path this wrapper
 * replaces is a no-op for the very same types — scalar publishers extend {@code Mono}/{@code Flux}
 * directly, no transform gives them an accessor field, so the old {@code instanceof
 * AsyncContextAccessor} write never fired either. A skipped seam behaves exactly like the
 * injection path did; scalar delivery is synchronous in subscribe() and rides the ambient trace.
 * <p>
 * Any internal failure returns the original publisher — the seam degrades to untraced, never broken.
 */
public final class SeamPublisherWrapper {
    private static final PluginLogger logger = PluginLogManager.getLogger(SeamPublisherWrapper.class);

    private SeamPublisherWrapper() {
    }

    /**
     * Whether {@link #wrap} would actually wrap this object. Callers that create an
     * {@link AsyncContext} just to wrap should check this first to avoid dangling async links.
     */
    public static boolean isWrappable(final Object result) {
        if (!(result instanceof Mono) && !(result instanceof Flux)) {
            return false;
        }
        if (result instanceof Fuseable.ScalarCallable) {
            return false;
        }
        if (result instanceof ConnectableFlux || result instanceof GroupedFlux) {
            return false;
        }
        return true;
    }

    public static Object wrap(final Object result, final AsyncContext asyncContext) {
        try {
            if (asyncContext == null || result == null || !isWrappable(result)) {
                return result;
            }
            return lift((Publisher<?>) result, asyncContext);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to wrap publisher {}. Caused:{}", result, th.getMessage(), th);
            }
            return result;
        }
    }

    private static <T> Publisher<T> lift(final Publisher<T> source, final AsyncContext asyncContext) {
        final Function<? super Publisher<T>, ? extends Publisher<T>> lift =
                Operators.lift((scannable, actual) -> newTracedSubscriber(actual, asyncContext));
        return lift.apply(source);
    }

    @SuppressWarnings("unchecked")
    private static <T> TracedSubscriber<T> newTracedSubscriber(final CoreSubscriber<? super T> actual, final AsyncContext asyncContext) {
        if (actual instanceof Fuseable.ConditionalSubscriber) {
            // preserve the downstream's tryOnNext optimization.
            return new TracedSubscriber.Conditional<T>((Fuseable.ConditionalSubscriber<? super T>) actual, asyncContext);
        }
        return new TracedSubscriber<T>(actual, asyncContext);
    }
}
