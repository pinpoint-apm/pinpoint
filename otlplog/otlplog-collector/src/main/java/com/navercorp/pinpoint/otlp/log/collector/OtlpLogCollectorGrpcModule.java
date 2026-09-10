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

package com.navercorp.pinpoint.otlp.log.collector;

import com.navercorp.pinpoint.collector.monitor.MonitoringExecutors;
import com.navercorp.pinpoint.common.server.thread.MonitoringExecutorProperties;
import com.navercorp.pinpoint.common.server.util.CallerUtils;
import com.navercorp.pinpoint.otlp.log.collector.service.GrpcOtlpLogService;
import com.navercorp.pinpoint.otlp.log.collector.service.OtlpLogExportService;
import com.navercorp.pinpoint.otlp.log.collector.service.OtlpLogIngestMetrics;
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
 * gRPC side of the log receiver. There is no server of its own: the {@link ServerServiceDefinition}
 * bean declared here is picked up by the trace collector's {@code serviceList} and served on the
 * same plaintext / TLS servers, so the transport-level limits (inbound message size, concurrent
 * calls per connection, flow control) are the trace server's. What is separate is the worker pool
 * and the in-flight byte budget, so a log flood applies back-pressure to log exporters only.
 */
@Configuration
public class OtlpLogCollectorGrpcModule {

    public static final String SERVICE_TAG = "otlplog";

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.otlp.log.worker.executor")
    public MonitoringExecutorProperties grpcOtlpLogWorkerExecutorProperties() {
        return new MonitoringExecutorProperties();
    }

    /**
     * Runs the filter/map/store work off the gRPC handler thread. Sized small (see the profile
     * properties): ~99% of records are dropped at the attribute key scan, so a task is cheap and the
     * queue, not the pool, absorbs bursts.
     */
    @Bean
    public FactoryBean<ExecutorService> grpcOtlpLogWorkerExecutor(@Qualifier("otlpMonitoringExecutors") MonitoringExecutors executors) {
        String beanName = CallerUtils.getCallerMethodName();
        MonitoringExecutorProperties properties = grpcOtlpLogWorkerExecutorProperties();
        return executors.newExecutorFactoryBean(properties, beanName);
    }

    @Bean
    public ServerServiceDefinition otlpLogServerServiceDefinition(OtlpLogExportService exportService,
                                                                  @Qualifier("grpcOtlpLogWorkerExecutor") Executor workerExecutor,
                                                                  @Value("${pinpoint.collector.otlplog.admission.max-in-flight-bytes:67108864}") int maxInFlightBytes,
                                                                  OtlpLogIngestMetrics ingestMetrics,
                                                                  MeterRegistry meterRegistry) {
        BindableService logService = new GrpcOtlpLogService(exportService, workerExecutor, maxInFlightBytes, ingestMetrics);
        // grpc.server.* metrics for LogsService/Export, tagged service=otlplog so they separate from
        // the trace service's series on the same server.
        final ServerInterceptor metricInterceptor = new MetricCollectingServerInterceptor(meterRegistry,
                (Counter.Builder builder) -> builder.tag("service", SERVICE_TAG),
                (Timer.Builder builder) -> builder.tag("service", SERVICE_TAG));
        return ServerInterceptors.intercept(logService, metricInterceptor);
    }
}
