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

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.io.SpanVersion;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpanBoTimeTest {

    @Test
    void spanStartTimeUsesVersionUnit() {
        SpanBo v2 = new SpanBo();
        v2.setTraceTime(SpanVersion.TRACE_V2, 2, 0);

        assertThat(v2.getStartTimeMillis()).isEqualTo(2);
        assertThat(v2.getStartTimeNanos()).isEqualTo(TimeUnit.MILLISECONDS.toNanos(2));

        SpanBo v3 = new SpanBo();
        v3.setTraceTime(SpanVersion.TRACE_V3, 2_500_000, 3_000_000, 0);

        assertThat(v3.getStartTimeMillis()).isEqualTo(2);
        assertThat(v3.getStartTimeNanos()).isEqualTo(2_500_000);
    }

    @Test
    void spanEndTimeFallsBackToElapsedMillis() {
        SpanBo span = new SpanBo();
        span.setTraceTime(SpanVersion.TRACE_V2, 2, 3);

        assertThat(span.hasEndTime()).isFalse();
        assertThat(span.getEndTimeMillis()).isEqualTo(5);
        assertThat(span.getEndTimeNanos()).isEqualTo(5_000_000);
    }

    @Test
    void spanEndTimeKeepsDefaultSentinel() {
        SpanBo span = new SpanBo();
        span.setVersion(SpanVersion.TRACE_V3);

        assertThat(span.hasEndTime()).isFalse();
    }

    @Test
    void spanTraceTimeSeparatesPreV3AndV3Contracts() {
        SpanBo span = new SpanBo();

        assertThatThrownBy(() -> span.setTraceTime(SpanVersion.TRACE_V3, 2, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TRACE_V3 span requires absolute start/end time");
        assertThatThrownBy(() -> span.setTraceTime(SpanVersion.TRACE_V2, 2, SpanBo.DEFAULT_END_TIME, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("only supported for TRACE_V3 spans");
    }

    @Test
    void spanChunkKeyTimeUsesVersionUnit() {
        SpanChunkBo v2 = new SpanChunkBo();
        v2.setTraceTime(SpanVersion.TRACE_V2, 2);

        assertThat(v2.getKeyTimeMillis()).isEqualTo(2);
        assertThat(v2.getKeyTimeNanos()).isEqualTo(TimeUnit.MILLISECONDS.toNanos(2));

        SpanChunkBo v3 = new SpanChunkBo();
        v3.setTraceTime(SpanVersion.TRACE_V3, 2_500_000);

        assertThat(v3.getKeyTimeMillis()).isEqualTo(2);
        assertThat(v3.getKeyTimeNanos()).isEqualTo(2_500_000);
    }

    @Test
    void spanEventTimeUsesExplicitV3Nanos() {
        SpanEventBo spanEvent = new SpanEventBo();
        spanEvent.setTraceTime(SpanVersion.TRACE_V3, 2_000_001, 2_500_002, 1);

        assertThat(spanEvent.hasStartTime()).isTrue();
        assertThat(spanEvent.hasEndTime()).isTrue();
        assertThat(spanEvent.getStartTimeNanos()).isEqualTo(2_000_001);
        assertThat(spanEvent.getEndTimeNanos()).isEqualTo(2_500_002);
        assertThat(spanEvent.getStartElapsed()).isEqualTo(1);
        assertThat(spanEvent.getEndElapsed()).isZero();
    }

    @Test
    void spanEventTimeRequiresExplicitV3Timestamp() {
        SpanEventBo spanEvent = new SpanEventBo();

        assertThat(spanEvent.hasStartTime()).isFalse();
        assertThat(spanEvent.hasEndTime()).isFalse();
        assertThatThrownBy(spanEvent::getStartTimeNanos)
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(spanEvent::getEndTimeNanos)
                .isInstanceOf(IllegalStateException.class);
    }
}
