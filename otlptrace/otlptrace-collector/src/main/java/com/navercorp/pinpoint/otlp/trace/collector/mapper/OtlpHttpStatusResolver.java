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
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Resolves the HTTP response status code to promote to an {@code HTTP_STATUS_CODE} annotation,
 * shared by the root-span ({@link OtlpTraceSpanMapper}) and SpanEvent ({@link OtlpTraceSpanEventMapper})
 * paths. This mirrors the native agent convention where both server spans
 * ({@code Span} via ServerResponseRecorder) and HTTP client SpanEvents (okhttp / httpclient /
 * resttemplate / … plugins via {@code SpanEventRecorder.recordAttribute(AnnotationKey.HTTP_STATUS_CODE, …)})
 * record the status as a first-class annotation.
 *
 * <p>Exposing the source key alongside the value lets the caller exclude only the raw attribute
 * key that was actually consumed, so a non-promoted status variant (or a non-numeric value that
 * could not be promoted) is retained instead of blanket-dropped.</p>
 */
public final class OtlpHttpStatusResolver {

    private OtlpHttpStatusResolver() {
    }

    /** Promoted HTTP status code together with the source attribute key it was read from. */
    public record ResponseStatus(int code, String sourceKey) {
    }

    /**
     * Returns the promoted HTTP status or {@code null} when no numeric status is present.
     * Precedence follows {@link OtlpTraceConstants#RESPONSE_STATUS_CODE_KEYS}: new semconv
     * ({@code http.response.status_code}) before legacy ({@code http.status_code}).
     */
    @Nullable
    public static ResponseStatus resolve(Map<String, AttributeValue> attributes) {
        for (String key : OtlpTraceConstants.RESPONSE_STATUS_CODE_KEYS) {
            final long code = resolveStatusCode(attributes, key);
            if (code != -1) {
                return new ResponseStatus((int) code, key);
            }
        }
        return null;
    }

    /**
     * Resolves an HTTP status code that may arrive typed as int or as string. The OTel HTTP
     * instrumentation emits it as an int, but Envoy emits {@code http.status_code} as a string
     * ("200"). Try the int form first, then a numeric-string fallback. Returns -1 when the key is
     * absent or non-numeric.
     */
    static long resolveStatusCode(Map<String, AttributeValue> attributes, String key) {
        final long intValue = AttributeUtils.getAttributeIntValue(attributes, key, -1L);
        if (intValue != -1) {
            return intValue;
        }
        final String stringValue = AttributeUtils.getAttributeStringValue(attributes, key, null);
        if (stringValue != null) {
            try {
                return Long.parseLong(stringValue.trim());
            } catch (NumberFormatException ignored) {
                return -1L;
            }
        }
        return -1L;
    }
}
