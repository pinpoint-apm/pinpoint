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

import com.navercorp.pinpoint.collector.uid.service.ServiceLookupService;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUidSupplier;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.io.request.UidException;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The resolver is the single place where an OTLP Resource becomes a Pinpoint identity, shared by
 * the trace and log receivers. It mirrors the native V4 header path: an explicit
 * {@code pinpoint.serviceName} is looked up, DEFAULT is short-circuited, and an unregistered
 * name is a {@link OtlpServiceNotFoundException} rather than a silent DEFAULT or UNKNOWN uid.
 */
class OtlpResourceIdResolverTest {

    private static final ServiceUid ORDER_TEAM = ServiceUid.of(100001);

    private static final ServiceLookupService REGISTRY = serviceName ->
            CompletableFuture.completedFuture("order-team".equals(serviceName) ? ORDER_TEAM : null);

    private static Map<String, AttributeValue> resource(String serviceName) {
        if (serviceName == null) {
            return Map.of(
                    "pinpoint.agentId", AttributeValue.of("agent-1"),
                    "pinpoint.applicationName", AttributeValue.of("order-api"));
        }
        return Map.of(
                "pinpoint.agentId", AttributeValue.of("agent-1"),
                "pinpoint.applicationName", AttributeValue.of("order-api"),
                "pinpoint.serviceName", AttributeValue.of(serviceName));
    }

    @Test
    void explicitRegisteredServiceName_resolvesNameAndUid() {
        OtlpResourceIdResolver resolver = new OtlpResourceIdResolver(REGISTRY, false);

        IdAndName id = resolver.resolve(resource("order-team"));

        assertThat(id.serviceName()).isEqualTo("order-team");
        assertThat(id.serviceUid().get()).isEqualTo(ORDER_TEAM);
        // the names are untouched by the uid step
        assertThat(id.applicationName()).isEqualTo("order-api");
        assertThat(id.agentId()).isEqualTo("agent-1");
    }

    @Test
    void explicitUnregisteredServiceName_throwsServiceNotFound() {
        OtlpResourceIdResolver resolver = new OtlpResourceIdResolver(REGISTRY, false);

        assertThatThrownBy(() -> resolver.resolve(resource("unknown-team")))
                .isInstanceOf(OtlpServiceNotFoundException.class)
                .hasMessageContaining("serviceName=unknown-team")
                .extracting(e -> ((OtlpServiceNotFoundException) e).getServiceName())
                .isEqualTo("unknown-team");
    }

    @Test
    void absentServiceName_isDefault_andSkipsTheLookup() {
        AtomicInteger lookups = new AtomicInteger();
        ServiceLookupService counting = serviceName -> {
            lookups.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        };
        OtlpResourceIdResolver resolver = new OtlpResourceIdResolver(counting, false);

        IdAndName id = resolver.resolve(resource(null));

        assertThat(id.serviceName()).isEqualTo(ServiceUid.DEFAULT_SERVICE_UID_NAME);
        assertThat(id.serviceUid()).isSameAs(ServiceUidSupplier.DEFAULT);
        assertThat(lookups).hasValue(0);
    }

    @Test
    void explicitDefaultServiceName_isDefault_andSkipsTheLookup() {
        OtlpResourceIdResolver resolver = new OtlpResourceIdResolver(
                serviceName -> { throw new AssertionError("lookup must not be called for DEFAULT"); }, false);

        IdAndName id = resolver.resolve(resource(ServiceUid.DEFAULT_SERVICE_UID_NAME));

        assertThat(id.serviceUid()).isSameAs(ServiceUidSupplier.DEFAULT);
    }

    @Test
    void lookupDisabled_staticDefault_acceptsAnyServiceName() {
        // StaticServiceLookupService (pinpoint.collector.service.lookup.enabled=false) answers DEFAULT
        // for every name, so an explicit serviceName is kept as a label but lands on the DEFAULT uid.
        OtlpResourceIdResolver resolver = new OtlpResourceIdResolver(
                serviceName -> CompletableFuture.completedFuture(ServiceUid.DEFAULT), false);

        IdAndName id = resolver.resolve(resource("order-team"));

        assertThat(id.serviceName()).isEqualTo("order-team");
        assertThat(id.serviceUid().get()).isEqualTo(ServiceUid.DEFAULT);
    }

    @Test
    void lookupFailure_propagatesAsUidException_notAsServiceNotFound() {
        OtlpResourceIdResolver resolver = new OtlpResourceIdResolver(
                serviceName -> CompletableFuture.failedFuture(new IllegalStateException("registry down")), false);

        assertThatThrownBy(() -> resolver.resolve(resource("order-team")))
                .isInstanceOf(UidException.class)
                .isNotInstanceOf(OtlpServiceNotFoundException.class);
    }

    @Test
    void serviceNamespace_isNotAFallback() {
        Map<String, AttributeValue> attrs = Map.of(
                "pinpoint.agentId", AttributeValue.of("agent-1"),
                "pinpoint.applicationName", AttributeValue.of("order-api"),
                "service.namespace", AttributeValue.of("unknown-team"));
        OtlpResourceIdResolver resolver = new OtlpResourceIdResolver(REGISTRY, false);

        IdAndName id = resolver.resolve(attrs);

        assertThat(id.serviceName()).isEqualTo(ServiceUid.DEFAULT_SERVICE_UID_NAME);
        assertThat(id.serviceUid()).isSameAs(ServiceUidSupplier.DEFAULT);
    }

    @Test
    void invalidServiceName_isAClientFault_beforeAnyLookup() {
        OtlpResourceIdResolver resolver = new OtlpResourceIdResolver(
                serviceName -> { throw new AssertionError("lookup must not be called for an invalid name"); }, false);

        assertThatThrownBy(() -> resolver.resolve(resource("bad team!")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid serviceName=");
    }

    @Test
    void applicationNameFallbackFlag_isPassedThrough() {
        Map<String, AttributeValue> appOnly = Map.of("pinpoint.applicationName", AttributeValue.of("order-api"));

        assertThatThrownBy(() -> new OtlpResourceIdResolver(REGISTRY, false).resolve(appOnly))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no per-instance identifier");
        assertThat(new OtlpResourceIdResolver(REGISTRY, true).resolve(appOnly).agentId()).isEqualTo("order-api");
    }
}
