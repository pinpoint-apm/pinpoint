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

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpanHeaderTest {

    @Test
    void of_roundTrip() {
        // guards the of() switch against a missing case when a constant is added
        for (SpanHeader header : SpanHeader.values()) {
            assertThat(SpanHeader.of(header.getCode()))
                    .as("of(%s.getCode())", header)
                    .isSameAs(header);
        }
    }

    @Test
    void of_unknownCode() {
        assertThat(SpanHeader.of(SpanEncoder.TYPE_PASSIVE_SPAN)).isNull();
        assertThat(SpanHeader.of(SpanEncoder.TYPE_INDEX)).isNull();
        assertThat(SpanHeader.of((byte) -1)).isNull();
    }

    @Test
    void spanChunk_discriminator() {
        assertThat(SpanHeader.SPAN.isSpanChunk()).isFalse();
        assertThat(SpanHeader.OTEL_SPAN.isSpanChunk()).isFalse();
        assertThat(SpanHeader.SPAN_CHUNK.isSpanChunk()).isTrue();
        assertThat(SpanHeader.OTEL_SPAN_CHUNK.isSpanChunk()).isTrue();

        assertThat(SpanHeader.SPAN_UID.isSpanChunk()).isFalse();
        assertThat(SpanHeader.OTEL_SPAN_UID.isSpanChunk()).isFalse();
        assertThat(SpanHeader.SPAN_CHUNK_UID.isSpanChunk()).isTrue();
        assertThat(SpanHeader.OTEL_SPAN_CHUNK_UID.isSpanChunk()).isTrue();
    }

    @Test
    void serviceUid_discriminator() {
        assertThat(SpanHeader.SPAN.hasServiceUid()).isFalse();
        assertThat(SpanHeader.SPAN_CHUNK.hasServiceUid()).isFalse();
        assertThat(SpanHeader.OTEL_SPAN.hasServiceUid()).isFalse();
        assertThat(SpanHeader.OTEL_SPAN_CHUNK.hasServiceUid()).isFalse();

        assertThat(SpanHeader.SPAN_UID.hasServiceUid()).isTrue();
        assertThat(SpanHeader.SPAN_CHUNK_UID.hasServiceUid()).isTrue();
        assertThat(SpanHeader.OTEL_SPAN_UID.hasServiceUid()).isTrue();
        assertThat(SpanHeader.OTEL_SPAN_CHUNK_UID.hasServiceUid()).isTrue();
    }
}
