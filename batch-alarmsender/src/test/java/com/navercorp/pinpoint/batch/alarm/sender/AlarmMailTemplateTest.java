/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.batch.alarm.sender;

import com.navercorp.pinpoint.batch.alarm.checker.AlarmCheckerInterface;
import com.navercorp.pinpoint.web.vo.RuleInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author minwoo-jung
 */
//@Disabled
@ExtendWith({MockitoExtension.class})
class AlarmMailTemplateTest {

    @Mock
    AlarmCheckerInterface checker;

    @Mock
    RuleInterface rule;

    @Test
    public void newBodyTest() {
        when(rule.getCheckerName()).thenReturn("checkerName");
        when(rule.getApplicationId()).thenReturn("applicationId");
        when(rule.getServiceType()).thenReturn("TOMCAT");
        when(checker.getRule()).thenReturn(rule);
        when(checker.getEmailMessage(anyString(), anyString(), anyString(), anyString())).thenReturn("ERROR RATE is 25 % (Threshold: 10 %)<br>");

        AlarmMailTemplate alarmMailTemplate = new AlarmMailTemplate(checker, "http://pinpoint.com", "batchEnv", 1);
        alarmMailTemplate.createBody();
    }
}