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

import com.navercorp.pinpoint.common.server.trace.OtelServerTraceId;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TraceSourceTypeTest {

    @Test
    void codes() {
        assertThat(TraceSourceType.PINPOINT.getCode()).isEqualTo((byte) 1);
        assertThat(TraceSourceType.OPENTELEMETRY.getCode()).isEqualTo((byte) 2);
    }

    @Test
    void codes_alignedWithServerTraceIdPrefix() {
        // Persisted code byte must match the prefix byte used by ServerTraceId serialization
        // so a future reader can interpret either source as the same type tag.
        assertThat(TraceSourceType.PINPOINT.getCode())
                .isEqualTo(PinpointServerTraceId.PREFIX_BYTE_SERVER_TRACE_ID);
        assertThat(TraceSourceType.OPENTELEMETRY.getCode())
                .isEqualTo(OtelServerTraceId.PREFIX_BYTE_SERVER_TRACE_ID);
    }

    @Test
    void of_roundTrip() {
        for (TraceSourceType type : TraceSourceType.values()) {
            assertThat(TraceSourceType.of(type.getCode())).isSameAs(type);
        }
    }

    @Test
    void of_unknown() {
        assertThatThrownBy(() -> TraceSourceType.of((byte) 99))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
