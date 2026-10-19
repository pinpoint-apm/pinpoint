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

package com.navercorp.pinpoint.otlp.trace.collector.linkindex;

import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkIndexWriter;
import com.navercorp.pinpoint.otlp.trace.linkindex.redis.LinkIndexRedisConnector;
import com.navercorp.pinpoint.otlp.trace.linkindex.redis.LinkIndexRedisConnectorFactory;
import com.navercorp.pinpoint.otlp.trace.linkindex.redis.LinkIndexRedisProperties;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

/**
 * Link index writer of the OTLP trace collector (disabled by default).
 * The store connection comes from a {@link LinkIndexRedisConnectorFactory} bean when one exists,
 * otherwise from {@code pinpoint.collector.otlptrace.link-index.redis.*}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "pinpoint.collector.otlptrace.link-index.enabled", havingValue = "true")
public class OtlpLinkIndexModule {

    private static final Logger logger = LogManager.getLogger(OtlpLinkIndexModule.class);

    static final String PREFIX = "pinpoint.collector.otlptrace.link-index.";
    static final String METRIC_PREFIX = "collector.otlptrace.linkindex.";

    public OtlpLinkIndexModule() {
        logger.info("Install {}", OtlpLinkIndexModule.class.getSimpleName());
    }

    @Bean
    public LinkIndexRedisProperties otlpLinkIndexRedisProperties(
            @Value("${" + PREFIX + "redis.mode:cluster}") String mode,
            @Value("${" + PREFIX + "redis.nodes:}") String nodes,
            @Value("${" + PREFIX + "redis.password:}") String password,
            @Value("${" + PREFIX + "redis.connect-timeout:3s}") Duration connectTimeout,
            @Value("${" + PREFIX + "redis.command-timeout:2s}") Duration commandTimeout) {
        return new LinkIndexRedisProperties(mode, nodes, password, connectTimeout, commandTimeout);
    }

    @Bean(destroyMethod = "close")
    public LinkIndexRedisConnector otlpLinkIndexRedisConnector(LinkIndexRedisProperties properties,
                                                               ObjectProvider<LinkIndexRedisConnectorFactory> factory) {
        final LinkIndexRedisConnectorFactory custom = factory.getIfAvailable();
        if (custom != null) {
            final LinkIndexRedisConnector connector = custom.create(properties);
            logger.info("Link index store from {}: {}", custom.getClass().getSimpleName(), connector);
            return connector;
        }
        if (!properties.hasNodes()) {
            throw new IllegalStateException(PREFIX + "redis.nodes is empty while " + PREFIX + "enabled=true");
        }
        logger.info("Link index store {}", properties);
        return properties.newConnector();
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    public LinkIndexWriter otlpLinkIndexWriter(LinkIndexRedisConnector connector,
                                               @Value("${" + PREFIX + "buckets:65536}") int buckets,
                                               @Value("${" + PREFIX + "ttl:5m}") Duration ttl,
                                               @Value("${" + PREFIX + "queue-size:100000}") int queueSize,
                                               @Value("${" + PREFIX + "max-entries-per-slot:1000000}") int maxEntriesPerSlot,
                                               @Value("${" + PREFIX + "sample-modulo:1}") int sampleModulo,
                                               @Value("${" + PREFIX + "redis.command-timeout:2s}") Duration timeout) {
        logger.info("Link index writer buckets:{} ttl:{} queueSize:{} maxEntriesPerSlot:{} sampleModulo:{}",
                buckets, ttl, queueSize, maxEntriesPerSlot, sampleModulo);
        return new LinkIndexWriter(connector.connect(), buckets, ttl.toMillis(), queueSize,
                maxEntriesPerSlot, sampleModulo, timeout, System::currentTimeMillis);
    }

    @Bean
    public LinkIndexTraceService otlpLinkIndexTraceService(LinkIndexWriter writer, ServiceTypeRegistryService registry) {
        return new LinkIndexTraceService(writer, registry);
    }

    @Bean
    public LinkIndexWriterMetrics otlpLinkIndexWriterMetrics(LinkIndexWriter writer, MeterRegistry meterRegistry) {
        return new LinkIndexWriterMetrics(writer, meterRegistry);
    }

    /**
     * Registers the writer counters under {@value #METRIC_PREFIX}.
     */
    public static class LinkIndexWriterMetrics {
        public LinkIndexWriterMetrics(LinkIndexWriter writer, MeterRegistry registry) {
            counter(registry, "written", writer, LinkIndexWriter::getWritten);
            counter(registry, "expireat.sent", writer, LinkIndexWriter::getExpireAtSent);
            counter(registry, "not.sampled", writer, LinkIndexWriter::getNotSampled);
            counter(registry, "write.errors", writer, LinkIndexWriter::getWriteErrors);
            FunctionCounter.builder(METRIC_PREFIX + "dropped", writer, w -> (double) w.getDroppedQueueFull())
                    .tag("reason", "queue_full").register(registry);
            FunctionCounter.builder(METRIC_PREFIX + "dropped", writer, w -> (double) w.getDroppedSlotLimit())
                    .tag("reason", "slot_limit").register(registry);
            FunctionCounter.builder(METRIC_PREFIX + "dropped", writer, w -> (double) w.getDroppedLate())
                    .tag("reason", "late").register(registry);
            Gauge.builder(METRIC_PREFIX + "queue.depth", writer, LinkIndexWriter::getQueueSize).register(registry);
        }

        private static void counter(MeterRegistry registry, String name, LinkIndexWriter writer,
                                    ToLongFunction<LinkIndexWriter> function) {
            final ToDoubleFunction<LinkIndexWriter> value = w -> (double) function.applyAsLong(w);
            FunctionCounter.builder(METRIC_PREFIX + name, writer, value).register(registry);
        }
    }
}
