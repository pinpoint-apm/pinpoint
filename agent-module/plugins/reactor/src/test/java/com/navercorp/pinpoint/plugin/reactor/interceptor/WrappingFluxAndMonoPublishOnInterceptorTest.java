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

package com.navercorp.pinpoint.plugin.reactor.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WrappingFluxAndMonoPublishOnInterceptorTest {
    private static final Object[] ARGS = new Object[0];

    private TraceContext traceContext;
    private Trace trace;
    private TraceBlock block;
    private AsyncContext asyncContext;
    private WrappingFluxAndMonoPublishOnInterceptor interceptor;

    @BeforeEach
    public void setUp() {
        traceContext = mock(TraceContext.class);
        trace = mock(Trace.class);
        block = mock(TraceBlock.class);
        asyncContext = mock(AsyncContext.class);
        MethodDescriptor methodDescriptor = mock(MethodDescriptor.class);

        when(traceContext.currentTraceObject()).thenReturn(trace);
        when(trace.getTraceBlock()).thenReturn(block);
        when(block.getTrace()).thenReturn(trace);
        when(block.isBegin()).thenReturn(true);
        when(block.recordNextAsyncContext()).thenReturn(asyncContext);
        interceptor = new WrappingFluxAndMonoPublishOnInterceptor(traceContext, methodDescriptor);
    }

    @Test
    public void wrappableResultIsReplaced() {
        Flux<Integer> result = Flux.range(1, 2);

        TraceBlock returned = interceptor.before(result, Flux.class, ARGS);
        Object replacement = interceptor.after(returned, result, Flux.class, ARGS, result, null);

        assertSame(block, returned);
        assertNotSame(result, replacement);
        assertTrue(replacement instanceof Flux);
        verify(block).recordNextAsyncContext();
        verify(block).close();
    }

    @Test
    public void scalarResultDoesNotMintDanglingContext() {
        Mono<Integer> result = Mono.just(1);

        TraceBlock returned = interceptor.before(result, Mono.class, ARGS);
        Object replacement = interceptor.after(returned, result, Mono.class, ARGS, result, null);

        assertSame(result, replacement);
        verify(block, never()).recordNextAsyncContext();
        verify(block).close();
    }

    @Test
    public void exceptionalExitDoesNotWrapOrMint() {
        Flux<Integer> result = Flux.range(1, 2);

        TraceBlock returned = interceptor.before(result, Flux.class, ARGS);
        Object replacement = interceptor.after(returned, result, Flux.class, ARGS, result, new IllegalStateException("test"));

        assertSame(result, replacement);
        verify(block, never()).recordNextAsyncContext();
        verify(block).close();
    }

    @Test
    public void noActiveTraceKeepsOriginalResult() {
        when(traceContext.currentTraceObject()).thenReturn(null);
        Flux<Integer> result = Flux.range(1, 2);

        TraceBlock returned = interceptor.before(result, Flux.class, ARGS);
        Object replacement = interceptor.after(returned, result, Flux.class, ARGS, result, null);

        assertNull(returned);
        assertSame(result, replacement);
        verify(block, never()).recordNextAsyncContext();
        verify(block, never()).close();
    }
}
