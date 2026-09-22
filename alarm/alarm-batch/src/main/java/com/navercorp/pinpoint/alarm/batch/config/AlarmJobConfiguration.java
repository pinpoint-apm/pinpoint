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
package com.navercorp.pinpoint.alarm.batch.config;

import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryService;
import com.navercorp.pinpoint.alarm.service.AlarmEvaluationService;
import com.navercorp.pinpoint.alarm.service.EffectiveAlarmRuleBulkResolutionService;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Evaluates the rules whose next check is due.
 *
 * <p>A rule names the data source it measures and the module that owns that data source
 * contributes the {@link MetricQueryService} for it, so this job routes by code rather
 * than knowing any backend.
 *
 * <p>The sweep asks the database only for the data sources this process installed, so a
 * deployment can be split across processes without each one reading and rejecting the
 * others' rules. The cost is that a rule whose data source <em>no</em> process installed is
 * not read by anyone: it stays enabled, its next check is never advanced, and nothing is
 * reported to its owner. No single process can tell that case apart from a rule another
 * process owns, so detecting it belongs outside the sweep -- a rule whose next check went
 * stale by several of its own intervals is the signal, and it needs no catalog to find.
 */
@Configuration
public class AlarmJobConfiguration {

    /** Where a continuation picks the sweep back up; the rules come back ordered by id. */
    private static final String LAST_RULE_ID_KEY = "alarm.evaluation.lastRuleId";

    private static final Logger logger = LogManager.getLogger(AlarmJobConfiguration.class);
    private static final long SLOW_EVALUATION_WARN_MILLIS = TimeUnit.SECONDS.toMillis(60);

