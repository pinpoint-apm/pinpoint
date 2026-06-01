/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.sampling.tail;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class TraceErrorsTest {

    @Test
    void span_cleanSpan_noError() {
        assertThat(TraceErrors.hasError(new SpanBo())).isFalse();
    }

    @Test
    void span_errCode_isError() {
        SpanBo bo = new SpanBo();
        bo.setErrCode(1);
        assertThat(TraceErrors.hasError(bo)).isTrue();
    }

    @Test
    void span_spanEventException_isError() {
        SpanEventBo event = Mockito.mock(SpanEventBo.class);
        when(event.hasException()).thenReturn(true);
        SpanBo bo = new SpanBo();
        bo.addSpanEventBoList(List.of(event));
        assertThat(TraceErrors.hasError(bo)).isTrue();
    }

    @Test
    void chunk_cleanChunk_noError() {
        assertThat(TraceErrors.hasError(new SpanChunkBo())).isFalse();
    }

    @Test
    void chunk_spanEventException_isError() {
        SpanEventBo event = Mockito.mock(SpanEventBo.class);
        when(event.hasException()).thenReturn(true);
        SpanChunkBo bo = new SpanChunkBo();
        bo.addSpanEventBoList(List.of(event));
        assertThat(TraceErrors.hasError(bo)).isTrue();
    }
}
