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

package com.navercorp.pinpoint.web.alarm;

import java.util.List;
import java.util.Map;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.alarm.vo.CheckerResult;
import com.navercorp.pinpoint.web.service.AlarmService;

/**
 * @author minwoo.jung
 */
public class AlarmWriter implements ItemWriter<AlarmChecker> {

    @Autowired
    private AlarmMessageSender alarmMessageSender;

    @Autowired
    private AlarmService alarmService;

    private StepExecution stepExecution;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public void write(List<? extends AlarmChecker> checkers) throws Exception {
        Map<String, CheckerResult> beforeCheckerResults = alarmService.selectBeforeCheckerResults(checkers.get(0).getRule().getApplicationId());

        for (AlarmChecker checker : checkers) {
            CheckerResult beforeCheckerResult = beforeCheckerResults.get(checker.getRule().getRuleId());

            if (beforeCheckerResult == null) {
                beforeCheckerResult = new CheckerResult(checker.getRule().getRuleId(), checker.getRule().getApplicationId(), checker.getRule().getCheckerName(), false, 0, 1);
            }

            if (checker.isDetected()) {
                sendAlarmMessage(beforeCheckerResult, checker);
            }

            alarmService.updateBeforeCheckerResult(beforeCheckerResult, checker);
        }
    }

    private void sendAlarmMessage(CheckerResult beforeCheckerResult, AlarmChecker checker) {
        if (isTurnToSendAlarm(beforeCheckerResult)) {
            if (checker.isSMSSend()) {
                alarmMessageSender.sendSms(checker, beforeCheckerResult.getSequenceCount() + 1, stepExecution);
            }
            if (checker.isEmailSend()) {
                alarmMessageSender.sendEmail(checker, beforeCheckerResult.getSequenceCount() + 1, stepExecution);
            }
        }

    }

    private boolean isTurnToSendAlarm(CheckerResult beforeCheckerResult) {
        if (!beforeCheckerResult.isDetected()) {
            return true;
        }

        int sequenceCount = beforeCheckerResult.getSequenceCount() + 1;

        if (sequenceCount == beforeCheckerResult.getTimingCount()) {
            return true;
        }

        return false;
    }
}
