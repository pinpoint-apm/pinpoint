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
package com.navercorp.pinpoint.plugin.redis.redisson.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * classpath-negative containment pin (W9 / doc 18 §4.7): all five wrapping interceptors run the
 * wrap path inside a {@code catch (Throwable)}. When reactor is absent, the first use of the
 * shaded SeamPublisherWrapper throws {@link NoClassDefFoundError} (lazy resolution — pinned in
 * seam-support's ReactorAbsenceClassLoadingTest); an ERROR, not an exception. This test proves
 * the catch really contains Error-class throwables from the wrap path and hands the original
 * result back — the call degrades to untraced, never broken. The redisson interceptor stands in
 * for the shared template.
 */
public class WrappingReactiveMethodInterceptorTest {

    private WrappingReactiveMethodInterceptor interceptor;
    private TraceContext traceContext;
    private Trace trace;
    private TraceBlock block;

    @BeforeEach
    public void setUp() {
        final ProfilerConfig profilerConfig = mock(ProfilerConfig.class);
        when(profilerConfig.readBoolean(anyString(), anyBoolean())).thenAnswer(inv -> inv.getArgument(1));
        when(profilerConfig.readInt(anyString(), anyInt())).thenAnswer(inv -> inv.getArgument(1));

        traceContext = mock(TraceContext.class);
        when(traceContext.getProfilerConfig()).thenReturn(profilerConfig);
        interceptor = new WrappingReactiveMethodInterceptor(traceContext, mock(MethodDescriptor.class));

        trace = mock(Trace.class);
        block = mock(TraceBlock.class);
        when(traceContext.currentTraceObject()).thenReturn(trace);
        when(trace.getTraceBlock()).thenReturn(block);
        when(block.getTrace()).thenReturn(trace);
        when(block.isBegin()).thenReturn(true);
    }

    @Test
    public void errorOnWrapPath_isContained_originalResultReturned() {
        // simulate the reactor-absence failure mode: an Error (NoClassDefFoundError) thrown from
        // the wrap path inside after()'s try block.
        when(block.recordNextAsyncContext())
                .thenThrow(new NoClassDefFoundError("reactor/core/publisher/Mono"));
        final Object result = Flux.range(1, 2).hide(); // wrappable, so the wrap path is entered

        final TraceBlock returned = interceptor.before(new Object(), Object.class, new Object[0]);
        final Object kept = interceptor.after(returned, new Object(), Object.class, new Object[0], result, null);

        // contained: the app gets its original publisher back, nothing propagates, and the
        // block is still closed on the error path.
        assertSame(result, kept);
        verify(block).close();
    }
}
