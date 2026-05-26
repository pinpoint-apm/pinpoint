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

import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.samplers.Sampler;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wires {@link PinpointTraceStateSampler} into the OTel SDK at startup via the
 * autoconfigure SPI. Loaded by the OTel Java agent when this extension jar is
 * placed on the agent's extension path (e.g.
 * {@code -Dotel.javaagent.extensions=pinpoint-otlptrace-otel-extension.jar}).
 *
 * <h3>Recommended configuration — single source via {@code OTEL_RESOURCE_ATTRIBUTES}</h3>
 *
 * <p>Set the Pinpoint identifiers <em>once</em> on the standard OTel resource
 * attributes string. The collector reads the exact same keys off the incoming OTLP
 * Resource block in {@code OtlpTraceMapperUtils.getId()}, so a single entry drives
 * both the outgoing tracestate <em>and</em> the upstream service's own Resource:</p>
 *
 * <pre>{@code
 * -Dotel.resource.attributes=\
 *     pinpoint.applicationName=order-api,\
 *     pinpoint.serviceName=order-team,\
 *     pinpoint.applicationType=1010
 * }</pre>
 *
 * <p>Pure OTel semantic-convention deployments also work — if you only set the
 * standard {@code service.name} / {@code service.namespace} attributes, both the
 * extension and the collector pick those up via the same fallback chain.</p>
 *
 * <p>Supported keys (primary &rarr; fallback, mirroring
 * {@code OtlpTraceMapperUtils.getApplicationName()} / {@code getServiceName()}):</p>
 * <ul>
 *   <li>{@code pinpoint.applicationName} &rarr; {@code otel.service.name} (config
 *       key) &rarr; {@code service.name} (resource attr).</li>
 *   <li>{@code pinpoint.serviceName} &rarr; {@code service.namespace}
 *       (resource attr).</li>
 *   <li>{@code pinpoint.applicationType} — optional; numeric Pinpoint ServiceType
 *       code. Sender-only (no collector counterpart). When absent, the collector
 *       defaults the parent's service type to {@code OPENTELEMETRY_SERVER}.</li>
 * </ul>
 *
 * <h3>⚠ Warning — {@code -Dpinpoint.X} overrides only affect outgoing tracestate</h3>
 *
 * <p>For each Pinpoint key the lookup order is:</p>
 * <ol>
 *   <li>Dedicated config property ({@code -Dpinpoint.applicationName=...})</li>
 *   <li>{@code OTEL_RESOURCE_ATTRIBUTES} entry with the same Pinpoint key
 *       (recommended)</li>
 *   <li>OTel semconv fallback (config + resource attr) — see the table above</li>
 * </ol>
 *
 * <p>A {@code -Dpinpoint.applicationName} override <strong>only</strong> affects what
 * this extension writes into the outgoing {@code tracestate} header. It does
 * <strong>not</strong> change the Resource attributes the OTel SDK builds for this
 * process's own spans — those still come exclusively from
 * {@code OTEL_RESOURCE_ATTRIBUTES}. Using the override therefore causes a
 * <strong>divergence</strong>:</p>
 * <ul>
 *   <li>This service's own spans arrive at the Pinpoint collector under the
 *       Resource's {@code pinpoint.applicationName} (e.g. {@code order-api}).</li>
 *   <li>Downstream services see {@code app:<override>} on their incoming tracestate
 *       and record the parent as e.g. {@code order-api-canary}.</li>
 * </ul>
 * <p>Result: the same physical process renders as two separate ApplicationMap nodes,
 * and the call edge from this service is attributed to the override name. Prefer
 * editing {@code OTEL_RESOURCE_ATTRIBUTES} itself for any production-visible change;
 * reserve {@code -Dpinpoint.X} for tests and one-off diagnostics.</p>
 *
 * <h3>Disabled mode</h3>
 *
 * <p>If none of the keys for {@code applicationName} or {@code serviceName} resolves,
 * the sampler is left untouched — the extension becomes a no-op rather than failing
 * the agent startup.</p>
 */
public final class PinpointAutoConfig implements AutoConfigurationCustomizerProvider {

    private static final Logger logger = Logger.getLogger(PinpointAutoConfig.class.getName());

