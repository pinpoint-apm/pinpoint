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

package com.navercorp.pinpoint.web.alarm;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.alarm.checker.SlowCountChecker;
import com.navercorp.pinpoint.web.alarm.vo.CheckerResult;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.service.AlarmService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
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
    
    @Before
    public void setUp() throws Exception {
        writer = new AlarmWriter(alarmMessageSender, alarmService, Optional.empty());

        beforeCheckerResults = new HashMap<>();
    }
    
    @Test
    public void whenSequenceCountIsLessThanTimingCountDoSendAlarm() throws Exception {
        // given
        Rule rule = getRuleStub(APPLICATION_ID, RULE_ID);
        
        AlarmChecker<Long> checker = getCheckerStub(rule, 1000L);
    
        List<AlarmChecker> checkers = new LinkedList<AlarmChecker>();
        checkers.add(checker);
    
        mockingAlarmService(getBeforeCheckerStub(0, 1));
        mockingAlarmMessageSender(checker);
        
        // when
        writer.write(checkers);
        
        // then
        verify(alarmMessageSender, times(1)).sendSms(checker, 1, null);
        verify(alarmMessageSender, times(1)).sendEmail(checker, 1, null);
    }
    
    @Test
    public void whenSequenceCountIsEqualToTimingCountDoNotSendAlarm() throws Exception {
        //given
        Rule rule = getRuleStub(APPLICATION_ID, RULE_ID);
        
        AlarmChecker<Long> checker = getCheckerStub(rule, 1000L);
    
        List<AlarmChecker> checkers = new LinkedList<AlarmChecker>();
        checkers.add(checker);
    
        mockingAlarmService(getBeforeCheckerStub(1, 1));
        mockingAlarmMessageSender(checker);
        
        // when
        writer.write(checkers);
        
        // then
        verify(alarmMessageSender, times(0)).sendSms(checker, 1, null);
        verify(alarmMessageSender, times(0)).sendEmail(checker, 1, null);
    }
    
    private void mockingAlarmService(CheckerResult beforeCheckerFixture) {
        beforeCheckerResults.put(RULE_ID, beforeCheckerFixture);
        when(alarmService.selectBeforeCheckerResults(APPLICATION_ID)).thenReturn(beforeCheckerResults);
    }
    
    private void mockingAlarmMessageSender(AlarmChecker<Long> checker) {
        doNothing().when(alarmMessageSender).sendSms(checker, 1, null);
        doNothing().when(alarmMessageSender).sendEmail(checker, 1, null);
    }
    
    private Rule getRuleStub(String appliationId, String ruleId) {
        Rule rule = new Rule(appliationId, "tomcat", CHECKER_NAME, 100, "testGroup", true, true, "");
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
