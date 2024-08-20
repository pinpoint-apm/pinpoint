/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.collector.monitor.config;

import com.navercorp.pinpoint.collector.monitor.dao.hbase.BulkOperationReporter;
import com.navercorp.pinpoint.collector.monitor.MonitoredThreadPoolExecutorFactoryProvider;
import com.navercorp.pinpoint.collector.monitor.micrometer.BulkOperationMetrics;
import com.navercorp.pinpoint.collector.monitor.micrometer.HBaseAsyncOperationMetrics;
import com.navercorp.pinpoint.collector.monitor.micrometer.MicrometerThreadPoolExecutorFactoryProvider;
import com.navercorp.pinpoint.common.hbase.counter.HBaseBatchPerformance;
import io.grpc.ServerInterceptor;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.grpc.MetricCollectingServerInterceptor;
import io.micrometer.core.instrument.binder.logging.Log4j2Metrics;
import io.micrometer.core.instrument.binder.netty4.NettyAllocatorMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufAllocatorMetricProvider;
import io.netty.buffer.PooledByteBufAllocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author intr3p1d
 */
@Configuration
public class MicrometerConfiguration {
    private final Logger logger = LogManager.getLogger(MicrometerConfiguration.class);
    private static final String GRPC_INTERCEPTOR_TAG_KEY = "service";


    public MicrometerConfiguration() {
        logger.info("Install {}", MicrometerConfiguration.class.getSimpleName());
    }

    @Bean
    @ConditionalOnMissingBean(MeterRegistry.class)
    public MeterRegistry simpleMeterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Bean
    public MonitoredThreadPoolExecutorFactoryProvider micrometerMonitoredThreadPoolExecutorFactoryProvider(
            MeterRegistry meterRegistry
    ) {
        try (Log4j2Metrics is = new Log4j2Metrics()) {
            is.bindTo(meterRegistry);
        }
        return new MicrometerThreadPoolExecutorFactoryProvider(meterRegistry);
    }

    @Bean
    @Qualifier("agentInterceptor")
    public ServerInterceptor metricCollectingAgentServerInterceptor(
            MeterRegistry meterRegistry
    ) {
        return new MetricCollectingServerInterceptor(meterRegistry,
                (Counter.Builder builder) -> addCounterTag(builder, "agent"),
                (Timer.Builder builder) -> addTimerTag(builder, "agent")
        );
    }

    @Bean
    @Qualifier("spanInterceptor")
    public ServerInterceptor metricCollectingSpanServerInterceptor(
            MeterRegistry meterRegistry
    ) {
        return new MetricCollectingServerInterceptor(meterRegistry,
                (Counter.Builder builder) -> addCounterTag(builder, "span"),
                (Timer.Builder builder) -> addTimerTag(builder, "span")
        );
    }

    @Bean
    @Qualifier("statInterceptor")
    public ServerInterceptor metricCollectingStatServerInterceptor(
            MeterRegistry meterRegistry
    ) {
        return new MetricCollectingServerInterceptor(meterRegistry,
                (Counter.Builder builder) -> addCounterTag(builder, "stat"),
                (Timer.Builder builder) -> addTimerTag(builder, "stat")
        );
    }

    private Counter.Builder addCounterTag(Counter.Builder builder, String tagValue) {
        builder.tag(MicrometerConfiguration.GRPC_INTERCEPTOR_TAG_KEY, tagValue);
        return builder;
    }

    private Timer.Builder addTimerTag(Timer.Builder builder, String tagValue) {
        builder.tag(MicrometerConfiguration.GRPC_INTERCEPTOR_TAG_KEY, tagValue);
        return builder;
    }

    @Bean
    @Qualifier("monitoredByteBufAllocator")
    public ByteBufAllocator monitoredByteBufAllocator(MeterRegistry meterRegistry) {
        ByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
        if (allocator != null) {
            new NettyAllocatorMetrics((ByteBufAllocatorMetricProvider) allocator).bindTo(meterRegistry);
        }
        return allocator;
    }

    @Bean
    public BulkOperationMetrics cachedStatisticsDaoMetrics(
            List<BulkOperationReporter> bulkOperationReporters,
            MeterRegistry meterRegistry
    ) {
        return new BulkOperationMetrics(bulkOperationReporters, meterRegistry);
    }

    @Bean
    public HBaseAsyncOperationMetrics asyncOperationMetrics(
            List<HBaseBatchPerformance> hBaseAsyncOperationList,
            MeterRegistry meterRegistry
    ) {
        return new HBaseAsyncOperationMetrics(hBaseAsyncOperationList, meterRegistry);
    }

}
