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

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DisableChildTraceTest {

    private Trace newTrace() {
        LocalTraceRoot traceRoot = mock(LocalTraceRoot.class);
        when(traceRoot.getShared()).thenReturn(mock(Shared.class));
        return new DisableChildTrace(traceRoot, mock(SpanRecorder.class), mock(SpanEventRecorder.class));
    }

    @Test
    void noArgBlockEndRestoresRootStack() {
        Trace trace = newTrace();
        assertThat(trace.isRootStack()).isTrue();

        trace.traceBlockBegin();
        assertThat(trace.isRootStack()).isFalse();

        trace.traceBlockEnd();
        assertThat(trace.isRootStack()).isTrue();
    }

    @Test
    void nestedNoArgBlockEnd() {
        Trace trace = newTrace();

        trace.traceBlockBegin();
        trace.traceBlockBegin();
        trace.traceBlockEnd();
        assertThat(trace.isRootStack()).isFalse();

        trace.traceBlockEnd();
        assertThat(trace.isRootStack()).isTrue();
    }

    @Test
    void stackIdBlockBeginEndIsBalanced() {
        Trace trace = newTrace();

        trace.traceBlockBegin(Trace.DEFAULT_STACKID);
        assertThat(trace.isRootStack()).isFalse();

        trace.traceBlockEnd(Trace.DEFAULT_STACKID);
        assertThat(trace.isRootStack()).isTrue();
    }
}
