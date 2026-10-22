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

package com.navercorp.pinpoint.batch.otlp;

import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributor;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkIndexStore;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkJoinService;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkMapWriter;
import com.navercorp.pinpoint.otlp.trace.linkindex.redis.LinkIndexRedisConnector;
import com.navercorp.pinpoint.otlp.trace.linkindex.redis.LinkIndexRedisConnectorFactory;
import com.navercorp.pinpoint.otlp.trace.linkindex.redis.LinkIndexRedisProperties;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkIndexKeys;
import com.navercorp.pinpoint.otlp.trace.linkindex.suppress.SuppressManager;
import com.navercorp.pinpoint.otlp.trace.linkindex.suppress.SuppressPolicy;
import com.navercorp.pinpoint.otlp.trace.linkindex.suppress.SuppressStore;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.builder.TaskletStepBuilder;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

/**
 * {@code otlpLinkJoinJob}: joins the OTLP link index written by the OTLP trace collectors and writes
 * the server-map rows of OTel calls whose callee did not receive the Pinpoint parent application.
 * Disabled by default ({@code job.otlp.link-join.enable}).
 * <p>
 * {@code buckets} and {@code sample-modulo} must be the same as the collectors'
 * {@code pinpoint.collector.otlptrace.link-index.*}, and {@code map-link.*} the same as the
 * collectors' {@code collector.map-link.*}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "job.otlp.link-join.enable", havingValue = "true")
public class OtlpLinkJoinJobConfig {

    private static final Logger logger = LogManager.getLogger(OtlpLinkJoinJobConfig.class);

    static final String PREFIX = "job.otlp.link-join.";
    /** wait after the slot end for the collectors' in-flight writes, aligned with the cron second */
    static final long SETTLE_MILLIS = 30_000;

    public OtlpLinkJoinJobConfig() {
        logger.info("Install {}", OtlpLinkJoinJobConfig.class.getSimpleName());
    }

    @Bean
    public LinkIndexRedisProperties otlpLinkJoinRedisProperties(
            @Value("${" + PREFIX + "redis.mode:cluster}") String mode,
            @Value("${" + PREFIX + "redis.nodes:}") String nodes,
            @Value("${" + PREFIX + "redis.password:}") String password,
            @Value("${" + PREFIX + "redis.connect-timeout:3s}") Duration connectTimeout,
            @Value("${" + PREFIX + "redis.command-timeout:10s}") Duration commandTimeout) {
        return new LinkIndexRedisProperties(mode, nodes, password, connectTimeout, commandTimeout);
    }

    @Bean(destroyMethod = "close")
    public LinkIndexRedisConnector otlpLinkJoinRedisConnector(LinkIndexRedisProperties properties,
                                                              ObjectProvider<LinkIndexRedisConnectorFactory> factory) {
        final LinkIndexRedisConnectorFactory custom = factory.getIfAvailable();
        if (custom != null) {
            final LinkIndexRedisConnector connector = custom.create(properties);
            logger.info("Link index store from {}: {}", custom.getClass().getSimpleName(), connector);
            return connector;
        }
        if (!properties.hasNodes()) {
            throw new IllegalStateException(PREFIX + "redis.nodes is empty while " + PREFIX + "enable=true");
        }
        logger.info("Link index store {}", properties);
        return properties.newConnector();
    }

    @Bean
    public LinkIndexStore otlpLinkIndexStore(LinkIndexRedisConnector connector, LinkIndexRedisProperties properties) {
        return new LinkIndexStore(connector.connect(), properties.commandTimeout());
    }

    @Bean
    public LinkMapWriter otlpLinkMapWriter(HbaseOperations hbaseOperations,
                                           TableNameProvider tableNameProvider,
                                           @Qualifier("uidRowKeyDistributor") RowKeyDistributor rowKeyDistributor,
                                           TimeSlot timeSlot,
                                           @Value("${" + PREFIX + "map-link.avg.enable:true}") boolean writeSum,
                                           @Value("${" + PREFIX + "map-link.max.enable:true}") boolean writeMax) {
        return new LinkMapWriter(hbaseOperations, tableNameProvider, rowKeyDistributor.getByteHasher(), timeSlot, writeSum, writeMax);
    }

    @Bean(destroyMethod = "close")
    public LinkJoinService otlpLinkJoinService(LinkIndexStore store,
                                               LinkMapWriter writer,
                                               ServiceTypeRegistryService registry,
                                               @Value("${" + PREFIX + "buckets:65536}") int buckets,
                                               @Value("${" + PREFIX + "threads:8}") int threads,
                                               @Value("${" + PREFIX + "sample-modulo:1}") int sampleModulo,
                                               @Value("${" + PREFIX + "ttl:5m}") Duration ttl,
                                               @Value("${" + PREFIX + "chunk-buckets:16}") int chunkBuckets,
                                               @Value("${" + PREFIX + "bucket-warn-entries:512}") int bucketWarnEntries) {
        logger.info("{} buckets:{} threads:{} sampleModulo:{} ttl:{} chunkBuckets:{} bucketWarnEntries:{}",
                OtlpLinkJoinLauncher.JOB_NAME, buckets, threads, sampleModulo, ttl, chunkBuckets, bucketWarnEntries);
        return new LinkJoinService(store, writer, registry::findServiceType, buckets, threads, sampleModulo,
                chunkBuckets, bucketWarnEntries);
    }

    /**
     * Suppression list: CLIENT identifiers that stay unmatched for a day are marked ACTIVE in the store, and the
     * collectors stop indexing them (apart from a probe). Any match releases an identifier at once.
     * {@code suppress.enabled=false} leaves the job with its join and cleanup steps only.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = PREFIX + "suppress.enabled", havingValue = "true", matchIfMissing = true)
    public static class SuppressConfig {

        @Bean
        public SuppressManager otlpLinkSuppressManager(LinkIndexRedisConnector connector,
                                                       LinkIndexRedisProperties properties,
                                                       @Value("${" + PREFIX + "suppress.activate-after:24h}") Duration activateAfter,
                                                       @Value("${" + PREFIX + "suppress.activate-slots:1440}") long activateSlots,
                                                       @Value("${" + PREFIX + "suppress.min-unmatched-per-slot:100}") int minUnmatchedPerSlot,
                                                       @Value("${" + PREFIX + "suppress.idle-ttl:7d}") Duration idleTtl,
                                                       @Value("${" + PREFIX + "suppress.max-keys:1000}") int maxKeys) {
            final SuppressPolicy policy = new SuppressPolicy(activateAfter.toMillis() / LinkIndexKeys.SLOT_MILLIS, activateSlots,
                    minUnmatchedPerSlot, idleTtl.toMillis() / LinkIndexKeys.SLOT_MILLIS, maxKeys);
            logger.info("{} suppression {}", OtlpLinkJoinLauncher.JOB_NAME, policy);
            return new SuppressManager(new SuppressStore(connector.connect(), properties.commandTimeout()), policy);
        }

        @Bean
        public Step otlpLinkSuppressStep(JobRepository jobRepository,
                                         LinkJoinService service,
                                         SuppressManager manager,
                                         @Qualifier("otlpLinkJoinStepCustomListener") Optional<StepExecutionListener> customListener) {
            final TaskletStepBuilder builder = new StepBuilder("otlpLinkSuppressStep", jobRepository)
                    .tasklet(new OtlpLinkJoinTasklets.Suppress(service, manager), stepTransactionManager());
            customListener.ifPresent(builder::listener);
            return builder.build();
        }

        @Bean
        public OtlpLinkSuppressMetrics otlpLinkSuppressMetrics(SuppressManager manager, ObjectProvider<MeterRegistry> meterRegistry) {
            final MeterRegistry registry = meterRegistry.getIfAvailable();
            if (registry == null) {
                return new OtlpLinkSuppressMetrics();
            }
            return new OtlpLinkSuppressMetrics(manager, registry);
        }
    }

    /**
     * Registers the suppression list totals under {@value OtlpLinkJoinMetrics#METRIC_PREFIX}{@code suppress.}:
     * {@code activated}, {@code released}, {@code expired}, {@code errors} counters and {@code candidates},
     * {@code active} gauges of the last run.
     */
    public static class OtlpLinkSuppressMetrics {
        static final String METRIC_PREFIX = OtlpLinkJoinMetrics.METRIC_PREFIX + "suppress.";

        OtlpLinkSuppressMetrics() {
        }

        public OtlpLinkSuppressMetrics(SuppressManager manager, MeterRegistry registry) {
            FunctionCounter.builder(METRIC_PREFIX + "activated", manager, m -> (double) m.getActivated()).register(registry);
            FunctionCounter.builder(METRIC_PREFIX + "released", manager, m -> (double) m.getReleased()).register(registry);
            FunctionCounter.builder(METRIC_PREFIX + "expired", manager, m -> (double) m.getExpired()).register(registry);
            FunctionCounter.builder(METRIC_PREFIX + "errors", manager, m -> (double) m.getErrors()).register(registry);
            Gauge.builder(METRIC_PREFIX + "candidates", manager, SuppressManager::getLastCandidates).register(registry);
            Gauge.builder(METRIC_PREFIX + "active", manager, SuppressManager::getLastActive).register(registry);
        }
    }

    /**
     * The steps touch only the link index store and HBase, never a JDBC DataSource, so they run
     * without a resource transaction (as the alarm batch does). Binding them to the main
     * {@code transactionManager} would open a JDBC connection per step for nothing, and would
     * fail where the main DataSource needs step-scoped context to hand one out.
     */
    private static PlatformTransactionManager stepTransactionManager() {
        return new ResourcelessTransactionManager();
    }

    /**
     * Optional hook for deployments whose HBase table names depend on step context (same contract as
     * {@code cleanupStepCustomListener} of the inactive-agent cleanup job): a {@code StepExecutionListener}
     * bean named {@code otlpLinkJoinStepCustomListener} is registered on both steps when present.
     */
    @Bean
    public Step otlpLinkJoinStep(JobRepository jobRepository,
                                 LinkJoinService service,
                                 @Qualifier("otlpLinkJoinStepCustomListener") Optional<StepExecutionListener> customListener) {
        final TaskletStepBuilder builder = new StepBuilder("otlpLinkJoinStep", jobRepository)
                .tasklet(new OtlpLinkJoinTasklets.Join(service), stepTransactionManager());
        customListener.ifPresent(builder::listener);
        return builder.build();
    }

    @Bean
    public Step otlpLinkCleanupStep(JobRepository jobRepository,
                                    LinkJoinService service,
                                    @Value("${" + PREFIX + "ttl:5m}") Duration ttl,
                                    @Qualifier("otlpLinkJoinStepCustomListener") Optional<StepExecutionListener> customListener) {
        final OtlpLinkJoinTasklets.Cleanup cleanup = new OtlpLinkJoinTasklets.Cleanup(service, jobRepository, ttl.toMillis(), System::currentTimeMillis);
        final TaskletStepBuilder builder = new StepBuilder("otlpLinkCleanupStep", jobRepository)
                .tasklet(cleanup, stepTransactionManager());
        customListener.ifPresent(builder::listener);
        return builder.build();
    }

    @Bean
    public Job otlpLinkJoinJob(JobRepository jobRepository,
                               @Qualifier("otlpLinkJoinStep") Step joinStep,
                               @Qualifier("otlpLinkSuppressStep") Optional<Step> suppressStep,
                               @Qualifier("otlpLinkCleanupStep") Step cleanupStep) {
        // join -> cleanup -> suppress: the cleanup step completes only once the job of slot k-1 has, which also
        // orders the suppression list updates of consecutive slots across batch instances (no lock on the list)
        final SimpleJobBuilder builder = new JobBuilder(OtlpLinkJoinLauncher.JOB_NAME, jobRepository)
                .start(joinStep)
                .next(cleanupStep);
        suppressStep.ifPresent(builder::next);
        return builder.build();
    }

    @Bean
    public OtlpLinkJoinLauncher otlpLinkJoinLauncher(@Qualifier("jobLauncher") JobLauncher jobLauncher,
                                                     @Qualifier("otlpLinkJoinJob") Job job,
                                                     @Value("${" + PREFIX + "ttl:5m}") Duration ttl) {
        return new OtlpLinkJoinLauncher(jobLauncher, job, SETTLE_MILLIS, ttl.toMillis(), System::currentTimeMillis);
    }

    @Bean
    public OtlpLinkJoinMetrics otlpLinkJoinMetrics(LinkJoinService service,
                                                   OtlpLinkJoinLauncher launcher,
                                                   ObjectProvider<MeterRegistry> meterRegistry) {
        final MeterRegistry registry = meterRegistry.getIfAvailable();
        if (registry == null) {
            logger.info("No MeterRegistry: {} metrics are not registered", OtlpLinkJoinLauncher.JOB_NAME);
            return new OtlpLinkJoinMetrics();
        }
        return new OtlpLinkJoinMetrics(service, launcher, registry);
    }

    /**
     * Registers the job's running totals under {@value #METRIC_PREFIX}. Counters sum the
     * {@code JoinSummary} / {@code CleanupSummary} of every run; the gauges describe the last joined slot
     * and the launcher's lag. The job and step timers come from Spring Batch itself
     * ({@code spring.batch.job}, {@code spring.batch.step}).
     */
    public static class OtlpLinkJoinMetrics {
        static final String METRIC_PREFIX = "batch.otlp.linkjoin.";

        OtlpLinkJoinMetrics() {
        }

        public OtlpLinkJoinMetrics(LinkJoinService service, OtlpLinkJoinLauncher launcher, MeterRegistry registry) {
            counter(registry, "entries", service, LinkJoinService::getTotalEntries);
            counter(registry, "pairs", service, LinkJoinService::getTotalPairs);
            counter(registry, "links", service, LinkJoinService::getTotalLinks);
            counter(registry, "rows", service, LinkJoinService::getTotalRows);
            counter(registry, "unmatched", service, LinkJoinService::getTotalUnmatched);
            FunctionCounter.builder(METRIC_PREFIX + "dropped", service, s -> (double) s.getTotalDroppedServer())
                    .tag("side", "server").register(registry);
            FunctionCounter.builder(METRIC_PREFIX + "dropped", service, s -> (double) s.getTotalDroppedClient())
                    .tag("side", "client").register(registry);
            counter(registry, "deleted.keys", service, LinkJoinService::getTotalDeletedKeys);
            Gauge.builder(METRIC_PREFIX + "max.bucket.entries", service, LinkJoinService::getLastMaxBucketEntries).register(registry);
            Gauge.builder(METRIC_PREFIX + "oversized.buckets", service, LinkJoinService::getLastOversizedBuckets).register(registry);

            FunctionCounter.builder(METRIC_PREFIX + "skipped.slots", launcher, l -> (double) l.getSkippedSlots()).register(registry);
            FunctionCounter.builder(METRIC_PREFIX + "failed.launches", launcher, l -> (double) l.getFailedLaunches()).register(registry);
            Gauge.builder(METRIC_PREFIX + "last.done.slot.age.seconds", launcher, OtlpLinkJoinLauncher::getLastDoneSlotAgeSeconds).register(registry);
        }

        private static void counter(MeterRegistry registry, String name, LinkJoinService service,
                                    ToLongFunction<LinkJoinService> function) {
            final ToDoubleFunction<LinkJoinService> value = s -> (double) function.applyAsLong(s);
            FunctionCounter.builder(METRIC_PREFIX + name, service, value).register(registry);
        }
    }
}
