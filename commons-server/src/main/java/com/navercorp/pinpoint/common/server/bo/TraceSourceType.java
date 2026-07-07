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

/**
 * Identifies the origin of a span: a Pinpoint-instrumented agent or an OpenTelemetry collector.
 * <p>
 * The numeric code is persisted as a single byte (e.g. an HBase column value) to mark a stored
 * span without relying on {@code ServerTraceId} type inspection. Codes intentionally match
 * {@code PinpointServerTraceId.PREFIX_BYTE_SERVER_TRACE_ID} and
 * {@code OtelServerTraceId.PREFIX_BYTE_SERVER_TRACE_ID}.
 */
public enum TraceSourceType {
    PINPOINT((byte) 1),
    OPENTELEMETRY((byte) 2);

    private final byte code;

    TraceSourceType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return this.code;
    }

    public static TraceSourceType of(byte code) {
        if (code == PINPOINT.code) {
            return PINPOINT;
        }
        if (code == OPENTELEMETRY.code) {
            return OPENTELEMETRY;
        }
        throw new IllegalArgumentException("unknown TraceSourceType code:" + code);
    }
}
