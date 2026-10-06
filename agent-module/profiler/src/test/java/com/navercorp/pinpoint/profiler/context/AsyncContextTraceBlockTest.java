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
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The AsyncContext-carrying blocks add only the context: recording, begin and close behave
 * exactly like the plain blocks they extend.
 */
@ExtendWith(MockitoExtension.class)
class AsyncContextTraceBlockTest {

    @Mock
    private Trace trace;
    @Mock
    private AsyncContext asyncContext;
    @Mock
    private SpanEventRecorder recorder;

    @Test
    void defaultBlock_carriesTheContextAndDelegatesLikeTheParent() {
        when(trace.currentSpanEventRecorder()).thenReturn(recorder);
        DefaultAsyncContextTraceBlock block = new DefaultAsyncContextTraceBlock(trace, asyncContext);

        assertThat(block.getAsyncContext()).isSameAs(asyncContext);
        assertThat(block.getTrace()).isSameAs(trace);
        assertThat(block.isBegin()).isFalse();

        block.begin();
        block.recordServiceType(ServiceType.ASYNC);
        block.close();

        assertThat(block.isBegin()).isTrue();
        verify(trace).traceBlockBegin();
        verify(recorder).recordServiceType(ServiceType.ASYNC);
        verify(trace).traceBlockEnd();
        assertThat(block.toString()).contains("DefaultAsyncContextTraceBlock");
    }

    @Test
    void defaultBlock_closeWithoutBegin_doesNotEndABlock() {
        DefaultAsyncContextTraceBlock block = new DefaultAsyncContextTraceBlock(trace, asyncContext);

        block.close();

        verify(trace, never()).traceBlockEnd();
    }

    @Test
    void defaultBlock_rejectsANullContext() {
        assertThatThrownBy(() -> new DefaultAsyncContextTraceBlock(trace, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("asyncContext");
    }

    @Test
    void disableBlock_carriesTheContextAndDelegatesLikeTheParent() {
        DisableAsyncContextTraceBlock block = new DisableAsyncContextTraceBlock(trace, recorder, asyncContext);

        assertThat(block.getAsyncContext()).isSameAs(asyncContext);
        assertThat(block.getTrace()).isSameAs(trace);

        block.begin();
        block.recordServiceType(ServiceType.ASYNC);
        block.close();

        assertThat(block.isBegin()).isTrue();
        verify(recorder).recordServiceType(ServiceType.ASYNC);
        verify(trace, never()).traceBlockBegin();
        verify(trace, never()).traceBlockEnd();
        assertThat(block.toString()).contains("DisableAsyncContextTraceBlock");
    }

    @Test
    void disableBlock_rejectsANullContext() {
        assertThatThrownBy(() -> new DisableAsyncContextTraceBlock(trace, recorder, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("asyncContext");
    }
}