    @Bean
    public ThreadPoolTaskExecutor alarmTaskExecutor(
            @Value("${pinpoint.modules.batch.alarm.worker.coreSize:4}") int coreSize,
            @Value("${pinpoint.modules.batch.alarm.worker.maxSize:4}") int maxSize) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(1024);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("alarm-");
        return executor;
    }

    @Bean
    public Job alarmEvaluationJob(JobRepository jobRepository, Step alarmStep) {
        return new JobBuilder("alarmEvaluationJob", jobRepository)
                .start(alarmStep)
                .build();
    }

    /**
     * The step runs without a transaction of its own.
     *
     * <p>Everything the tasklet writes goes through its own REQUIRES_NEW transactions, so that
     * one rule's failure cannot roll back another's, and the job repository writes its metadata
     * to a different data source. A transaction here would therefore have nothing to roll back
     * and nothing to make consistent -- it would only hold an alarm connection and its read
     * view open for the length of the whole sweep.
     */
    @Bean
    public Step alarmStep(JobRepository jobRepository, Tasklet alarmTasklet) {
        return new StepBuilder("alarmStep", jobRepository)
                .tasklet(alarmTasklet, new ResourcelessTransactionManager())
                .build();
    }

    @Bean
    public Tasklet alarmTasklet(
            AlarmRuleV2Dao alarmRuleV2Dao,
            EffectiveAlarmRuleBulkResolutionService effectiveAlarmRuleBulkResolver,
            AlarmEvaluationService evaluationService,
            List<MetricQueryService> metricQueryServices,
            @Qualifier("alarmTaskExecutor") ThreadPoolTaskExecutor taskExecutor,
            @Value("${pinpoint.modules.batch.alarm.batchSize:300}") int batchSize) {
        // Rejected here rather than left to behave oddly: batchSize 0 makes the due-rule
        // query return nothing and every sweep look like a successful empty one.
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be greater than zero");
        }
        Map<String, MetricQueryService> serviceMap = metricQueryServices.stream()
                .collect(Collectors.toMap(s -> s.getDataSource().name(), service -> service));
        // What this process can evaluate, which is what it asks the database for. More than one
        // evaluation process may share the alarm tables, one per set of data sources it has the
        // stores for, and a rule read by a process that cannot evaluate it is not merely
        // skipped: the failure moves the rule's next check and leaves a CHECK_FAILED row.
        Set<String> evaluableDataSources = Set.copyOf(serviceMap.keySet());
        if (evaluableDataSources.isEmpty()) {
            throw new IllegalStateException("No metric query service is installed, "
                    + "so this process would evaluate nothing");
        }
        logger.info("Alarm evaluation is scoped to data sources: {}", evaluableDataSources);
        return (contribution, chunkContext) -> {
            ExecutionContext stepContext = chunkContext.getStepContext()
                    .getStepExecution().getExecutionContext();
            long afterId = stepContext.containsKey(LAST_RULE_ID_KEY)
                    ? stepContext.getLong(LAST_RULE_ID_KEY) : 0L;

            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            List<AlarmRuleV2> fetched = alarmRuleV2Dao.selectDueEnabledRulesAfter(
                    afterId, batchSize, now, evaluableDataSources);
            if (fetched.isEmpty()) {
                stepContext.remove(LAST_RULE_ID_KEY);
                return RepeatStatus.FINISHED;
            }

            // Resolution failures are reported per rule, not raised: one rule pointing at a
            // deleted bundle item must not stop the rest of the sweep.
            EffectiveAlarmRuleBulkResolutionService.ResolveResult resolveResult =
                    effectiveAlarmRuleBulkResolver.resolveWithFailures(fetched);
            resolveResult.failures().forEach(failure -> handleEvaluationFailureSafely(
                    evaluationService, failure.rule(), failure.exception()));

            List<AlarmRuleV2> rules = resolveResult.rules();

            long evaluationStartNanos = System.nanoTime();
            // Submitted one at a time, because submit() rejects once the queue is full and the
            // pool has no thread to take the task. Letting that out of the tasklet would end the
            // sweep with the rules already submitted still running and nobody waiting on them,
            // and with no rule recording why it was not evaluated. A rejected rule is reported
            // like any other failure and comes back due on a later sweep.
            List<AlarmRuleV2> submittedRules = new ArrayList<>(rules.size());
            List<Future<?>> futures = new ArrayList<>(rules.size());
            for (AlarmRuleV2 rule : rules) {
                try {
                    futures.add(taskExecutor.submit(() -> {
                        MetricQueryService metricQueryService = serviceMap.get(rule.getDataSource());
                        if (metricQueryService == null) {
                            // AlarmDataSourceRegistry signals an unknown code the same way.
                            throw new IllegalArgumentException(
                                    "No metric query service for dataSource=" + rule.getDataSource());
                        }
                        evaluationService.evaluate(rule, metricQueryService);
                    }));
                    submittedRules.add(rule);
                } catch (TaskRejectedException rejected) {
                    recordFailure(evaluationService, rule, rejected);
                }
            }

            // Waited on without a deadline of its own. Every way an evaluation can block already
            // ends on its own -- the metric backend's own request timeout, the statement timeout
            // on the alarm queries, the lock wait on the state row -- so a deadline here would
            // only be a second, shorter one layered over those. That is worse than none: the
            // interrupt it cancels with is not binding on a query already in the driver, so the
            // rule is recorded failed while its evaluation runs on and can still store a result
            // over a newer one, and the failure carries a bare timeout instead of the reason the
            // backend gave. A backend that can hang forever needs a bound where the hang is, not
            // a blanket one here that fires first on every backend that already has one.
            for (int i = 0; i < futures.size(); i++) {
                AlarmRuleV2 rule = submittedRules.get(i);
                Future<?> future = futures.get(i);
                try {
                    future.get();
                } catch (ExecutionException e) {
                    recordFailure(evaluationService, rule, e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Alarm evaluation sweep was interrupted with {} rules left",
                            futures.size() - i);
                    futures.subList(i, futures.size()).forEach(f -> f.cancel(true));
                    return RepeatStatus.FINISHED;
                }
            }

            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - evaluationStartNanos);
            if (elapsedMillis > SLOW_EVALUATION_WARN_MILLIS) {
                logger.warn("Alarm evaluation took longer than 60s: elapsed_ms={}, rule_count={}",
                        elapsedMillis, rules.size());
            }

            // Carried across continuations so the sweep advances even when a rule stays due.
            // Evaluating a rule normally moves its next check forward, which is what keeps the
            // next query from returning it -- but recording a failure is allowed to fail, and
            // without a cursor a batch of rules whose state could not be written comes back on
            // every continuation and the sweep never ends. A rule that becomes due behind the
            // cursor waits for the next sweep, which is seconds away.
            stepContext.putLong(LAST_RULE_ID_KEY, fetched.get(fetched.size() - 1).getId());

            // A full batch means there may be more due rules behind it.
            return fetched.size() >= batchSize ? RepeatStatus.CONTINUABLE : RepeatStatus.FINISHED;
        };
    }

    private static void recordFailure(AlarmEvaluationService evaluationService,
                                      AlarmRuleV2 rule,
                                      Throwable failure) {
        logger.error("Failed or timed out evaluating alarm rule: {}", rule, failure);
        handleEvaluationFailureSafely(evaluationService, rule,
                failure instanceof RuntimeException re ? re : new RuntimeException(failure));
    }

    /**
     * Recording a failure must not itself fail the sweep: the state row it writes is what
     * schedules the rule's next check, and losing the whole batch over one unwritable row
     * would stop every other rule from being evaluated.
     */
    private static void handleEvaluationFailureSafely(
            AlarmEvaluationService evaluationService,
            AlarmRuleV2 rule,
            RuntimeException failure) {
        try {
            evaluationService.handleEvaluationFailed(rule, failure);
        } catch (RuntimeException recordFailure) {
            logger.error("Failed to record alarm check failure: {}", rule, recordFailure);
        }
    }
}
