/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Set;

/**
 * Normalizes the RPC operation name to the fully-qualified form of the RC semantic conventions,
 * {@code <service>/<method>} (e.g. {@code oteldemo.CheckoutService/PlaceOrder}).
 *
 * <p>Two attribute shapes arrive side by side while SDKs migrate:</p>
 * <ul>
 *   <li>RC (semconv 1.39+): {@code rpc.method} already carries the fully-qualified name and
 *       {@code rpc.service} is deprecated / absent.</li>
 *   <li>1.x: {@code rpc.service} = {@code oteldemo.CheckoutService}, {@code rpc.method} =
 *       {@code PlaceOrder}.</li>
 * </ul>
 * Without normalization the same gRPC operation would land under two keys ({@code PlaceOrder}
 * vs {@code oteldemo.CheckoutService/PlaceOrder}) in the transaction rpc, the exception
 * uriTemplate and URI stat, and bare 1.x method names collide across services ({@code Get},
 * {@code List}). The 1.x pair is therefore composed into the RC form; an RC value (contains a
 * {@code /}) is passed through untouched, including the {@code _OTHER} placeholder.
 *
 * <p>The resolved keys are added to {@code consumedKeys} so the caller filters only the
 * attributes that were promoted.</p>
 */
public final class OtlpRpcMethodResolver {

    private static final char SEPARATOR = '/';

    private OtlpRpcMethodResolver() {
    }

    /**
     * Fully-qualified RPC method, or {@code null} when {@code rpc.method} is absent or blank.
     * Consumes {@code rpc.method}, and {@code rpc.service} when it was composed in.
     */
    @Nullable
    public static String resolve(Map<String, AttributeValue> attributes, Set<String> consumedKeys) {
        final String method = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_RPC_METHOD, null);
        if (!hasText(method)) {
            return null;
        }
        final String trimmedMethod = method.trim();
        consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_RPC_METHOD);
        if (trimmedMethod.indexOf(SEPARATOR) >= 0) {
            // RC shape: already "<service>/<method>" (some senders keep a leading "/") — never
            // re-prefix a service.
            return trimmedMethod;
        }
        final String service = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_RPC_SERVICE, null);
        if (hasText(service)) {
            consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_RPC_SERVICE);
            return service.trim() + SEPARATOR + trimmedMethod;
        }
        // 1.x sender without rpc.service: nothing to qualify with, keep the bare method.
        return trimmedMethod;
    }

    /**
     * RPC service name for the endPoint fallback: {@code rpc.service} when a 1.x sender still
     * emits it, otherwise the service part of a fully-qualified RC {@code rpc.method}
     * ({@code oteldemo.CheckoutService/PlaceOrder} → {@code oteldemo.CheckoutService}; a leading
     * {@code /} is ignored). {@code null} when neither yields a service. Only {@code rpc.service}
     * is consumed here — {@code rpc.method} is consumed by {@link #resolve} on the rpc path.
     */
    @Nullable
    public static String resolveService(Map<String, AttributeValue> attributes, Set<String> consumedKeys) {
        final String service = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_RPC_SERVICE, null);
        if (hasText(service)) {
            consumedKeys.add(OtlpTraceConstants.ATTRIBUTE_KEY_RPC_SERVICE);
            return service.trim();
        }
        final String method = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_RPC_METHOD, null);
        if (!hasText(method)) {
            return null;
        }
        String qualified = method.trim();
        if (qualified.charAt(0) == SEPARATOR) {
            qualified = qualified.substring(1);
        }
        final int lastSeparator = qualified.lastIndexOf(SEPARATOR);
        if (lastSeparator <= 0) {
            return null;
        }
        return qualified.substring(0, lastSeparator);
    }

    private static boolean hasText(@Nullable String value) {
        if (value == null) {
            return false;
        }
        return !value.trim().isEmpty();
    }
}
