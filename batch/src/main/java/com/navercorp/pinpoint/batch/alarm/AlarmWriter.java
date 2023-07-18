/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.batch.alarm.vo.AppAlarmChecker;
import com.navercorp.pinpoint.batch.alarm.vo.CheckerResult;
import com.navercorp.pinpoint.batch.service.AlarmService;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class AlarmWriter implements ItemWriter<AppAlarmChecker>, StepExecutionListener {

    private final Logger logger = LogManager.getLogger(AlarmWriter.class);

    private final AlarmMessageSender alarmMessageSender;
    private final AlarmService alarmService;
    private final AlarmWriterInterceptor interceptor;

    private StepExecution stepExecution;

    public AlarmWriter(
            AlarmMessageSender alarmMessageSender,
            AlarmService alarmService,
            @Nullable AlarmWriterInterceptor alarmWriterInterceptor
    ) {
        this.alarmMessageSender = Objects.requireNonNull(alarmMessageSender, "alarmMessageSender");
        this.alarmService = Objects.requireNonNull(alarmService, "alarmService");
        this.interceptor = Objects.requireNonNullElseGet(alarmWriterInterceptor, DefaultAlarmWriterInterceptor::new);
    }

    @Override
    public void beforeStep(@Nonnull StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public ExitStatus afterStep(@Nonnull StepExecution stepExecution) {
        return null;
    }

    @Override
    public void write(@Nonnull List<? extends AppAlarmChecker> appAlarmCheckers) {
        List<AlarmChecker<?>> checkers = flatten(appAlarmCheckers);
        interceptor.before(checkers);
        try {
            for (AppAlarmChecker appAlarmChecker: appAlarmCheckers) {
                execute(appAlarmChecker.getChildren());
            }
        } finally {
            interceptor.after(checkers);
        }
    }

    public List<AlarmChecker<?>> flatten(List<? extends AppAlarmChecker> appAlarmCheckers) {
        List<AlarmChecker<?>> checkers = new ArrayList<>();
        for (AppAlarmChecker appAlarmChecker : appAlarmCheckers) {
            checkers.addAll(appAlarmChecker.getChildren());
        }
        return checkers;
    }

    private void execute(List<? extends AlarmChecker<?>> checkers) {
        if (CollectionUtils.isEmpty(checkers)) {
            return;
        }

        if (!haveSameApplicationId(checkers)) {
            logger.error("All checkers must have same applicationId");
            throw new IllegalArgumentException("All checkers must have same applicationId");
        }

        final String applicationId = checkers.get(0).getRule().getApplicationId();
        Map<String, CheckerResult> beforeCheckerResults = alarmService.selectBeforeCheckerResults(applicationId);

        for (AlarmChecker<?> checker : checkers) {
            final Rule rule = checker.getRule();
            final String ruleId = rule.getRuleId();
            final String checkerName = rule.getCheckerName();

            CheckerResult beforeCheckerResult = Objects.requireNonNullElseGet(
                    beforeCheckerResults.get(ruleId),
                    () -> new CheckerResult(ruleId, applicationId, checkerName, false, 0, 1)
            );

            if (checker.isDetected()) {
                sendAlarmMessage(beforeCheckerResult, checker);
            }

            alarmService.updateBeforeCheckerResult(beforeCheckerResult, checker);
        }
    }

    private boolean haveSameApplicationId(List<? extends AlarmChecker<?>> checkers) {
        if (CollectionUtils.isEmpty(checkers)) {
            return true;
        }

        final String applicationId = checkers.get(0).getRule().getApplicationId();
        return checkers.stream()
                .map(AlarmChecker::getRule)
                .map(Rule::getApplicationId)
                .allMatch(applicationId::equals);
    }

    private void sendAlarmMessage(CheckerResult beforeCheckerResult, AlarmChecker<?> checker) {
        if (isTurnToSendAlarm(beforeCheckerResult)) {
            if (checker.isSMSSend()) {
                alarmMessageSender.sendSms(checker, beforeCheckerResult.getSequenceCount() + 1);
            }
            if (checker.isEmailSend()) {
                alarmMessageSender.sendEmail(checker, beforeCheckerResult.getSequenceCount() + 1);
            }
            if (checker.isWebhookSend()) {
                alarmMessageSender.sendWebhook(checker, beforeCheckerResult.getSequenceCount() + 1);
            }
        }
    }

    private boolean isTurnToSendAlarm(CheckerResult beforeCheckerResult) {
        if (!beforeCheckerResult.isDetected()) {
            return true;
        }

        int sequenceCount = beforeCheckerResult.getSequenceCount() + 1;

        return sequenceCount == beforeCheckerResult.getTimingCount();
    }
}
