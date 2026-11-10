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

package com.navercorp.pinpoint.otlp.trace.collector.inspector;

import com.navercorp.pinpoint.inspector.collector.InspectorCollectorConfig;
import com.navercorp.pinpoint.otlp.trace.inspector.MetricDialect;
import com.navercorp.pinpoint.otlp.trace.inspector.MetricVocabulary;
import com.navercorp.pinpoint.otlp.trace.inspector.DotnetDialect;
import com.navercorp.pinpoint.otlp.trace.inspector.GoDialect;
import com.navercorp.pinpoint.otlp.trace.inspector.MicrometerDialect;
import com.navercorp.pinpoint.otlp.trace.inspector.NodeDialect;
import com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect;
import com.navercorp.pinpoint.otlp.trace.inspector.PythonDialect;
import com.navercorp.pinpoint.otlp.trace.inspector.OtlpMetricInspectorMapper;
import com.navercorp.pinpoint.otlp.trace.inspector.SeriesStateCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * OTLP metrics receiver co-hosted in the OTLPTRACE collector: {@code MetricsService/Export} on the
 * trace collector's gRPC servers and {@code POST /v1/metrics} on its servlet port. The JVM runtime
 * metrics of a Resource are mapped to the native agent's {@code AgentStatBo}
 * ({@code pinpoint-otlptrace-inspector}) and stored through the Inspector storage module
 * ({@link InspectorCollectorConfig}: Kafka -> Pinot), so OTel applications get the Inspector charts.
 *
 * <p>Same process as traces for the same reasons as the logs receiver: OTLP exporters send every
 * signal to one endpoint, and the Resource resolves to the same (serviceName, applicationName,
 * agentId) as its spans — the agent the Inspector lists — through the trace path's
 * {@code OtlpResourceIdResolver}. A Resource that only sends metrics is registered in AgentInfo
 * here, as the first span would have done.
 *
 * <p>Gated by two keys: {@value #ENABLED_KEY} (this feature, default off, otlptrace properties) and
 * {@value #INSPECTOR_MODULE_ENABLED_KEY} (the storage module, collector-starter application.yml).
 * Without the storage module there is no {@code AgentStatService} to write to, so the receiver is
 * not registered rather than failing the context; {@link OtlpMetricInspectorGateReporter} logs the
 * mismatch at startup.
 */
@Configuration
@Import({
        OtlpMetricInspectorGrpcModule.class,
        OtlpMetricInspectorHttpModule.class,
        InspectorCollectorConfig.class
})
@ComponentScan({
        "com.navercorp.pinpoint.otlp.trace.collector.inspector.service",
        "com.navercorp.pinpoint.otlp.trace.collector.inspector.controller"
})
@ConditionalOnProperty(name = {OtlpMetricInspectorModule.INSPECTOR_MODULE_ENABLED_KEY, OtlpMetricInspectorModule.ENABLED_KEY}, havingValue = "true")
public class OtlpMetricInspectorModule {

    public static final String ENABLED_KEY = "pinpoint.collector.otlptrace.inspector.enabled";
    /** The storage module's gate; the receiver has nothing to write to without it. */
    public static final String INSPECTOR_MODULE_ENABLED_KEY = "pinpoint.modules.collector.inspector.enabled";

    /**
     * Per-series state for cumulative-to-delta ({@code responseTime}) and delta-to-cumulative (GC
     * counters) conversion. Bounded; an agent that stops exporting is forgotten after the idle time.
     */
    @Bean
    public SeriesStateCache otlpMetricSeriesStateCache(
            @Value("${pinpoint.collector.otlptrace.inspector.delta-cache.max-size:100000}") long maxSize,
            @Value("${pinpoint.collector.otlptrace.inspector.delta-cache.expire-after-access:10m}") Duration expireAfterAccess) {
        return new SeriesStateCache(maxSize, expireAfterAccess);
    }

    /**
     * The metric naming dialects understood, in precedence order: {@code otel} (semantic conventions,
     * the OTel Java agent), {@code micrometer} (Spring Boot Actuator's OTLP registry), {@code go} (the
     * OTel Go runtime instrumentation), {@code nodejs} (the OTel JS runtime-node instrumentation),
     * {@code python} (the OTel Python system-metrics instrumentation) and {@code dotnet} (the .NET runtime
     * metrics).
     * Resolution is per metric name, so one Resource may mix them.
     */
    @Bean
    public MetricVocabulary otlpMetricVocabulary(
            @Value("${pinpoint.collector.otlptrace.inspector.dialects:otel,micrometer,go,nodejs,python,dotnet}") List<String> dialectNames) {
        final List<MetricDialect> dialects = new ArrayList<>(dialectNames.size());
        for (String dialectName : dialectNames) {
            final String name = dialectName.trim().toLowerCase(Locale.ROOT);
            if (name.isEmpty()) {
                continue;
            }
            switch (name) {
                case OtelSemconvDialect.NAME:
                    dialects.add(new OtelSemconvDialect());
                    break;
                case MicrometerDialect.NAME:
                    dialects.add(new MicrometerDialect());
                    break;
                case GoDialect.NAME:
                    dialects.add(new GoDialect());
                    break;
                case NodeDialect.NAME:
                    dialects.add(new NodeDialect());
                    break;
                case PythonDialect.NAME:
                    dialects.add(new PythonDialect());
                    break;
                case DotnetDialect.NAME:
                    dialects.add(new DotnetDialect());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown pinpoint.collector.otlptrace.inspector.dialects entry '" + dialectName
                            + "' (known: " + OtelSemconvDialect.NAME + ", " + MicrometerDialect.NAME + ", " + GoDialect.NAME + ", " + NodeDialect.NAME + ", " + PythonDialect.NAME + ", " + DotnetDialect.NAME + ")");
            }
        }
        return new MetricVocabulary(dialects);
    }

    @Bean
    public OtlpMetricInspectorMapper otlpMetricInspectorMapper(MetricVocabulary otlpMetricVocabulary, SeriesStateCache otlpMetricSeriesStateCache) {
        return new OtlpMetricInspectorMapper(otlpMetricVocabulary, otlpMetricSeriesStateCache);
    }
}
