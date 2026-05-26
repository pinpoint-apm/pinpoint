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

package com.navercorp.pinpoint.otlp.otel.extension;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PinpointAutoConfigTest {

    private static final String TRACE_ID = "0102030405060708090a0b0c0d0e0f10";

    @Test
    void dedicatedConfigProperties_areHonored() {
        Sampler result = customize(map(
                "pinpoint.serviceName", "my-svc",
                "pinpoint.applicationName", "my-app",
                "pinpoint.applicationType", "1010"));

        assertThat(extractPpValue(result)).isEqualTo("svc:my-svc;app:my-app;type:1010");
    }

    @Test
    void resourceAttributes_singleSource_drivesBothEnds() {
        // The recommended deployment: one OTEL_RESOURCE_ATTRIBUTES setting that the
        // collector parses from incoming spans AND this extension reads here.
        Sampler result = customize(map(
                "otel.resource.attributes",
                "pinpoint.serviceName=my-svc,pinpoint.applicationName=my-app,pinpoint.applicationType=1010"));

        assertThat(extractPpValue(result)).isEqualTo("svc:my-svc;app:my-app;type:1010");
    }

    @Test
    void dedicatedConfig_overridesResourceAttributes() {
        Sampler result = customize(map(
                "otel.resource.attributes",
                "pinpoint.serviceName=from-res,pinpoint.applicationName=from-res",
                "pinpoint.applicationName", "override-app"));

        assertThat(extractPpValue(result)).isEqualTo("svc:from-res;app:override-app");
    }

    @Test
    void otelServiceName_isFallbackForApplicationName() {
        // Mirrors OtlpTraceMapperUtils.getApplicationName(): pinpoint.applicationName
        // → service.name. We additionally bridge "otel.service.name" config key.
        Sampler result = customize(map(
                "pinpoint.serviceName", "my-svc",
                "otel.service.name", "fallback-app"));

        assertThat(extractPpValue(result)).isEqualTo("svc:my-svc;app:fallback-app");
    }

    @Test
    void resourceAttribute_appOnly_stillWorks() {
        Sampler result = customize(map(
                "otel.resource.attributes", "pinpoint.applicationName=my-app"));

        assertThat(extractPpValue(result)).isEqualTo("app:my-app");
    }

    @Test
    void resourceAttribute_typeNonNumeric_logsAndSkipsType() {
        Sampler result = customize(map(
                "otel.resource.attributes",
                "pinpoint.serviceName=my-svc,pinpoint.applicationName=my-app,pinpoint.applicationType=tomcat"));

        // svc/app still picked up; type is silently dropped to avoid corrupting tracestate.
        assertThat(extractPpValue(result)).isEqualTo("svc:my-svc;app:my-app");
    }

    @Test
    void nothingConfigured_returnsPassThroughSampler() {
        Sampler baseline = Sampler.alwaysOn();
        Sampler result = PinpointAutoConfig.customizeSampler(baseline,
                new StubConfigProperties(Collections.emptyMap()));

        // No wrapping → same instance returned.
        assertThat(result).isSameAs(baseline);
    }

    @Test
    void pureOtelSemconv_resourceAttributesOnly_works() {
        // No pinpoint.* keys at all — only OTel standard semconv. Mirrors the
        // collector's getApplicationName/getServiceName fallback chain.
        Sampler result = customize(map(
                "otel.resource.attributes",
                "service.name=order-api,service.namespace=order-team"));

        assertThat(extractPpValue(result)).isEqualTo("svc:order-team;app:order-api");
    }

    @Test
    void serviceNamespace_isFallbackForServiceName() {
        Sampler result = customize(map(
                "pinpoint.applicationName", "my-app",
                "otel.resource.attributes", "service.namespace=order-team"));

        assertThat(extractPpValue(result)).isEqualTo("svc:order-team;app:my-app");
    }

    @Test
    void pinpointServiceName_winsOverServiceNamespace() {
        Sampler result = customize(map(
                "pinpoint.serviceName", "explicit-svc",
                "pinpoint.applicationName", "my-app",
                "otel.resource.attributes", "service.namespace=fallback-svc"));

        assertThat(extractPpValue(result)).isEqualTo("svc:explicit-svc;app:my-app");
    }

    @Test
    void serviceNameResourceAttr_isFallbackForApplicationName_whenNoOtelServiceName() {
        // OTEL_RESOURCE_ATTRIBUTES=service.name=... only — no -Dotel.service.name.
        // This path is what 'otel.service.name' config-key lookup misses.
        Sampler result = customize(map(
                "otel.resource.attributes", "service.name=order-api"));

        assertThat(extractPpValue(result)).isEqualTo("app:order-api");
    }

    @Test
    void otelServiceName_takesPrecedenceOverServiceNameResourceAttr() {
        // Pinpoint key absent → next is otel.service.name (config) before
        // service.name (resource attr).
        Sampler result = customize(map(
                "otel.service.name", "from-config",
                "otel.resource.attributes", "service.name=from-res"));

        assertThat(extractPpValue(result)).isEqualTo("app:from-config");
    }

    @Test
    void pinpointApplicationName_winsOverAllOtelFallbacks() {
        Sampler result = customize(map(
                "pinpoint.applicationName", "explicit-app",
                "otel.service.name", "from-config",
                "otel.resource.attributes", "service.name=from-res,service.namespace=svc"));

        assertThat(extractPpValue(result)).isEqualTo("svc:svc;app:explicit-app");
    }

    @Test
    void realWorldJvmOptions_serviceNameAndAgentId() {
        // Real-world JVM options observed in operation:
        //   -Dotel.resource.attributes=service.name=p.otlp3.app,pinpoint.agentId=p.otlp.3
        //
        // pinpoint.agentId is consumed by the collector (OtlpTraceMapperUtils.getAgentAuth)
        // to identify the producer; it is NOT used in tracestate propagation, so the
        // extension ignores it. With only service.name set on the OTel side, the OTel
        // semconv fallback chain promotes it to app, leaving svc/type unset.
        Sampler result = customize(map(
                "otel.resource.attributes",
                "service.name=p.otlp3.app,pinpoint.agentId=p.otlp.3"));

        assertThat(extractPpValue(result)).isEqualTo("app:p.otlp3.app");
    }

    @Test
    void emptyStrings_treatedAsMissing() {
        // OTel SDK passes empty system properties as empty strings; we must treat them
        // the same as missing so the resource-attribute fallback still kicks in.
        Sampler result = customize(map(
                "pinpoint.serviceName", "",
                "otel.resource.attributes", "pinpoint.serviceName=from-res,pinpoint.applicationName=from-res"));

        assertThat(extractPpValue(result)).isEqualTo("svc:from-res;app:from-res");
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private static Sampler customize(Map<String, String> properties) {
        return PinpointAutoConfig.customizeSampler(
                Sampler.alwaysOn(), new StubConfigProperties(properties));
    }

    private static String extractPpValue(Sampler sampler) {
        SamplingResult sr = sampler.shouldSample(
                Context.root(), TRACE_ID, "span",
                SpanKind.SERVER, Attributes.empty(), Collections.emptyList());
        return sr.getUpdatedTraceState(TraceState.getDefault()).get("pp");
    }

    private static Map<String, String> map(String... keyValues) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            m.put(keyValues[i], keyValues[i + 1]);
        }
        return m;
    }

    /**
     * Minimal ConfigProperties stub covering the methods PinpointAutoConfig actually
     * touches. Mirrors OTel DefaultConfigProperties semantics for those keys.
     */
    private static final class StubConfigProperties implements ConfigProperties {
        private final Map<String, String> values;

        StubConfigProperties(Map<String, String> values) {
            this.values = values;
        }

        @Override public String getString(String name) {
            String v = values.get(name);
            return (v == null || v.isEmpty()) ? null : v;
        }

        @Override public Boolean getBoolean(String name) {
            String v = getString(name);
            return v == null ? null : Boolean.valueOf(v);
        }

        @Override public Integer getInt(String name) {
            String v = getString(name);
            if (v == null) return null;
            try { return Integer.valueOf(v); } catch (NumberFormatException e) { return null; }
        }

        @Override public Long getLong(String name) {
            String v = getString(name);
            if (v == null) return null;
            try { return Long.valueOf(v); } catch (NumberFormatException e) { return null; }
        }

        @Override public Double getDouble(String name) {
            String v = getString(name);
            if (v == null) return null;
            try { return Double.valueOf(v); } catch (NumberFormatException e) { return null; }
        }

        @Override public Duration getDuration(String name) {
            return null;
        }

        @Override public List<String> getList(String name) {
            String v = getString(name);
            if (v == null) return Collections.emptyList();
            return Arrays.asList(v.split(","));
        }

        @Override public Map<String, String> getMap(String name) {
            String v = getString(name);
            if (v == null) return Collections.emptyMap();
            Map<String, String> out = new HashMap<>();
            for (String entry : v.split(",")) {
                int eq = entry.indexOf('=');
                if (eq > 0) {
                    out.put(entry.substring(0, eq).trim(), entry.substring(eq + 1).trim());
                }
            }
            return out;
        }
    }
}
