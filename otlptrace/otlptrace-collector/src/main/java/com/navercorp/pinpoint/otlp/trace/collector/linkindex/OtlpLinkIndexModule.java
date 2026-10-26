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
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkKind;
import com.navercorp.pinpoint.otlp.trace.linkindex.SlotQuota;
import com.navercorp.pinpoint.otlp.trace.linkindex.suppress.LinkIndexSuppression;
import com.navercorp.pinpoint.otlp.trace.linkindex.suppress.SuppressKey;
import com.navercorp.pinpoint.otlp.trace.linkindex.suppress.SuppressStore;
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
import java.util.Locale;
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
                                               @Value("${" + PREFIX + "sample-modulo:1}") int sampleModulo,
                                               @Value("${" + PREFIX + "redis.command-timeout:2s}") Duration timeout,
                                               LinkIndexSuppression suppression,
                                               SlotQuota quota) {
        logger.info("Link index writer buckets:{} ttl:{} queueSize:{} sampleModulo:{} {}",
                buckets, ttl, queueSize, sampleModulo, quota);
        if (quota.getClientLimit() < suppression.getProbePerSlot()) {
            // the probes of a suppressed identifier go through the quota: fewer than probe-per-slot would be written
            logger.warn("Link index {}max-entries-per-identifier-percent gives {} entries per identifier and slot, "
                            + "less than {}suppress.probe-per-slot={}: the probes of a suppressed identifier are cut by the quota",
                    PREFIX, quota.getClientLimit(), PREFIX, suppression.getProbePerSlot());
        }
        return new LinkIndexWriter(connector.connect(), buckets, ttl.toMillis(), queueSize,
                sampleModulo, timeout, System::currentTimeMillis, suppression, quota);
    }

    /**
     * Entries per slot and collector ({@code max-entries-per-slot}) and the share of them one CLIENT identifier
     * (application + destination) or the SERVER entries of one application may use.
     */
    @Bean
    public SlotQuota otlpLinkIndexSlotQuota(@Value("${" + PREFIX + "max-entries-per-slot:1000000}") int maxEntriesPerSlot,
                                            @Value("${" + PREFIX + "max-entries-per-identifier-percent:20}") int clientPercent,
                                            @Value("${" + PREFIX + "max-server-entries-per-application-percent:100}") int serverPercent) {
        return new SlotQuota(maxEntriesPerSlot, clientPercent, serverPercent);
    }

    /**
     * The suppression list ({@link LinkIndexSuppression}): CLIENT identifiers the join job found unmatched for a day
     * are not indexed, apart from a probe per slot. {@code suppress.enabled=false} applies only the static
     * {@code suppress.force} rules.
     */
    @Bean(initMethod = "start", destroyMethod = "close")
    public LinkIndexSuppression otlpLinkIndexSuppression(LinkIndexRedisConnector connector,
                                                         @Value("${" + PREFIX + "suppress.enabled:true}") boolean enabled,
                                                         @Value("${" + PREFIX + "suppress.refresh:60s}") Duration refresh,
                                                         @Value("${" + PREFIX + "suppress.probe-per-slot:100}") int probePerSlot,
                                                         @Value("${" + PREFIX + "suppress.force:}") String force,
                                                         @Value("${" + PREFIX + "suppress.pin:}") String pin,
                                                         @Value("${" + PREFIX + "redis.command-timeout:2s}") Duration timeout) {
        final SuppressStore store = enabled ? new SuppressStore(connector.connect(), timeout) : null;
        final LinkIndexSuppression suppression = new LinkIndexSuppression(store, refresh, probePerSlot,
                SuppressKey.parseRules(force), SuppressKey.parseRules(pin));
        logger.info("Link index suppression {}", suppression);
        return suppression;
    }

    @Bean
    public LinkIndexTraceService otlpLinkIndexTraceService(LinkIndexWriter writer, ServiceTypeRegistryService registry) {
        return new LinkIndexTraceService(writer, registry);
    }

    @Bean
    public LinkIndexSuppressionMetrics otlpLinkIndexSuppressionMetrics(LinkIndexSuppression suppression, MeterRegistry meterRegistry) {
        return new LinkIndexSuppressionMetrics(suppression, meterRegistry);
    }

    /**
     * Registers the suppression counters under {@value #METRIC_PREFIX}: {@code suppressed} (entries not written),
     * {@code probed} (entries of suppressed identifiers still written), {@code suppress.active} (identifiers of the
     * last snapshot) and {@code suppress.refresh.errors}. Kept apart from {@code dropped}: suppression is not a loss.
     */
    public static class LinkIndexSuppressionMetrics {
        public LinkIndexSuppressionMetrics(LinkIndexSuppression suppression, MeterRegistry registry) {
            FunctionCounter.builder(METRIC_PREFIX + "suppressed", suppression, s -> (double) s.getSuppressed()).register(registry);
            FunctionCounter.builder(METRIC_PREFIX + "probed", suppression, s -> (double) s.getProbed()).register(registry);
            FunctionCounter.builder(METRIC_PREFIX + "suppress.refresh.errors", suppression, s -> (double) s.getRefreshErrors()).register(registry);
            Gauge.builder(METRIC_PREFIX + "suppress.active", suppression, LinkIndexSuppression::getActiveCount).register(registry);
        }
    }

    @Bean
    public LinkIndexWriterMetrics otlpLinkIndexWriterMetrics(LinkIndexWriter writer, SlotQuota quota, MeterRegistry meterRegistry) {
        return new LinkIndexWriterMetrics(writer, quota, meterRegistry);
    }

    /**
     * Registers the writer counters under {@value #METRIC_PREFIX}: {@code dropped} is tagged with the reason
     * ({@link LinkIndexWriter.DropReason}) and the kind ({@code client} / {@code server}). The quota gauges describe
     * the last ended slot: {@code slot.entries}, {@code slot.limit.reached.second} (60 when the slot quota was not
     * reached) and {@code quota.limited.identifiers}.
     */
    public static class LinkIndexWriterMetrics {
        public LinkIndexWriterMetrics(LinkIndexWriter writer, SlotQuota quota, MeterRegistry registry) {
            counter(registry, "written", writer, LinkIndexWriter::getWritten);
            counter(registry, "expireat.sent", writer, LinkIndexWriter::getExpireAtSent);
            counter(registry, "not.sampled", writer, LinkIndexWriter::getNotSampled);
            counter(registry, "write.errors", writer, LinkIndexWriter::getWriteErrors);
            for (LinkIndexWriter.DropReason reason : LinkIndexWriter.DropReason.values()) {
                for (LinkKind kind : LinkKind.values()) {
                    FunctionCounter.builder(METRIC_PREFIX + "dropped", writer, w -> (double) w.getDropped(reason, kind))
                            .tag("reason", reason.tag())
                            .tag("kind", kind.name().toLowerCase(Locale.ROOT))
                            .register(registry);
                }
            }
            Gauge.builder(METRIC_PREFIX + "queue.depth", writer, LinkIndexWriter::getQueueSize).register(registry);
            Gauge.builder(METRIC_PREFIX + "slot.entries", quota, SlotQuota::getSlotEntries).register(registry);
            Gauge.builder(METRIC_PREFIX + "slot.limit.reached.second", quota, SlotQuota::getSlotLimitReachedSecond).register(registry);
            Gauge.builder(METRIC_PREFIX + "quota.limited.identifiers", quota, SlotQuota::getLimitedIdentifiers).register(registry);
            FunctionCounter.builder(METRIC_PREFIX + "quota.untracked", quota, q -> (double) q.getUntracked()).register(registry);
        }

        private static void counter(MeterRegistry registry, String name, LinkIndexWriter writer,
                                    ToLongFunction<LinkIndexWriter> function) {
            final ToDoubleFunction<LinkIndexWriter> value = w -> (double) function.applyAsLong(w);
            FunctionCounter.builder(METRIC_PREFIX + name, writer, value).register(registry);
        }
    }
}
