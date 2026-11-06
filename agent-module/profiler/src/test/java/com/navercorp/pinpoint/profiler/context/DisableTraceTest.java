/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DisableTraceTest {

    private final SpanEventRecorder spanEventRecorder = mock(SpanEventRecorder.class);

    private Trace newTrace() {
        LocalTraceRoot traceRoot = TraceRoot.local("testAgent", 2, 1);
        SpanRecorder spanRecorder = mock(SpanRecorder.class);
        return new DisableTrace(traceRoot, spanRecorder, spanEventRecorder, CloseListener.EMPTY);
    }

    @Test
    public void traceBlockEnd_returnsToRootStack() {
        Trace trace = newTrace();
        assertThat(trace.isRootStack()).isTrue();

        trace.traceBlockBegin();
        assertThat(trace.isRootStack()).isFalse();

        trace.traceBlockEnd();
        assertThat(trace.isRootStack()).isTrue();
    }

    @Test
    public void traceBlockEnd_nested() {
        Trace trace = newTrace();

        trace.traceBlockBegin();
        trace.traceBlockBegin();
        assertThat(trace.isRootStack()).isFalse();

        trace.traceBlockEnd();
        assertThat(trace.isRootStack()).isFalse();

        trace.traceBlockEnd();
        assertThat(trace.isRootStack()).isTrue();
    }

    @Test
    public void traceBlockEnd_withStackId() {
        Trace trace = newTrace();

        trace.traceBlockBegin(100);
        assertThat(trace.isRootStack()).isFalse();

        trace.traceBlockEnd(100);
        assertThat(trace.isRootStack()).isTrue();
    }

    @Test
    public void traceBlockEnd_repeatedPairs() {
        Trace trace = newTrace();

        for (int i = 0; i < 3; i++) {
            trace.traceBlockBegin();
            trace.traceBlockEnd();
        }
        assertThat(trace.isRootStack()).isTrue();
    }

    @Test
    public void traceBlockBegin_returnsSharedRecorder() {
        Trace trace = newTrace();

        assertThat(trace.traceBlockBegin()).isSameAs(spanEventRecorder);
        assertThat(trace.currentSpanEventRecorder()).isSameAs(spanEventRecorder);
        trace.traceBlockEnd();
    }

    @Test
    public void traceBlock_doesNotChangeStack() {
        Trace trace = newTrace();

        TraceBlock traceBlock = trace.getTraceBlock();
        traceBlock.begin();
        assertThat(trace.isRootStack()).isTrue();

        traceBlock.close();
        assertThat(trace.isRootStack()).isTrue();
    }
}
