package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import io.opentelemetry.proto.common.v1.KeyValue;

import java.util.List;

/**
 * Write-side model of the {@code OPENTELEMETRY_EVENT} annotation JSON, wrapped in the
 * parent event name:
 * <pre>{@code {"<name>": {"time": <unix_nano>, "attributes": {...}}}}</pre>
 *
 * @param name       OTel event name — an event with an empty name is invalid and skipped
 * @param time       event timestamp in unix nanos
 * @param attributes raw OTel event attributes — transformed (value capping) at serialization
 *                   time, only serialized when non-empty
 */
public record OtelEvent(String name,
                        long time,
                        List<KeyValue> attributes) {
}
