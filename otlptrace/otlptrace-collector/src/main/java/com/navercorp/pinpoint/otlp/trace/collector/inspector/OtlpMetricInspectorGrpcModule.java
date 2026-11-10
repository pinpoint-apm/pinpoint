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

import com.navercorp.pinpoint.collector.monitor.MonitoringExecutors;
import com.navercorp.pinpoint.common.server.thread.MonitoringExecutorProperties;
import com.navercorp.pinpoint.common.server.util.CallerUtils;
import com.navercorp.pinpoint.otlp.trace.collector.inspector.service.GrpcOtlpMetricService;
import com.navercorp.pinpoint.otlp.trace.collector.inspector.service.OtlpMetricExportService;
import com.navercorp.pinpoint.otlp.trace.collector.inspector.service.OtlpMetricIngestMetrics;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.grpc.MetricCollectingServerInterceptor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * gRPC side of the metrics receiver. No server of its own: the {@link ServerServiceDefinition} bean
 * is picked up by the trace module's {@code serviceList} and served on the same plaintext / TLS
 * servers, so the transport-level limits are the trace server's. The worker pool and the in-flight
 * byte budget are the metrics receiver's own, so a metrics flood back-pressures metrics exporters
 * only.
 */
@Configuration
public class OtlpMetricInspectorGrpcModule {

    public static final String SERVICE_TAG = "otlpmetric";

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.otlp.metric.worker.executor")
    public MonitoringExecutorProperties grpcOtlpMetricWorkerExecutorProperties() {
        return new MonitoringExecutorProperties();
    }

    /**
     * Runs the map/store work off the gRPC handler thread. A task is one export of one JVM (tens to
     * a few hundred points), so the pool stays small and the queue absorbs the per-minute burst.
     */
    @Bean
    public FactoryBean<ExecutorService> grpcOtlpMetricWorkerExecutor(@Qualifier("otlpMonitoringExecutors") MonitoringExecutors executors) {
        String beanName = CallerUtils.getCallerMethodName();
        MonitoringExecutorProperties properties = grpcOtlpMetricWorkerExecutorProperties();
        return executors.newExecutorFactoryBean(properties, beanName);
    }

    @Bean
    public ServerServiceDefinition otlpMetricServerServiceDefinition(OtlpMetricExportService exportService,
                                                                     @Qualifier("grpcOtlpMetricWorkerExecutor") Executor workerExecutor,
                                                                     @Value("${pinpoint.collector.otlptrace.inspector.admission.max-in-flight-bytes:33554432}") int maxInFlightBytes,
                                                                     OtlpMetricIngestMetrics ingestMetrics,
                                                                     MeterRegistry meterRegistry) {
        BindableService metricService = new GrpcOtlpMetricService(exportService, workerExecutor, maxInFlightBytes, ingestMetrics);
        // grpc.server.* metrics for MetricsService/Export, tagged service=otlpmetric so they separate
        // from the trace and log services' series on the same server.
        final ServerInterceptor metricInterceptor = new MetricCollectingServerInterceptor(meterRegistry,
                (Counter.Builder builder) -> builder.tag("service", SERVICE_TAG),
                (Timer.Builder builder) -> builder.tag("service", SERVICE_TAG));
        return ServerInterceptors.intercept(metricService, metricInterceptor);
    }
}
