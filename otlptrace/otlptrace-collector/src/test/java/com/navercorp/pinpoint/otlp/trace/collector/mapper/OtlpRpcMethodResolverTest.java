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
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpRpcMethodResolverTest {

    private static Map<String, AttributeValue> attrs(String... keyValues) {
        Map<String, AttributeValue> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], AttributeValue.of(keyValues[i + 1]));
        }
        return map;
    }

    @Test
    void resolve_legacyPair_composedIntoFullyQualified_consumesBoth() {
        Set<String> consumed = new HashSet<>();
        String rpc = OtlpRpcMethodResolver.resolve(
                attrs("rpc.service", "oteldemo.CheckoutService", "rpc.method", "PlaceOrder"), consumed);
        assertThat(rpc).isEqualTo("oteldemo.CheckoutService/PlaceOrder");
        assertThat(consumed).containsExactlyInAnyOrder("rpc.service", "rpc.method");
    }

    @Test
    void resolve_rcFullyQualified_passedThrough_serviceNotConsumed() {
        // A stale rpc.service next to an RC rpc.method must not be prefixed twice.
        Set<String> consumed = new HashSet<>();
        String rpc = OtlpRpcMethodResolver.resolve(
                attrs("rpc.service", "oteldemo.CheckoutService", "rpc.method", "oteldemo.CheckoutService/PlaceOrder"), consumed);
        assertThat(rpc).isEqualTo("oteldemo.CheckoutService/PlaceOrder");
        assertThat(consumed).containsExactly("rpc.method");
    }

    @Test
    void resolve_leadingSlash_keptAsIs() {
        Set<String> consumed = new HashSet<>();
        assertThat(OtlpRpcMethodResolver.resolve(attrs("rpc.method", "/flagd.evaluation.v1.Service/EventStream"), consumed))
                .isEqualTo("/flagd.evaluation.v1.Service/EventStream");
    }

    @Test
    void resolve_methodOnly_staysBare() {
        Set<String> consumed = new HashSet<>();
        assertThat(OtlpRpcMethodResolver.resolve(attrs("rpc.method", "GetAds"), consumed)).isEqualTo("GetAds");
        assertThat(consumed).containsExactly("rpc.method");
    }

    @Test
    void resolve_otherPlaceholder_composedWithService() {
        // RC "_OTHER" (unrecognized method) is treated like any method name.
        Set<String> consumed = new HashSet<>();
        assertThat(OtlpRpcMethodResolver.resolve(attrs("rpc.service", "svc.Api", "rpc.method", "_OTHER"), consumed))
                .isEqualTo("svc.Api/_OTHER");
    }

    @Test
    void resolve_blankOrMissingMethod_isNull_consumesNothing() {
        Set<String> consumed = new HashSet<>();
        assertThat(OtlpRpcMethodResolver.resolve(attrs("rpc.service", "svc.Api", "rpc.method", " "), consumed)).isNull();
        assertThat(OtlpRpcMethodResolver.resolve(attrs("rpc.service", "svc.Api"), consumed)).isNull();
        assertThat(consumed).isEmpty();
    }

    @Test
    void resolve_blankService_ignored() {
        Set<String> consumed = new HashSet<>();
        assertThat(OtlpRpcMethodResolver.resolve(attrs("rpc.service", "  ", "rpc.method", "GetAds"), consumed)).isEqualTo("GetAds");
        assertThat(consumed).containsExactly("rpc.method");
    }

    @Test
    void resolve_trimsWhitespace() {
        Set<String> consumed = new HashSet<>();
        assertThat(OtlpRpcMethodResolver.resolve(attrs("rpc.service", " svc.Api ", "rpc.method", " Get "), consumed))
                .isEqualTo("svc.Api/Get");
    }

    @Test
    void resolveService_legacyKey_consumed() {
        Set<String> consumed = new HashSet<>();
        assertThat(OtlpRpcMethodResolver.resolveService(attrs("rpc.service", "oteldemo.CartService", "rpc.method", "AddItem"), consumed))
                .isEqualTo("oteldemo.CartService");
        assertThat(consumed).containsExactly("rpc.service");
    }

    @Test
    void resolveService_derivedFromFullyQualifiedMethod_methodNotConsumed() {
        Set<String> consumed = new HashSet<>();
        assertThat(OtlpRpcMethodResolver.resolveService(attrs("rpc.method", "oteldemo.CartService/AddItem"), consumed))
                .isEqualTo("oteldemo.CartService");
        assertThat(consumed).isEmpty();
    }

    @Test
    void resolveService_leadingSlashIgnored() {
        assertThat(OtlpRpcMethodResolver.resolveService(attrs("rpc.method", "/flagd.evaluation.v1.Service/EventStream"), new HashSet<>()))
                .isEqualTo("flagd.evaluation.v1.Service");
    }

    @Test
    void resolveService_nestedPath_usesLastSeparator() {
        assertThat(OtlpRpcMethodResolver.resolveService(attrs("rpc.method", "a/b/c"), new HashSet<>())).isEqualTo("a/b");
    }

    @Test
    void resolveService_bareMethodOrSlashOnly_isNull() {
        Set<String> consumed = new HashSet<>();
        assertThat(OtlpRpcMethodResolver.resolveService(attrs("rpc.method", "AddItem"), consumed)).isNull();
        assertThat(OtlpRpcMethodResolver.resolveService(attrs("rpc.method", "/AddItem"), consumed)).isNull();
        assertThat(OtlpRpcMethodResolver.resolveService(attrs("rpc.method", "/"), consumed)).isNull();
        assertThat(OtlpRpcMethodResolver.resolveService(attrs(), consumed)).isNull();
        assertThat(consumed).isEmpty();
    }
}
