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

package com.navercorp.pinpoint.batch.config;

import com.navercorp.pinpoint.batch.common.BatchMetaCleanupProperties;
import com.navercorp.pinpoint.batch.dao.BatchMetaCandidateDao;
import com.navercorp.pinpoint.batch.job.BatchMetaCleanupTasklet;
import com.navercorp.pinpoint.batch.otlp.OtlpLinkJoinJobConfig;
import com.navercorp.pinpoint.batch.otlp.OtlpLinkJoinLauncher;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.Duration;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

/**
 * {@code batchMetaCleanupJob}: deletes the Spring Batch meta data ({@code BATCH_*} tables of the meta
 * data source) of job instances older than their retention. Scheduled by {@code job.cleanup.batch-meta.cron}
 * when {@code job.cleanup.batch-meta.enable} is on; dry-run by default.
 * <p>
 * The step runs without a resource transaction; the tasklet opens one transaction of the meta data source
 * per deleted instance (instance row locked, executions re-checked, then {@code deleteJobInstance}), so a
 * run of tens of thousands of instances does not become one transaction and a concurrent restart cannot
 * be swallowed.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
public class BatchMetaCleanupJobConfig {

    public static final String JOB_NAME = "batchMetaCleanupJob";
    public static final String STEP_NAME = "batchMetaCleanupStep";

    private static final Logger logger = LogManager.getLogger(BatchMetaCleanupJobConfig.class);

    @Bean
    @ConfigurationProperties(prefix = BatchMetaCleanupProperties.PREFIX)
    public BatchMetaCleanupProperties batchMetaCleanupProperties() {
        return new BatchMetaCleanupProperties();
    }

    @Bean
    public BatchMetaCandidateDao batchMetaCandidateDao(@Qualifier("metaDataDataSource") DataSource metaDataDataSource) {
        return new BatchMetaCandidateDao(metaDataDataSource);
    }

    /**
     * @param otlpLinkJoinEnabled {@code otlpLinkJoinJob} is registered here ({@link OtlpLinkJoinJobConfig} is
     *                            conditional on it), so it is deletable when {@code job-names} is empty
     * @param otlpLinkJoinTtl     the launcher revisits every slot of the last ttl through its meta data; the
     *                            retention of {@code otlpLinkJoinJob} must cover it
     */
    @Bean
    public BatchMetaCleanupTasklet batchMetaCleanupTasklet(JobRepository jobRepository,
                                                           BatchMetaCandidateDao batchMetaCandidateDao,
                                                           BatchMetaCleanupProperties batchMetaCleanupProperties,
                                                           @Qualifier("metaDataTransactionManager") PlatformTransactionManager metaDataTransactionManager,
                                                           JobRegistry jobRegistry,
                                                           @Value("${job.otlp.link-join.enable:false}") boolean otlpLinkJoinEnabled,
                                                           @Value("${job.otlp.link-join.ttl:5m}") Duration otlpLinkJoinTtl) {
        batchMetaCleanupProperties.validate();
        if (batchMetaCleanupProperties.covers(OtlpLinkJoinLauncher.JOB_NAME, otlpLinkJoinEnabled)) {
            batchMetaCleanupProperties.requireRetention(OtlpLinkJoinLauncher.JOB_NAME, otlpLinkJoinTtl,
                    "job.otlp.link-join.ttl is " + otlpLinkJoinTtl + " and the launcher revisits the slots of the last ttl through their meta data");
        }
        logger.info("{} {}", JOB_NAME, batchMetaCleanupProperties);
        // the same transaction manager as the JobRepository: deleteJobInstance joins the locked transaction
        return new BatchMetaCleanupTasklet(jobRepository, batchMetaCandidateDao, batchMetaCleanupProperties,
                new TransactionTemplate(metaDataTransactionManager), jobRegistry::getJobNames, Clock.systemDefaultZone());
    }

    @Bean
    public Step batchMetaCleanupStep(JobRepository jobRepository, BatchMetaCleanupTasklet batchMetaCleanupTasklet) {
        return new StepBuilder(STEP_NAME, jobRepository)
                .tasklet(batchMetaCleanupTasklet, new ResourcelessTransactionManager())
                .build();
    }

    @Bean
    public Job batchMetaCleanupJob(JobRepository jobRepository, @Qualifier("batchMetaCleanupStep") Step batchMetaCleanupStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(batchMetaCleanupStep)
                .build();
    }

    @Bean
    public BatchMetaCleanupMetrics batchMetaCleanupMetrics(BatchMetaCleanupTasklet tasklet, ObjectProvider<MeterRegistry> meterRegistry) {
        final MeterRegistry registry = meterRegistry.getIfAvailable();
        if (registry == null) {
            logger.info("No MeterRegistry: {} metrics are not registered", JOB_NAME);
            return new BatchMetaCleanupMetrics();
        }
        return new BatchMetaCleanupMetrics(tasklet, registry);
    }

    /**
     * Running totals of the tasklet under {@value #METRIC_PREFIX}; the job and step timers come from
     * Spring Batch itself ({@code spring.batch.job}, {@code spring.batch.step}).
     */
    public static class BatchMetaCleanupMetrics {
        static final String METRIC_PREFIX = "batch.meta.cleanup.";

        BatchMetaCleanupMetrics() {
        }

        public BatchMetaCleanupMetrics(BatchMetaCleanupTasklet tasklet, MeterRegistry registry) {
            counter(registry, "deleted", tasklet, BatchMetaCleanupTasklet::getTotalDeleted);
            counter(registry, "would.delete", tasklet, BatchMetaCleanupTasklet::getTotalWouldDelete);
            counter(registry, "failed", tasklet, BatchMetaCleanupTasklet::getTotalFailed);
            skipped(registry, "retained", tasklet, BatchMetaCleanupTasklet::getTotalSkippedRetained);
            skipped(registry, "running", tasklet, BatchMetaCleanupTasklet::getTotalSkippedRunning);
            skipped(registry, "not_allowed", tasklet, BatchMetaCleanupTasklet::getTotalSkippedNotAllowed);
            skipped(registry, "locked", tasklet, BatchMetaCleanupTasklet::getTotalSkippedLocked);
            Gauge.builder(METRIC_PREFIX + "last.run.duration.seconds", tasklet, t -> t.getLastRunDurationMillis() / 1000.0).register(registry);
            Gauge.builder(METRIC_PREFIX + "last.run.cap.reached", tasklet, t -> t.isLastRunCapReached() ? 1 : 0).register(registry);
        }

        private static void counter(MeterRegistry registry, String name, BatchMetaCleanupTasklet tasklet,
                                    ToLongFunction<BatchMetaCleanupTasklet> function) {
            final ToDoubleFunction<BatchMetaCleanupTasklet> value = t -> (double) function.applyAsLong(t);
            FunctionCounter.builder(METRIC_PREFIX + name, tasklet, value).register(registry);
        }

        private static void skipped(MeterRegistry registry, String reason, BatchMetaCleanupTasklet tasklet,
                                    ToLongFunction<BatchMetaCleanupTasklet> function) {
            final ToDoubleFunction<BatchMetaCleanupTasklet> value = t -> (double) function.applyAsLong(t);
            FunctionCounter.builder(METRIC_PREFIX + "skipped", tasklet, value).tag("reason", reason).register(registry);
        }
    }
}
