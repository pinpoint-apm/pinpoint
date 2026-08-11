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
package com.navercorp.pinpoint.plugin.spring.webflux.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Weaver-carried block pairing of the wrapping WebClient exchange interceptor on the
 * synchronous-trace base: before() begins a TraceBlock on the ambient raw trace and hands it to
 * the weaver; after() unwinds exactly that block instead of re-resolving the ambient trace. The
 * replace hook wraps the response publisher, minting the next AsyncContext even for unsampled
 * traces as the original did.
 */
public class WrappingDefaultWebClientExchangeMethodInterceptorTest {

    private static final Object[] ARGS = new Object[]{null};

    private WrappingDefaultWebClientExchangeMethodInterceptor interceptor;
    private TraceContext traceContext;
    private Trace trace;
    private TraceBlock block;

    @BeforeEach
    public void setUp() {
        traceContext = mock(TraceContext.class);
        trace = mock(Trace.class);
        block = mock(TraceBlock.class);
        when(traceContext.currentRawTraceObject()).thenReturn(trace);
        when(trace.getTraceBlock()).thenReturn(block);
        when(block.getTrace()).thenReturn(trace);

        interceptor = new WrappingDefaultWebClientExchangeMethodInterceptor(traceContext, mock(MethodDescriptor.class));
    }

    @Test
    public void before_beginsBlockAndHandsItToTheWeaver() {
        final TraceBlock returned = interceptor.before(new Object(), Object.class, ARGS);

        assertSame(block, returned);
        verify(block, times(1)).begin();
    }

    @Test
    public void beforeWithoutTrace_returnsNull() {
        when(traceContext.currentRawTraceObject()).thenReturn(null);

        assertNull(interceptor.before(new Object(), Object.class, ARGS));
    }

    @Test
    public void afterWithNullBlock_keepsResultAndTouchesNothing() {
        final Object result = new Object();

        final Object returned = interceptor.after(null, new Object(), Object.class, ARGS, result, null);

        assertSame(result, returned);
        verify(block, never()).close();
    }

    @Test
    public void afterBegun_wrapsAndClosesTheBlock() {
        when(block.isBegin()).thenReturn(true);
        when(trace.canSampled()).thenReturn(true);
        final AsyncContext nextContext = mock(AsyncContext.class);
        when(block.recordNextAsyncContext()).thenReturn(nextContext);
        final Mono<String> source = Mono.defer(() -> Mono.just("body"));

        final Object returned = interceptor.after(block, new Object(), Object.class, ARGS, source, null);

        assertNotSame(source, returned);
        assertTrue(returned instanceof Mono);
        verify(block, times(1)).recordApi(org.mockito.ArgumentMatchers.any(MethodDescriptor.class));
        verify(block, times(1)).close();
    }

    @Test
    public void afterUnsampled_skipsApiRecordingButStillWraps() {
        // as in the original: the next AsyncContext is recorded even for unsampled traces.
        when(block.isBegin()).thenReturn(true);
        when(trace.canSampled()).thenReturn(false);
        final AsyncContext nextContext = mock(AsyncContext.class);
        when(block.recordNextAsyncContext()).thenReturn(nextContext);
        final Mono<String> source = Mono.defer(() -> Mono.just("body"));

        final Object returned = interceptor.after(block, new Object(), Object.class, ARGS, source, null);

        assertNotSame(source, returned);
        verify(block, never()).recordApi(org.mockito.ArgumentMatchers.any(MethodDescriptor.class));
        verify(block, times(1)).recordNextAsyncContext();
        verify(block, times(1)).close();
    }

    @Test
    public void afterWithThrowable_keepsResult() {
        when(block.isBegin()).thenReturn(true);
        when(trace.canSampled()).thenReturn(true);
        final Mono<String> source = Mono.defer(() -> Mono.just("body"));

        final Object returned = interceptor.after(block, new Object(), Object.class, ARGS, source, new RuntimeException("fail"));

        assertSame(source, returned);
        verify(block, never()).recordNextAsyncContext();
        verify(block, times(1)).close();
    }

    @Test
    public void afterNotBegun_keepsResultAndClosesQuietly() {
        when(block.isBegin()).thenReturn(false);
        final Mono<String> source = Mono.defer(() -> Mono.just("body"));

        final Object returned = interceptor.after(block, new Object(), Object.class, ARGS, source, null);

        assertSame(source, returned);
        verify(block, never()).recordNextAsyncContext();
        // close() is a no-op end when not begun - the block owns that decision.
        verify(block, times(1)).close();
    }
}
