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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OtlpIdValidatorTest {

    private static ByteString bytes(int len, int fill) {
        final byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            b[i] = (byte) fill;
        }
        return ByteString.copyFrom(b);
    }

    private static ByteString of(int... values) {
        final byte[] b = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            b[i] = (byte) values[i];
        }
        return ByteString.copyFrom(b);
    }

    // --- trace id ---

    @Test
    void validTraceId_16BytesNonZero() {
        final ByteString id = bytes(16, 1);
        assertThat(OtlpIdValidator.isValidTraceId(id)).isTrue();
        assertThat(OtlpIdValidator.validateTraceId(id)).hasSize(16);
    }

    @Test
    void traceId_wrongLength_rejected() {
        assertThat(OtlpIdValidator.isValidTraceId(bytes(8, 1))).isFalse();
        assertThat(OtlpIdValidator.isValidTraceId(bytes(17, 1))).isFalse();
        assertThatThrownBy(() -> OtlpIdValidator.validateTraceId(bytes(8, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("length");
    }

    @Test
    void traceId_empty_rejected() {
        assertThat(OtlpIdValidator.isValidTraceId(ByteString.EMPTY)).isFalse();
        assertThatThrownBy(() -> OtlpIdValidator.validateTraceId(ByteString.EMPTY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void traceId_allZero_rejected() {
        assertThat(OtlpIdValidator.isValidTraceId(bytes(16, 0))).isFalse();
        assertThatThrownBy(() -> OtlpIdValidator.validateTraceId(bytes(16, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("all-zero");
    }

    // --- span id ---

    @Test
    void validSpanId_8BytesNonZero_parsedBigEndian() {
        final ByteString id = of(0, 0, 0, 0, 0, 0, 0, 1);
        assertThat(OtlpIdValidator.isValidSpanId(id)).isTrue();
        assertThat(OtlpIdValidator.validateSpanId(id)).isEqualTo(1L);
    }

    @Test
    void spanId_longerThan8_rejectedNotTruncated() {
        // Regression: a >8-byte span ID used to be silently truncated to its first 8 bytes.
        assertThat(OtlpIdValidator.isValidSpanId(bytes(9, 1))).isFalse();
        assertThatThrownBy(() -> OtlpIdValidator.validateSpanId(bytes(9, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("length");
    }

    @Test
    void spanId_shorterThan8_rejected() {
        assertThat(OtlpIdValidator.isValidSpanId(bytes(4, 1))).isFalse();
        assertThatThrownBy(() -> OtlpIdValidator.validateSpanId(bytes(4, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void spanId_allZero_rejected() {
        assertThat(OtlpIdValidator.isValidSpanId(bytes(8, 0))).isFalse();
        assertThatThrownBy(() -> OtlpIdValidator.validateSpanId(bytes(8, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("all-zero");
    }

    // --- parent span id ---

    @Test
    void parentSpanId_absent_isValid() {
        assertThat(OtlpIdValidator.isValidParentSpanId(ByteString.EMPTY)).isTrue();
    }

    @Test
    void parentSpanId_presentValid_isValid() {
        assertThat(OtlpIdValidator.isValidParentSpanId(bytes(8, 9))).isTrue();
    }

    @Test
    void parentSpanId_presentInvalid_isInvalid() {
        assertThat(OtlpIdValidator.isValidParentSpanId(bytes(8, 0))).isFalse();
        assertThat(OtlpIdValidator.isValidParentSpanId(bytes(9, 1))).isFalse();
    }
}
