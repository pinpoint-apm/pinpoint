/*
 * Copyright 2020 NAVER Corp.
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
import com.navercorp.pinpoint.batch.alarm.checker.SlowCountChecker;
import com.navercorp.pinpoint.batch.alarm.vo.AppAlarmChecker;
import com.navercorp.pinpoint.batch.alarm.vo.CheckerResult;
import com.navercorp.pinpoint.batch.service.AlarmService;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AlarmWriterIsolationTest {

    private static final String APPLICATION_ID = "testService";
    private static final String CHECKER_NAME = CheckerCategory.SLOW_COUNT.getName();
    private static final String RULE_ID = "TEST_RULE";

    private AlarmWriter writer;

    @Mock
    AlarmMessageSender alarmMessageSender;

    @Mock
    AlarmService alarmService;

    Map<String, CheckerResult> beforeCheckerResults;

    @BeforeEach
    public void setUp() {
        writer = new AlarmWriter(alarmMessageSender, alarmService, null);

        beforeCheckerResults = new HashMap<>();
    }

    @Test
    public void whenSequenceCountIsLessThanTimingCountDoSendAlarm() {
        // given
        Rule rule = getRuleStub(APPLICATION_ID, RULE_ID);

        AlarmChecker<Long> checker = getCheckerStub(rule, 1000L);

        List<AlarmChecker<?>> checkers = List.of(checker);

        mockingAlarmService(getBeforeCheckerStub(0, 1));
        mockingAlarmMessageSender(checker);

        // when
        writer.write(List.of(new AppAlarmChecker(checkers)));

        // then
        verify(alarmMessageSender).sendSms(checker, 1);
        verify(alarmMessageSender).sendEmail(checker, 1);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void whenSequenceCountIsEqualToTimingCountDoNotSendAlarm() {
        //given
        Rule rule = getRuleStub(APPLICATION_ID, RULE_ID);

        AlarmChecker<Long> checker = getCheckerStub(rule, 1000L);

        List<AlarmChecker<?>> checkers = List.of(checker);

        mockingAlarmService(getBeforeCheckerStub(1, 1));
        mockingAlarmMessageSender(checker);

        // when
        writer.write(List.of(new AppAlarmChecker(checkers)));

        // then
        verify(alarmMessageSender, never()).sendSms(checker, 1);
        verify(alarmMessageSender, never()).sendEmail(checker, 1);
    }

    private void mockingAlarmService(CheckerResult beforeCheckerFixture) {
        beforeCheckerResults.put(RULE_ID, beforeCheckerFixture);
        when(alarmService.selectBeforeCheckerResults(APPLICATION_ID)).thenReturn(beforeCheckerResults);
    }

    private void mockingAlarmMessageSender(AlarmChecker<Long> checker) {
        doNothing().when(alarmMessageSender).sendSms(checker, 1);
        doNothing().when(alarmMessageSender).sendEmail(checker, 1);
    }

    private Rule getRuleStub(String appliationId, String ruleId) {
        Rule rule = new Rule(appliationId, "tomcat", CHECKER_NAME, 100, "testGroup", true, true, true, "");
        rule.setRuleId(ruleId);
        return rule;
    }

    private CheckerResult getBeforeCheckerStub(int sequenceCount, int timingCount) {
        return new CheckerResult(RULE_ID, APPLICATION_ID, CHECKER_NAME, true, sequenceCount, timingCount);
    }

    private AlarmChecker<Long> getCheckerStub(Rule rule, Long detectedValue) {
        return new SlowCountChecker(null, rule) {
            @Override
            public boolean isDetected() {
                return true;
            }

            @Override
            protected Long getDetectedValue() {
                return detectedValue;
            }
        };
    }

}
