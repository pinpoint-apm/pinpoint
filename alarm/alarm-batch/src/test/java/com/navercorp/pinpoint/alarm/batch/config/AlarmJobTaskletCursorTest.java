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
import com.navercorp.pinpoint.alarm.service.AlarmEvaluationService;
import com.navercorp.pinpoint.alarm.service.EffectiveAlarmRuleBulkResolutionService;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryService;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
import com.navercorp.pinpoint.alarm.vo.AlarmState;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * That a continuation picks up after the rules the previous one read.
 *
 * <p>The sweep normally stops returning a rule because evaluating it moved its next check
 * forward. Recording a failure is allowed to fail, though, so a rule can stay due -- and
 * with the query always starting from id 0, a full batch of those comes back on every
 * continuation and the sweep never ends. On a single-threaded scheduler that stops alarms
 * altogether, so the cursor is what bounds the sweep.
 */
class AlarmJobTaskletCursorTest {

    private final List<Long> requestedAfterIds = new ArrayList<>();

    @Test
    void aContinuationReadsPastTheRulesTheLastOneTook() throws Exception {
        int batchSize = 2;
        // Always a full batch of the same rules: what the query returns when nothing the
        // sweep does can make them stop being due.
        Tasklet tasklet = tasklet(batchSize, List.of(rule(10L), rule(20L)));

        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
        ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));

        assertEquals(RepeatStatus.CONTINUABLE, tasklet.execute(null, chunkContext));
        assertEquals(RepeatStatus.CONTINUABLE, tasklet.execute(null, chunkContext));
        assertEquals(RepeatStatus.CONTINUABLE, tasklet.execute(null, chunkContext));

        // Without the cursor every continuation would ask from 0 again.
        assertEquals(List.of(0L, 20L, 20L), requestedAfterIds);
    }

    @Test
    void anEmptySweepClearsTheCursorSoTheNextOneStartsOver() throws Exception {
        Tasklet tasklet = tasklet(2, List.of());

        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
        stepExecution.getExecutionContext().putLong("alarm.evaluation.lastRuleId", 99L);
        ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));

        assertEquals(RepeatStatus.FINISHED, tasklet.execute(null, chunkContext));

        assertEquals(List.of(99L), requestedAfterIds);
        assertEquals(false,
                stepExecution.getExecutionContext().containsKey("alarm.evaluation.lastRuleId"));
    }

    private Tasklet tasklet(int batchSize, List<AlarmRuleV2> due) {
        AlarmRuleV2Dao ruleDao = mock(AlarmRuleV2Dao.class);
        when(ruleDao.selectDueEnabledRulesAfter(anyLong(), anyInt(), any(LocalDateTime.class), any()))
                .thenAnswer(invocation -> {
                    requestedAfterIds.add(invocation.getArgument(0));
                    return due;
                });

        EffectiveAlarmRuleBulkResolutionService resolver =
                mock(EffectiveAlarmRuleBulkResolutionService.class);
        when(resolver.resolveWithFailures(any()))
                .thenAnswer(invocation -> new EffectiveAlarmRuleBulkResolutionService.ResolveResult(
                        invocation.getArgument(0), List.of()));

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.initialize();

        return new AlarmJobConfiguration().alarmTasklet(
                ruleDao, resolver, mock(AlarmEvaluationService.class),
                List.of(emptyResultService()), executor,
                batchSize);
    }

    /** The sweep needs a service for the rules' data source; what it answers does not matter. */
    private static MetricQueryService emptyResultService() {
        return new MetricQueryService() {
            @Override
            public AlarmDataSource getDataSource() {
                return IntegrationTestAlarmDataSource.PRIMARY;
            }

            @Override
            public MetricQueryResult query(AlarmRuleV2 rule, List<AlarmCondition> conditions,
                                           List<AlarmFilter> filters, AlarmState state) {
                return MetricQueryResult.empty();
            }
        };
    }

    private static AlarmRuleV2 rule(Long id) {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setId(id);
        rule.setDataSource("PRIMARY");
        return rule;
    }
}
