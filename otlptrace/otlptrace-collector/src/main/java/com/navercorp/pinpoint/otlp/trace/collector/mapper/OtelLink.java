package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.common.v1.KeyValue;

import java.util.List;

/**
 * Write-side model of the {@code OPENTELEMETRY_LINK} annotation JSON. Absent fields are
 * omitted from the output, matching the wire format read back by the web-side
 * {@code OtelLinkSerde}.
 *
 * @param traceId    raw OTel trace id bytes — hex-encoded at serialization time, only
 *                   serialized when non-empty
 * @param spanId     OTel span id as long, 0 when absent (an all-zero span id is invalid per
 *                   the OTel spec) — serialized as a decimal string to avoid JS Number
 *                   precision loss for any consumer that reads the raw annotation JSON
 *                   directly; Java readers accept both number and decimal-string forms
 * @param traceState raw W3C tracestate (vendor propagation context — AWS, Datadog, Sentry,
 *                   etc.) — capped at serialization time, only serialized when non-empty
 * @param attributes raw OTel link attributes — transformed (value capping) at serialization
 *                   time, only serialized when non-empty
 * @param dropped    SDK-side data-loss counter for link attributes, same convention as
 *                   Span/SpanEvent {@code OPENTELEMETRY_DROPPED} — only serialized when > 0
 */
public record OtelLink(ByteString traceId,
                            long spanId,
                            String traceState,
                            List<KeyValue> attributes,
                            int dropped) {
}
