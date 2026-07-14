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

import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Resolves the gRPC result status to promote to the {@code grpc.status} (160) annotation,
 * shared by the root-span ({@link OtlpTraceSpanMapper}) and SpanEvent
 * ({@link OtlpTraceSpanEventMapper}) paths. The native gRPC client plugin records the status
 * <em>name</em> ({@code io.grpc Status.getCode().name()}, e.g. "OK" / "UNAVAILABLE") on the
 * client SpanEvent, so the OTel numeric code is translated to the same representation.
 * The native agent has no gRPC <em>server</em> plugin — the root-span promotion is
 * OTel-only added value.
 *
 * <p>Key precedence: standard RPC semconv {@code rpc.grpc.status_code} (int 0-16) before the
 * nonstandard {@code grpc.status_code} (numeric string, emitted by e.g. the ASP.NET Core gRPC
 * instrumentation). Exposing the source key lets the caller exclude only the consumed key from
 * the raw attributes via the consumedKeys mechanism.</p>
 */
public final class OtlpGrpcStatusResolver {

    // gRPC status code → canonical name, fixed by the gRPC spec (code 0-16). Hardcoded to
    // avoid an io.grpc dependency; values match io.grpc Status.Code.name(), which is what the
    // native gRPC client plugin records into the grpc.status annotation.
    private static final String[] STATUS_NAMES = {
            "OK",                   // 0
            "CANCELLED",            // 1
            "UNKNOWN",              // 2
            "INVALID_ARGUMENT",     // 3
            "DEADLINE_EXCEEDED",    // 4
            "NOT_FOUND",            // 5
            "ALREADY_EXISTS",       // 6
            "PERMISSION_DENIED",    // 7
            "RESOURCE_EXHAUSTED",   // 8
            "FAILED_PRECONDITION",  // 9
            "ABORTED",              // 10
            "OUT_OF_RANGE",         // 11
            "UNIMPLEMENTED",        // 12
            "INTERNAL",             // 13
            "UNAVAILABLE",          // 14
            "DATA_LOSS",            // 15
            "UNAUTHENTICATED",      // 16
    };

    private OtlpGrpcStatusResolver() {
    }

    /** Promoted gRPC status name together with the source attribute key it was read from. */
    public record GrpcStatus(String name, String sourceKey) {
    }

    /**
     * Returns the promoted gRPC status or {@code null} when no numeric status is present.
     * The numeric value may arrive typed as int (standard semconv) or as a numeric string
     * (nonstandard variant) — both forms are accepted per key.
     */
    @Nullable
    public static GrpcStatus resolve(Map<String, AttributeValue> attributes) {
        for (String key : OtlpTraceConstants.GRPC_STATUS_CODE_KEYS) {
            final long code = OtlpHttpStatusResolver.resolveStatusCode(attributes, key);
            if (code != -1) {
                return new GrpcStatus(toStatusName(code), key);
            }
        }
        return null;
    }

    /** Canonical gRPC status name for the code, or the raw number for out-of-range codes. */
    static String toStatusName(long code) {
        if (code >= 0 && code < STATUS_NAMES.length) {
            return STATUS_NAMES[(int) code];
        }
        return String.valueOf(code);
    }
}
