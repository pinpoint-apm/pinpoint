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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class SeamPublisherWrapperTest {

    private AsyncContext asyncContext;

    @BeforeEach
    public void setUp() {
        // continueAsyncTraceObject(false) returns null by default: windows are no-ops here,
        // these tests only exercise the wrap guards and reactive mechanics.
        asyncContext = mock(AsyncContext.class);
    }

    @Test
    public void scalarCallable_leftAlone() {
        Mono<String> just = Mono.just("a");
        Mono<Object> empty = Mono.empty();
        Mono<Object> error = Mono.error(new IllegalStateException("boom"));

        assertSame(just, SeamPublisherWrapper.wrap(just, asyncContext));
        assertSame(empty, SeamPublisherWrapper.wrap(empty, asyncContext));
        assertSame(error, SeamPublisherWrapper.wrap(error, asyncContext));
    }

    @Test
    public void subtypesAndNonPublishers_leftAlone() {
        ConnectableFlux<Integer> connectable = Flux.range(1, 3).publish();
        GroupedFlux<Integer, Integer> grouped = Flux.range(0, 4).groupBy(i -> i % 2).blockFirst();

        assertSame(connectable, SeamPublisherWrapper.wrap(connectable, asyncContext));
        assertSame(grouped, SeamPublisherWrapper.wrap(grouped, asyncContext));
        assertEquals("not a publisher", SeamPublisherWrapper.wrap("not a publisher", asyncContext));
        assertNull(SeamPublisherWrapper.wrap(null, asyncContext));
    }

    @Test
    public void nullAsyncContext_leftAlone() {
        Flux<Integer> source = Flux.range(1, 3);

        assertSame(source, SeamPublisherWrapper.wrap(source, null));
    }

    @Test
    public void wrappedTypeMatchesSource() {
        Object wrappedMono = SeamPublisherWrapper.wrap(Mono.defer(() -> Mono.just("a")), asyncContext);
        Object wrappedFlux = SeamPublisherWrapper.wrap(Flux.range(1, 3), asyncContext);

        assertTrue(wrappedMono instanceof Mono, "a wrapped Mono must still be a Mono");
        assertTrue(wrappedFlux instanceof Flux, "a wrapped Flux must still be a Flux");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void reactorContext_flowsThroughTheWrapper() {
        Mono<String> source = Mono.deferContextual(ctx -> Mono.just((String) ctx.getOrDefault("k", "missing")));
        Mono<String> wrapped = (Mono<String>) SeamPublisherWrapper.wrap(source, asyncContext);
        TestFusionSubscriber<String> downstream = new TestFusionSubscriber<String>(Context.of("k", "v"));

        wrapped.subscribe(downstream);

        assertEquals(Collections.singletonList("v"), downstream.received);
        assertTrue(downstream.completed);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void resubscription_isolatedPerSubscriber() {
        Flux<Integer> wrapped = (Flux<Integer>) SeamPublisherWrapper.wrap(Flux.range(1, 2), asyncContext);
        TestFusionSubscriber<Integer> first = new TestFusionSubscriber<Integer>();
        TestFusionSubscriber<Integer> second = new TestFusionSubscriber<Integer>();

        wrapped.subscribe(first);
        wrapped.subscribe(second);

        assertEquals(Arrays.asList(1, 2), first.received);
        assertEquals(Arrays.asList(1, 2), second.received);
        assertTrue(first.subscription instanceof TracedSubscriber);
        assertTrue(second.subscription instanceof TracedSubscriber);
        assertNotSame(first.subscription, second.subscription, "each subscription must get its own TracedSubscriber");
    }
}
