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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncContextTraceBlock;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.recorder.WrappedSpanEventRecorder;
import com.navercorp.pinpoint.profiler.context.storage.Storage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Every Trace implementation hands out a context-carrying block from getTraceBlock(AsyncContext)
 * and keeps the plain block from getTraceBlock(). (DefaultTrace is covered in DefaultTraceTest.)
 */
@ExtendWith(MockitoExtension.class)
class AsyncContextTraceBlockFactoryTest {

    @Mock
    private TraceRoot traceRoot;
    @Mock
    private LocalTraceRoot localTraceRoot;
    @Mock
    private Shared shared;
    @Mock
    private CallStack<SpanEvent> callStack;
    @Mock
    private Storage storage;
    @Mock
    private SpanRecorder spanRecorder;
    @Mock
    private WrappedSpanEventRecorder wrappedSpanEventRecorder;
    @Mock
    private SpanEventRecorder spanEventRecorder;
    @Mock
    private LocalAsyncId localAsyncId;
    @Mock
    private AsyncContext asyncContext;

    @Test
    void childTrace() {
        Trace trace = new ChildTrace(traceRoot, callStack, storage, spanRecorder, wrappedSpanEventRecorder, localAsyncId, false);

        assertCarries(trace, DefaultAsyncContextTraceBlock.class);
        assertThat(trace.getTraceBlock()).isExactlyInstanceOf(DefaultTraceBlock.class);
    }

    @Test
    void disableChildTrace() {
        Trace trace = new DisableChildTrace(localTraceRoot, spanRecorder, spanEventRecorder);

        assertCarries(trace, DisableAsyncContextTraceBlock.class);
        assertThat(trace.getTraceBlock()).isExactlyInstanceOf(DisableTraceBlock.class);
    }

    @Test
    void disableTrace() {
        when(localTraceRoot.getShared()).thenReturn(shared);
        Trace trace = new DisableTrace(localTraceRoot, spanRecorder, spanEventRecorder, CloseListener.EMPTY);

        assertCarries(trace, DisableAsyncContextTraceBlock.class);
        assertThat(trace.getTraceBlock()).isExactlyInstanceOf(DisableTraceBlock.class);
    }

    private void assertCarries(Trace trace, Class<? extends AsyncContextTraceBlock> expected) {
        TraceBlock block = trace.getTraceBlock(asyncContext);

        assertThat(block).isExactlyInstanceOf(expected);
        assertThat(((AsyncContextTraceBlock) block).getAsyncContext()).isSameAs(asyncContext);
        assertThat(block.getTrace()).isSameAs(trace);
        assertThat(block.isBegin()).isFalse();
    }
}
