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

package com.navercorp.pinpoint.otlp.trace.collector.util;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OtlpJsonIdsTest {

    private static final String TRACE_ID_HEX = "0102030405060708090a0b0c0d0e0f10";
    private static final String SPAN_ID_HEX = "1112131415161718";
    private static final String LINK_TRACE_ID_HEX = "3132333435363738393a3b3c3d3e3f30";
    private static final String LINK_SPAN_ID_HEX = "4142434445464748";

    private static ByteString hexBytes(String hex) {
        return ByteString.copyFrom(HexFormat.of().parseHex(hex));
    }

    /** What JsonFormat makes of a hex ID: the hex text decoded as base64. */
    private static ByteString asJsonFormatWouldDecode(String hex) {
        return ByteString.copyFrom(Base64.getDecoder().decode(hex));
    }

    @Test
    void hexIdFromBase64Bytes_roundTrip() {
        ByteString traceAsBase64 = asJsonFormatWouldDecode(TRACE_ID_HEX);
        ByteString spanAsBase64 = asJsonFormatWouldDecode(SPAN_ID_HEX);
        assertThat(traceAsBase64.size()).isEqualTo(24);
        assertThat(spanAsBase64.size()).isEqualTo(12);

        assertThat(OtlpJsonIds.hexIdFromBase64Bytes(traceAsBase64)).isEqualTo(hexBytes(TRACE_ID_HEX));
        assertThat(OtlpJsonIds.hexIdFromBase64Bytes(spanAsBase64)).isEqualTo(hexBytes(SPAN_ID_HEX));
        assertThat(OtlpJsonIds.hexIdFromBase64Bytes(ByteString.EMPTY)).isEqualTo(ByteString.EMPTY);
        // Upper-case hex survives the round trip too (base64 is case-sensitive, hex is not).
        assertThat(OtlpJsonIds.hexIdFromBase64Bytes(asJsonFormatWouldDecode(TRACE_ID_HEX.toUpperCase())))
                .isEqualTo(hexBytes(TRACE_ID_HEX));
    }

    @Test
    void hexIdFromBase64Bytes_nonHexRoundTrip_rejected() {
        // A base64 ID decodes to 16 bytes whose re-encoding is not hex digits.
        ByteString base64Id = hexBytes(TRACE_ID_HEX);
        assertThatThrownBy(() -> OtlpJsonIds.hexIdFromBase64Bytes(base64Id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a hexadecimal digit");
        // "zz" as JsonFormat would decode it: 1 byte, re-encodes to "zw".
        assertThatThrownBy(() -> OtlpJsonIds.hexIdFromBase64Bytes(asJsonFormatWouldDecode("zz")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("not a hexadecimal digit: \"z\" = 122");
    }

    @Test
    void decodeHexIds_rewritesSpanAndLinkIds_inPlace() {
        ExportTraceServiceRequest.Builder request = ExportTraceServiceRequest.newBuilder()
                .addResourceSpans(ResourceSpans.newBuilder()
                        .addScopeSpans(ScopeSpans.newBuilder()
                                .addSpans(Span.newBuilder()
                                        .setTraceId(asJsonFormatWouldDecode(TRACE_ID_HEX))
                                        .setSpanId(asJsonFormatWouldDecode(SPAN_ID_HEX))
                                        // absent parent_span_id: empty stays empty
                                        .setName("op")
                                        .addLinks(Span.Link.newBuilder()
                                                .setTraceId(asJsonFormatWouldDecode(LINK_TRACE_ID_HEX))
                                                .setSpanId(asJsonFormatWouldDecode(LINK_SPAN_ID_HEX))))
                                .addSpans(Span.newBuilder()
                                        .setTraceId(asJsonFormatWouldDecode(TRACE_ID_HEX))
                                        .setSpanId(asJsonFormatWouldDecode(LINK_SPAN_ID_HEX))
                                        .setParentSpanId(asJsonFormatWouldDecode(SPAN_ID_HEX)))));

        OtlpJsonIds.decodeHexIds(request);
        ExportTraceServiceRequest decoded = request.build();

        Span first = decoded.getResourceSpans(0).getScopeSpans(0).getSpans(0);
        assertThat(first.getTraceId()).isEqualTo(hexBytes(TRACE_ID_HEX));
        assertThat(first.getSpanId()).isEqualTo(hexBytes(SPAN_ID_HEX));
        assertThat(first.getParentSpanId()).isEmpty();
        assertThat(first.getName()).isEqualTo("op");
        assertThat(first.getLinks(0).getTraceId()).isEqualTo(hexBytes(LINK_TRACE_ID_HEX));
        assertThat(first.getLinks(0).getSpanId()).isEqualTo(hexBytes(LINK_SPAN_ID_HEX));

        Span second = decoded.getResourceSpans(0).getScopeSpans(0).getSpans(1);
        assertThat(second.getParentSpanId()).isEqualTo(hexBytes(SPAN_ID_HEX));
        assertThat(second.getSpanId()).isEqualTo(hexBytes(LINK_SPAN_ID_HEX));
    }

    @Test
    void decodeHexIds_emptyRequest_noop() {
        ExportTraceServiceRequest.Builder request = ExportTraceServiceRequest.newBuilder();
        OtlpJsonIds.decodeHexIds(request);
        assertThat(request.build()).isEqualTo(ExportTraceServiceRequest.getDefaultInstance());
    }
}
