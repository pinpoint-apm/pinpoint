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

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.TraceSourceType;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.io.SpanVersion;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Focused tests for {@link SpanDecoderV0#decode} type-byte dispatch.
 * End-to-end round-trip is covered by {@code SpanEncoderTest}; this class targets
 * the dispatch logic and the {@link TraceSourceType} assignment in isolation.
 */
class SpanDecoderV0Test {

    private final SpanEncoderV0 encoder = new SpanEncoderV0();
    private final SpanDecoderV0 decoder = new SpanDecoderV0();

    @Test
    void decode_typeSpan_assignsPinpointSource() {
        SpanBo input = newMinimalSpan(TraceSourceType.PINPOINT);
        BasicSpan decoded = roundTripSpan(input);

        assertThat(decoded).isInstanceOf(SpanBo.class);
        assertThat(decoded.getTraceSourceType()).isEqualTo(TraceSourceType.PINPOINT);
    }

    @Test
    void decode_typeOtelSpan_assignsOpentelemetrySource() {
        SpanBo input = newMinimalSpan(TraceSourceType.OPENTELEMETRY);
        BasicSpan decoded = roundTripSpan(input);

        assertThat(decoded).isInstanceOf(SpanBo.class);
        assertThat(decoded.getTraceSourceType()).isEqualTo(TraceSourceType.OPENTELEMETRY);
    }

    @Test
    void decode_typeSpanChunk_assignsPinpointSource() {
        SpanChunkBo input = newMinimalSpanChunk(TraceSourceType.PINPOINT);
        BasicSpan decoded = roundTripSpanChunk(input);

        assertThat(decoded).isInstanceOf(SpanChunkBo.class);
        assertThat(decoded.getTraceSourceType()).isEqualTo(TraceSourceType.PINPOINT);
    }

    @Test
    void decode_typeOtelSpanChunk_assignsOpentelemetrySource() {
        SpanChunkBo input = newMinimalSpanChunk(TraceSourceType.OPENTELEMETRY);
        BasicSpan decoded = roundTripSpanChunk(input);

        assertThat(decoded).isInstanceOf(SpanChunkBo.class);
        assertThat(decoded.getTraceSourceType()).isEqualTo(TraceSourceType.OPENTELEMETRY);
    }

    @Test
    void decode_unknownType_returnsNull() {
        Buffer qualifier = new FixedBuffer(1);
        qualifier.putByte((byte) 99);
        qualifier.setOffset(0);
        Buffer value = new FixedBuffer(new byte[]{0});

        SpanDecodingContext ctx = new SpanDecodingContext(newTransactionId());
        BasicSpan decoded = decoder.decode(qualifier, value, ctx);

        assertThat(decoded).isNull();
    }

    private SpanBo newMinimalSpan(TraceSourceType type) {
        SpanBo span = new SpanBo(type);
        span.getSpanOwner().setAgentId("agent");
        span.getSpanOwner().setApplicationName("app");
        span.getSpanOwner().setAgentStartTime(100L);
        span.setSpanId(1L);
        span.setParentSpanId(-1L);
        span.setServiceType((short) 1000);
        span.setTraceTime(SpanVersion.TRACE_V2, System.currentTimeMillis(), 0);
        span.setCollectorAcceptTime(System.currentTimeMillis());
        return span;
    }

    private SpanChunkBo newMinimalSpanChunk(TraceSourceType type) {
        SpanChunkBo chunk = new SpanChunkBo(type);
        chunk.getSpanOwner().setAgentId("agent");
        chunk.getSpanOwner().setApplicationName("app");
        chunk.getSpanOwner().setAgentStartTime(100L);
        chunk.setSpanId(1L);
        chunk.setCollectorAcceptTime(System.currentTimeMillis());
        return chunk;
    }

    private BasicSpan roundTripSpan(SpanBo input) {
        SpanEncodingContext<SpanBo> encCtx = new SpanEncodingContext<>(input);
        Buffer qualifier = wrap(encoder.encodeSpanQualifier(encCtx));
        Buffer value = wrap(encoder.encodeSpanColumnValue(encCtx));

        SpanDecodingContext decCtx = new SpanDecodingContext(newTransactionId());
        decCtx.setCollectorAcceptedTime(input.getCollectorAcceptTime());
        return decoder.decode(qualifier, value, decCtx);
    }

    private BasicSpan roundTripSpanChunk(SpanChunkBo input) {
        SpanEncodingContext<SpanChunkBo> encCtx = new SpanEncodingContext<>(input);
        Buffer qualifier = wrap(encoder.encodeSpanChunkQualifier(encCtx));
        Buffer value = wrap(encoder.encodeSpanChunkColumnValue(encCtx));

        SpanDecodingContext decCtx = new SpanDecodingContext(newTransactionId());
        decCtx.setCollectorAcceptedTime(input.getCollectorAcceptTime());
        return decoder.decode(qualifier, value, decCtx);
    }

    private Buffer wrap(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        return new FixedBuffer(bytes);
    }

    private ServerTraceId newTransactionId() {
        return new PinpointServerTraceId("agent", 100L, 1L);
    }
}