    // Pinpoint-specific keys. CamelCase mirrors
    // com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperUtils
    // .KEY_PINPOINT_SERVICE_NAME / KEY_APPLICATION_NAME so a single
    // OTEL_RESOURCE_ATTRIBUTES setting drives both ends.
    static final String CONFIG_PP_SERVICE_NAME = "pinpoint.serviceName";
    static final String CONFIG_PP_APPLICATION_NAME = "pinpoint.applicationName";
    static final String CONFIG_PP_APPLICATION_TYPE = "pinpoint.applicationType";

    // OTel semantic-convention fallbacks. Mirrors the collector's fallback chain in
    // OtlpTraceMapperUtils.getApplicationName()/getServiceName() so a deployment that
    // only sets the standard OTel keys still produces consistent identifiers.
    static final String CONFIG_OTEL_SERVICE_NAME = "otel.service.name";   // SDK config key
    static final String CONFIG_SERVICE_NAME = "service.name";             // resource attr
    static final String CONFIG_SERVICE_NAMESPACE = "service.namespace";   // resource attr

    static final String CONFIG_OTEL_RESOURCE_ATTRIBUTES = "otel.resource.attributes";

    @Override
    public void customize(AutoConfigurationCustomizer customizer) {
        customizer.addSamplerCustomizer(PinpointAutoConfig::customizeSampler);
    }

    /**
     * Package-private entry point used by both the SPI lambda and unit tests so the
     * resolution rules can be exercised without standing up the full SDK builder.
     */
    static Sampler customizeSampler(Sampler base, ConfigProperties config) {
        final Map<String, String> resAttrs = config.getMap(CONFIG_OTEL_RESOURCE_ATTRIBUTES);

        final String svc = resolveWithFallback(config, resAttrs,
                CONFIG_PP_SERVICE_NAME,
                CONFIG_SERVICE_NAMESPACE);
        final String app = resolveWithFallback(config, resAttrs,
                CONFIG_PP_APPLICATION_NAME,
                CONFIG_OTEL_SERVICE_NAME,
                CONFIG_SERVICE_NAME);
        final Integer type = resolveInt(config, resAttrs, CONFIG_PP_APPLICATION_TYPE);

        if (isBlank(svc) && isBlank(app)) {
            logger.log(Level.INFO, "Pinpoint tracestate injection disabled: "
                            + "none of {0}/{1}/{2}/{3} is set",
                    new Object[]{CONFIG_PP_APPLICATION_NAME, CONFIG_PP_SERVICE_NAME,
                            CONFIG_OTEL_SERVICE_NAME, CONFIG_SERVICE_NAMESPACE});
            return base;
        }

        logger.log(Level.INFO, "Pinpoint tracestate injection enabled: svc={0}, app={1}, type={2}",
                new Object[]{svc, app, type});
        return new PinpointTraceStateSampler(base, svc, app, type);
    }

    /**
     * Walk the priority list: for each key, try the dedicated config property first
     * (e.g. {@code -Dservice.namespace=...}) and then the {@code OTEL_RESOURCE_ATTRIBUTES}
     * entry with the same name. Move to the next key only when neither yields a
     * non-empty value.
     */
    private static String resolveWithFallback(ConfigProperties config,
                                              Map<String, String> resAttrs,
                                              String primaryKey,
                                              String... fallbackKeys) {
        String value = lookup(config, resAttrs, primaryKey);
        if (!isBlank(value)) {
            return value;
        }
        for (String key : fallbackKeys) {
            value = lookup(config, resAttrs, key);
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private static String lookup(ConfigProperties config,
                                 Map<String, String> resAttrs,
                                 String key) {
        final String fromConfig = config.getString(key);
        if (!isBlank(fromConfig)) {
            return fromConfig;
        }
        if (resAttrs != null) {
            final String fromResource = resAttrs.get(key);
            if (!isBlank(fromResource)) {
                return fromResource;
            }
        }
        return null;
    }

    private static Integer resolveInt(ConfigProperties config,
                                      Map<String, String> resAttrs,
                                      String key) {
        final Integer fromConfig = config.getInt(key);
        if (fromConfig != null) {
            return fromConfig;
        }
        if (resAttrs != null) {
            final String raw = resAttrs.get(key);
            if (!isBlank(raw)) {
                try {
                    return Integer.valueOf(raw.trim());
                } catch (NumberFormatException e) {
                    logger.log(Level.WARNING, "Ignoring non-numeric {0}={1}",
                            new Object[]{key, raw});
                }
            }
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isEmpty();
    }
}
