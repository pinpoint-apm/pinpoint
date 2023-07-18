/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.batch.alarm.checker;

import com.navercorp.pinpoint.batch.alarm.collector.AgentEventDataCollector;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.AgentEventDao;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
@ExtendWith(MockitoExtension.class)
public class DeadlockCheckerTest {

    private static final String APPLICATION_NAME = "local_service";

    private static final String AGENT_ID_1 = "local_tomcat_1";
    private static final String AGENT_ID_2 = "local_tomcat_2";
    private static final String AGENT_ID_3 = "local_tomcat_3";
    private static final String SERVICE_TYPE = "tomcat";

    private static final long CURRENT_TIME_MILLIS = System.currentTimeMillis();
    private static final long INTERVAL_MILLIS = 300000;
    private static final long START_TIME_MILLIS = CURRENT_TIME_MILLIS - INTERVAL_MILLIS;
    private static final List<String> mockAgentIds = List.of(AGENT_ID_1, AGENT_ID_2, AGENT_ID_3);

    @Mock
    private AgentEventDao mockAgentEventDao;

    private long createEventTimestamp() {
        return RandomUtils.nextLong(START_TIME_MILLIS, CURRENT_TIME_MILLIS);
    }

    @Test
    public void checkTest1() {
        Rule rule = new Rule(APPLICATION_NAME, SERVICE_TYPE, CheckerCategory.ERROR_COUNT.getName(), 50, "testGroup", false, false, false, "");

        Range range = Range.newUncheckedRange(START_TIME_MILLIS, CURRENT_TIME_MILLIS);
        when(mockAgentEventDao.getAgentEvents(AGENT_ID_1, range, Collections.emptySet())).thenReturn(List.of(createAgentEvent(AGENT_ID_1, createEventTimestamp(), AgentEventType.AGENT_CLOSED_BY_SERVER)));
        when(mockAgentEventDao.getAgentEvents(AGENT_ID_2, range, Collections.emptySet())).thenReturn(List.of(createAgentEvent(AGENT_ID_2, createEventTimestamp(), AgentEventType.AGENT_DEADLOCK_DETECTED)));
        when(mockAgentEventDao.getAgentEvents(AGENT_ID_3, range, Collections.emptySet())).thenReturn(List.of(createAgentEvent(AGENT_ID_3, createEventTimestamp(), AgentEventType.AGENT_PING)));

        AgentEventDataCollector dataCollector = new AgentEventDataCollector(DataCollectorCategory.AGENT_EVENT, mockAgentEventDao, mockAgentIds, CURRENT_TIME_MILLIS, INTERVAL_MILLIS);
        DeadlockChecker checker = new DeadlockChecker(dataCollector, rule);
        checker.check();
        Assertions.assertTrue(checker.isDetected());

        String emailMessage = checker.getEmailMessage("pinpointUrl", "applicationId", "serviceType", "currentTime");
        Assertions.assertTrue(StringUtils.hasLength(emailMessage));

        List<String> smsMessage = checker.getSmsMessage();
        assertThat(smsMessage).hasSize(1);
    }

    @Test
    public void checkTest2() {
        Rule rule = new Rule(APPLICATION_NAME, SERVICE_TYPE, CheckerCategory.ERROR_COUNT.getName(), 50, "testGroup", false, false, false, "");

        Range range = Range.newUncheckedRange(START_TIME_MILLIS, CURRENT_TIME_MILLIS);
        when(mockAgentEventDao.getAgentEvents(AGENT_ID_1, range, Collections.emptySet())).thenReturn(List.of(createAgentEvent(AGENT_ID_1, createEventTimestamp(), AgentEventType.AGENT_CLOSED_BY_SERVER)));
        when(mockAgentEventDao.getAgentEvents(AGENT_ID_2, range, Collections.emptySet())).thenReturn(List.of(createAgentEvent(AGENT_ID_2, createEventTimestamp(), AgentEventType.AGENT_SHUTDOWN)));
        when(mockAgentEventDao.getAgentEvents(AGENT_ID_3, range, Collections.emptySet())).thenReturn(List.of(createAgentEvent(AGENT_ID_3, createEventTimestamp(), AgentEventType.AGENT_PING)));

        AgentEventDataCollector dataCollector = new AgentEventDataCollector(DataCollectorCategory.AGENT_EVENT, mockAgentEventDao, mockAgentIds, CURRENT_TIME_MILLIS, INTERVAL_MILLIS);
        DeadlockChecker checker = new DeadlockChecker(dataCollector, rule);
        checker.check();
        Assertions.assertFalse(checker.isDetected());

        String emailMessage = checker.getEmailMessage("pinpointUrl", "applicationId", "serviceType", "currentTime");
        assertThat(emailMessage).isNullOrEmpty();

        List<String> smsMessage = checker.getSmsMessage();
        assertThat(smsMessage).isEmpty();
    }

    private AgentEventBo createAgentEvent(String agentId, long eventTimestamp, AgentEventType agentEventType) {
        return new AgentEventBo(agentId, START_TIME_MILLIS, eventTimestamp, agentEventType);
    }

}
