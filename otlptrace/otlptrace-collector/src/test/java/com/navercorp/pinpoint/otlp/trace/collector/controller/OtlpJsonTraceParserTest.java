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

package com.navercorp.pinpoint.otlp.trace.collector.controller;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.common.v1.KeyValueList;
import io.opentelemetry.proto.resource.v1.Resource;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OtlpJsonTraceParserTest {

    private static final String TRACE_ID_HEX = "0102030405060708090a0b0c0d0e0f10";
    private static final String SPAN_ID_HEX = "1112131415161718";
    private static final String PARENT_SPAN_ID_HEX = "2122232425262728";
    private static final String LINK_TRACE_ID_HEX = "3132333435363738393a3b3c3d3e3f30";
    private static final String LINK_SPAN_ID_HEX = "4142434445464748";

    private static final ByteString TRACE_ID = hexBytes(TRACE_ID_HEX);
    private static final ByteString SPAN_ID = hexBytes(SPAN_ID_HEX);
    private static final ByteString PARENT_SPAN_ID = hexBytes(PARENT_SPAN_ID_HEX);

    private static ByteString hexBytes(String hex) {
        return ByteString.copyFrom(HexFormat.of().parseHex(hex));
    }

    private static ExportTraceServiceRequest parse(String json) {
        return OtlpJsonTraceParser.parse(json.getBytes(StandardCharsets.UTF_8));
    }

    private static Span firstSpan(ExportTraceServiceRequest request) {
        return request.getResourceSpans(0).getScopeSpans(0).getSpans(0);
    }

    private static String spanRequest(String spanJson) {
        return "{\"resourceSpans\":[{\"scopeSpans\":[{\"spans\":[" + spanJson + "]}]}]}";
    }

    @Test
    void hexIds_lowercase() {
        ExportTraceServiceRequest request = parse(spanRequest(
                "{\"traceId\":\"" + TRACE_ID_HEX + "\",\"spanId\":\"" + SPAN_ID_HEX + "\"," +
                        "\"parentSpanId\":\"" + PARENT_SPAN_ID_HEX + "\",\"name\":\"op\"}"));

        Span span = firstSpan(request);
        assertThat(span.getTraceId()).isEqualTo(TRACE_ID);
        assertThat(span.getSpanId()).isEqualTo(SPAN_ID);
        assertThat(span.getParentSpanId()).isEqualTo(PARENT_SPAN_ID);
    }

    @Test
    void hexIds_uppercase() {
        ExportTraceServiceRequest request = parse(spanRequest(
                "{\"traceId\":\"" + TRACE_ID_HEX.toUpperCase() + "\",\"spanId\":\"" + SPAN_ID_HEX.toUpperCase() + "\"}"));

        Span span = firstSpan(request);
        assertThat(span.getTraceId()).isEqualTo(TRACE_ID);
        assertThat(span.getSpanId()).isEqualTo(SPAN_ID);
    }

    @Test
    void linkIds_converted() {
        ExportTraceServiceRequest request = parse(spanRequest(
                "{\"traceId\":\"" + TRACE_ID_HEX + "\",\"spanId\":\"" + SPAN_ID_HEX + "\"," +
                        "\"links\":[{\"traceId\":\"" + LINK_TRACE_ID_HEX + "\",\"spanId\":\"" + LINK_SPAN_ID_HEX + "\"}]}"));

        Span.Link link = firstSpan(request).getLinks(0);
        assertThat(link.getTraceId()).isEqualTo(hexBytes(LINK_TRACE_ID_HEX));
        assertThat(link.getSpanId()).isEqualTo(hexBytes(LINK_SPAN_ID_HEX));
    }

    @Test
    void snakeCaseFieldNames_accepted() {
        ExportTraceServiceRequest request = parse(
                "{\"resource_spans\":[{\"scope_spans\":[{\"spans\":[" +
                        "{\"trace_id\":\"" + TRACE_ID_HEX + "\",\"span_id\":\"" + SPAN_ID_HEX + "\"," +
                        "\"parent_span_id\":\"" + PARENT_SPAN_ID_HEX + "\",\"start_time_unix_nano\":\"123\"}]}]}]}");

        Span span = firstSpan(request);
        assertThat(span.getTraceId()).isEqualTo(TRACE_ID);
        assertThat(span.getSpanId()).isEqualTo(SPAN_ID);
        assertThat(span.getParentSpanId()).isEqualTo(PARENT_SPAN_ID);
        assertThat(span.getStartTimeUnixNano()).isEqualTo(123L);
    }

    @Test
    void enum_byNameAndByNumber() {
        Span byName = firstSpan(parse(spanRequest("{\"kind\":\"SPAN_KIND_SERVER\"}")));
        Span byNumber = firstSpan(parse(spanRequest("{\"kind\":2}")));

        assertThat(byName.getKind()).isEqualTo(Span.SpanKind.SPAN_KIND_SERVER);
        assertThat(byNumber.getKind()).isEqualTo(Span.SpanKind.SPAN_KIND_SERVER);
    }

    @Test
    void int64_stringAndNumber() {
        Span byString = firstSpan(parse(spanRequest("{\"startTimeUnixNano\":\"1700000000000000001\"}")));
        Span byNumber = firstSpan(parse(spanRequest("{\"endTimeUnixNano\":1700000000000000002}")));

        assertThat(byString.getStartTimeUnixNano()).isEqualTo(1700000000000000001L);
        assertThat(byNumber.getEndTimeUnixNano()).isEqualTo(1700000000000000002L);
    }

    @Test
    void unknownFields_skipped() {
        ExportTraceServiceRequest request = parse(spanRequest(
                "{\"traceId\":\"" + TRACE_ID_HEX + "\",\"notAnOtlpField\":{\"nested\":[1,2,3]}}"));

        assertThat(firstSpan(request).getTraceId()).isEqualTo(TRACE_ID);
    }

    /**
     * The depth-agnostic field-name rewrite must not touch user data: attribute keys and string
     * values are JSON <i>values</i> ({@code {"key": ..., "value": {"stringValue": ...}}}), never
     * field names, so a user string "traceId" (or a hex-looking value) passes through verbatim.
     */
    @Test
    void attributeValues_notRewritten() {
        ExportTraceServiceRequest request = parse(spanRequest(
                "{\"traceId\":\"" + TRACE_ID_HEX + "\",\"attributes\":[" +
                        "{\"key\":\"traceId\",\"value\":{\"stringValue\":\"" + TRACE_ID_HEX + "\"}}," +
                        "{\"key\":\"nested\",\"value\":{\"kvlistValue\":{\"values\":[" +
                        "{\"key\":\"spanId\",\"value\":{\"stringValue\":\"plain\"}}]}}}]}"));

        Span span = firstSpan(request);
        KeyValue first = span.getAttributes(0);
        assertThat(first.getKey()).isEqualTo("traceId");
        assertThat(first.getValue().getStringValue()).isEqualTo(TRACE_ID_HEX);

        KeyValueList nested = span.getAttributes(1).getValue().getKvlistValue();
        assertThat(nested.getValues(0).getKey()).isEqualTo("spanId");
        assertThat(nested.getValues(0).getValue().getStringValue()).isEqualTo("plain");
    }

    @Test
    void topLevelArray_rejected() {
        // ExportTraceServiceRequest is a message, so the document root must be an object.
        assertThatThrownBy(() -> parse("[]")).isInstanceOf(OtlpTraceParseException.class);
    }

    @Test
    void emptyBody_rejected() {
        assertThatThrownBy(() -> OtlpJsonTraceParser.parse(new byte[0])).isInstanceOf(OtlpTraceParseException.class);
    }

    @Test
    void oddLengthHex_rejected() {
        assertThatThrownBy(() -> parse(spanRequest("{\"traceId\":\"abc\"}")))
                .isInstanceOf(OtlpTraceParseException.class);
    }

    @Test
    void nonHexCharacters_rejected() {
        assertThatThrownBy(() -> parse(spanRequest("{\"traceId\":\"zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz\"}")))
                .isInstanceOf(OtlpTraceParseException.class);
    }

    @Test
    void malformedJson_rejected() {
        assertThatThrownBy(() -> parse("{\"resourceSpans\":["))
                .isInstanceOf(OtlpTraceParseException.class);
    }

    @Test
    void malformedJson_messageKeepsCauseAndPositionOnly() {
        // Jackson's default message repeats the JsonLocation boilerplate ("[Source: REDACTED (...);
        // line: 1, column: N]") up to twice — ~265 chars for this input. Only the cause and the
        // position are forwarded to the client.
        assertThatThrownBy(() -> parse("{\"resourceSpans\":[}"))
                .isInstanceOf(OtlpTraceParseException.class)
                .hasMessage("Unexpected close marker '}': expected ']' (for Array starting at line 1, column 18) (line 1, column 19)");
        assertThatThrownBy(() -> parse("{\"resourceSpans\":[{\"scopeSpans\":"))
                .hasMessage("Unexpected end-of-input within/between Object entries (line 1, column 33)");
    }

    /**
     * A non-string ID value skips the hex rewrite; {@link com.google.protobuf.util.JsonFormat} then
     * coerces the number through its lenient base64 handling instead of erroring. Pinned as accepted:
     * the garbage bytes it yields are rejected downstream by {@code OtlpIdValidator}, which owns ID
     * validation for both transports.
     */
    @Test
    void nonStringIdValue_pinnedAsAccepted() {
        ExportTraceServiceRequest request = parse(spanRequest("{\"traceId\":123,\"name\":\"op\"}"));
        assertThat(firstSpan(request).getName()).isEqualTo("op");
    }

    /**
     * Pins the design §6-1 question: {@link com.google.protobuf.util.JsonFormat} accepts out-of-range
     * enum numbers for proto3 open enums (same leniency as OTel's pdata), so a request carrying a
     * future span kind is preserved as UNRECOGNIZED rather than failing the whole export with 400.
     */
    @Test
    void outOfRangeEnumNumber_pinnedAsAccepted() {
        Span span = firstSpan(parse(spanRequest("{\"kind\":99}")));
        assertThat(span.getKindValue()).isEqualTo(99);
        assertThat(span.getKind()).isEqualTo(Span.SpanKind.UNRECOGNIZED);
    }

    @Test
    void emptyIdString_passesThroughAsEmptyBytes() {
        Span span = firstSpan(parse(spanRequest("{\"traceId\":\"\",\"name\":\"op\"}")));
        assertThat(span.getTraceId()).isEqualTo(ByteString.EMPTY);
    }

    /**
     * Cross-transport identity (design §5): the same span parsed from protobuf bytes and from
     * OTLP/JSON must produce bit-identical {@code ExportTraceServiceRequest}s — in particular the
     * raw ID bytes the storage layer derives transactionId/spanId from.
     */
    @Test
    void crossTransportIdentity() {
        ExportTraceServiceRequest expected = ExportTraceServiceRequest.newBuilder()
                .addResourceSpans(ResourceSpans.newBuilder()
                        .setResource(Resource.newBuilder()
                                .addAttributes(KeyValue.newBuilder()
                                        .setKey("service.name")
                                        .setValue(AnyValue.newBuilder().setStringValue("svc"))))
                        .addScopeSpans(ScopeSpans.newBuilder()
                                .addSpans(Span.newBuilder()
                                        .setTraceId(TRACE_ID)
                                        .setSpanId(SPAN_ID)
                                        .setParentSpanId(PARENT_SPAN_ID)
                                        .setName("op")
                                        .setKind(Span.SpanKind.SPAN_KIND_INTERNAL)
                                        .setStartTimeUnixNano(1700000000000000001L)
                                        .setEndTimeUnixNano(1700000000000000002L)
                                        .addAttributes(KeyValue.newBuilder()
                                                .setKey("input_tokens")
                                                .setValue(AnyValue.newBuilder().setIntValue(42)))
                                        .addLinks(Span.Link.newBuilder()
                                                .setTraceId(hexBytes(LINK_TRACE_ID_HEX))
                                                .setSpanId(hexBytes(LINK_SPAN_ID_HEX)))
                                        .setStatus(Status.newBuilder().setCode(Status.StatusCode.STATUS_CODE_OK)))))
                .build();

        String json = "{\"resourceSpans\":[{" +
                "\"resource\":{\"attributes\":[{\"key\":\"service.name\",\"value\":{\"stringValue\":\"svc\"}}]}," +
                "\"scopeSpans\":[{\"spans\":[{" +
                "\"traceId\":\"" + TRACE_ID_HEX + "\"," +
                "\"spanId\":\"" + SPAN_ID_HEX + "\"," +
                "\"parentSpanId\":\"" + PARENT_SPAN_ID_HEX + "\"," +
                "\"name\":\"op\"," +
                "\"kind\":\"SPAN_KIND_INTERNAL\"," +
                "\"startTimeUnixNano\":\"1700000000000000001\"," +
                "\"endTimeUnixNano\":\"1700000000000000002\"," +
                "\"attributes\":[{\"key\":\"input_tokens\",\"value\":{\"intValue\":\"42\"}}]," +
                "\"links\":[{\"traceId\":\"" + LINK_TRACE_ID_HEX + "\",\"spanId\":\"" + LINK_SPAN_ID_HEX + "\"}]," +
                "\"status\":{\"code\":\"STATUS_CODE_OK\"}" +
                "}]}]}]}";

        ExportTraceServiceRequest fromJson = parse(json);
        ExportTraceServiceRequest fromProtobuf = parseProtobuf(expected.toByteArray());

        assertThat(fromJson).isEqualTo(expected);
        assertThat(fromJson).isEqualTo(fromProtobuf);
    }

    private static ExportTraceServiceRequest parseProtobuf(byte[] bytes) {
        try {
            return ExportTraceServiceRequest.parseFrom(bytes);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            throw new AssertionError(e);
        }
    }
}
