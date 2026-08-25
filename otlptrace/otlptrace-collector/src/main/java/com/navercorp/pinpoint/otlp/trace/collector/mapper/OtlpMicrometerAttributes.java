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
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Spring Boot Actuator's micrometer-tracing (OTel bridge) does not emit OpenTelemetry semantic-convention
 * keys. Its HTTP observations copy the Micrometer {@code KeyValue}s onto the span as plain string tags:
 * <ul>
 *   <li>server: {@code method}, {@code uri} (route template), {@code status}, {@code outcome}, {@code exception},
 *       {@code http.url} (raw request path)</li>
 *   <li>client: the same plus {@code client.name}; {@code http.url} is the full URL</li>
 * </ul>
 * Without this mapping such spans get no HTTP status / method annotation and their rpc is the raw
 * {@code http.url} path instead of the route template (high cardinality).
 *
 * <p>These generic key names ({@code method}, {@code status}, {@code uri}) may legitimately mean something else in
 * other SDKs, so the mapping is gated on the instrumentation scope Spring Boot uses for its bridge
 * ({@value #SCOPE_SPRING_BOOT}, exact match) and only complements the semantic-convention keys: it is consulted
 * after them, never instead of them.</p>
 *
 * <p>Micrometer's {@code method} on {@code @Observed} (INTERNAL) spans is the Java method name, so callers must
 * restrict {@link #getHttpMethod} to SERVER / CLIENT spans.</p>
 */
public final class OtlpMicrometerAttributes {

    /** Instrumentation scope name of Spring Boot's micrometer-tracing OTel bridge. */
    public static final String SCOPE_SPRING_BOOT = "org.springframework.boot";

    public static final String KEY_URI = "uri";
    public static final String KEY_METHOD = "method";
    public static final String KEY_STATUS = "status";

    /**
     * Values Spring's {@code DefaultServerRequestObservationConvention} puts in {@code uri} when no handler route
     * matched. They carry no path information, so the rpc falls back to the {@code http.url} path instead.
     */
    static final Set<String> URI_PLACEHOLDERS = Set.of("/**", "UNKNOWN", "REDIRECTION", "NOT_FOUND", "root");

    private OtlpMicrometerAttributes() {
    }

    public static boolean isMicrometerScope(@Nullable InstrumentationScope scope) {
        return scope != null && SCOPE_SPRING_BOOT.equals(scope.getName());
    }

    /**
     * Route template from {@code uri}, or {@code null} when absent or a no-route placeholder (the caller then
     * continues with its own fallback chain). The key is consumed only when a template is returned.
     */
    @Nullable
    public static String getUriTemplate(Map<String, AttributeValue> attributes, Set<String> consumedKeys) {
        final String uri = AttributeUtils.getAttributeStringValue(attributes, KEY_URI, null);
        if (uri == null || uri.isEmpty() || URI_PLACEHOLDERS.contains(uri)) {
            return null;
        }
        consumedKeys.add(KEY_URI);
        return uri;
    }

    /**
     * HTTP status from the string-typed {@code status} tag ("200"). Non-numeric values such as the client-side
     * {@code IO_ERROR} / {@code CLIENT_ERROR} are not promoted and stay in the raw attribute list.
     */
    public static OtlpHttpStatusResolver.@Nullable ResponseStatus getResponseStatus(Map<String, AttributeValue> attributes) {
        final long code = OtlpHttpStatusResolver.resolveStatusCode(attributes, KEY_STATUS);
        if (code == -1) {
            return null;
        }
        return new OtlpHttpStatusResolver.ResponseStatus((int) code, KEY_STATUS);
    }

    /** HTTP request method from {@code method}; Micrometer uses {@code "none"} for unknown methods → not promoted. */
    @Nullable
    public static String getHttpMethod(Map<String, AttributeValue> attributes, Set<String> consumedKeys) {
        final String method = AttributeUtils.getAttributeStringValue(attributes, KEY_METHOD, null);
        if (method == null || method.isEmpty() || "none".equals(method)) {
            return null;
        }
        consumedKeys.add(KEY_METHOD);
        return method;
    }
}
