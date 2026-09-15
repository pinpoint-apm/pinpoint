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
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * That a rule the executor refuses is reported rather than ending the sweep.
 *
 * <p>submit() throws once the queue is full and no thread can take the task, which on a
 * single-threaded scheduler would leave the rules already submitted running with nobody
 * waiting on them and no rule recording why it was skipped.
 */
class AlarmJobTaskletRejectionTest {

    @Test
    void aRefusedRuleIsReportedAndTheRestOfTheSweepStillRuns() throws Exception {
        AlarmRuleV2 refused = rule(10L);
        AlarmRuleV2 accepted = rule(20L);
        AlarmEvaluationService evaluationService = mock(AlarmEvaluationService.class);

        Tasklet tasklet = tasklet(List.of(refused, accepted), evaluationService);

        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
        assertEquals(RepeatStatus.FINISHED,
                tasklet.execute(null, new ChunkContext(new StepContext(stepExecution))));

        // Refusing to run a rule is not the rule's owner's problem, so it is infrastructure.
        verify(evaluationService).handleInfrastructureFailed(eq(refused), any());
        // And the accepted rule is still the one the wait loop reports on. Indexing the whole
        // page rather than what was submitted would pin this failure on the refused rule.
        verify(evaluationService).handleRuleConfigurationFailed(eq(accepted), any());
    }

    private Tasklet tasklet(List<AlarmRuleV2> due, AlarmEvaluationService evaluationService) {
        AlarmRuleV2Dao ruleDao = mock(AlarmRuleV2Dao.class);
        when(ruleDao.selectDueEnabledRulesAfter(anyLong(), anyInt(), any(LocalDateTime.class)))
                .thenReturn(due);

        EffectiveAlarmRuleBulkResolutionService resolver =
                mock(EffectiveAlarmRuleBulkResolutionService.class);
        when(resolver.resolveWithFailures(any()))
                .thenAnswer(invocation -> new EffectiveAlarmRuleBulkResolutionService.ResolveResult(
                        invocation.getArgument(0), List.of()));

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor() {
            private int submitted = 0;

            @Override
            public Future<?> submit(Runnable task) {
                // The first rule arrives when the queue is full; the second when it is not.
                if (submitted++ == 0) {
                    throw new TaskRejectedException("queue is full");
                }
                return super.submit(task);
            }
        };
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.initialize();

        // No metric query service is registered, so whatever does get submitted fails as a rule
        // configuration error -- which is what tells the two failures apart.
        return new AlarmJobConfiguration().alarmTasklet(
                ruleDao, resolver, evaluationService,
                new AlarmEvaluationFailureClassifier(), List.of(), executor, 10, 6);
    }

    private static AlarmRuleV2 rule(Long id) {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setId(id);
        rule.setDataSource("PRIMARY");
        return rule;
    }
}
