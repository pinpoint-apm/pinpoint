/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.web.alarm.checker;

import com.navercorp.pinpoint.web.alarm.collector.AgentStatDataCollector;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
public class SystemCpuUsageRateCheckerTest {


    @Test
    public void checkTest() {
        Rule rule = new Rule();
        rule.setThreshold(10);
        AgentStatDataCollector agentStatDataCollector = mock(AgentStatDataCollector.class);
        Map<String, Long> result = new HashMap<>();
        result.put("testAgent1", 30L);
        result.put("testAgent2", 50L);
        when(agentStatDataCollector.getSystemCpuUsageRate()).thenReturn(result);
        SystemCpuUsageRateChecker systemCpuUsageRateChecker = new SystemCpuUsageRateChecker(agentStatDataCollector, rule);
        systemCpuUsageRateChecker.check();
        assertTrue(systemCpuUsageRateChecker.isDetected());
    }

    @Test
    public void check2Test() {
        Rule rule = new Rule();
        rule.setThreshold(70);
        AgentStatDataCollector agentStatDataCollector = mock(AgentStatDataCollector.class);
        Map<String, Long> result = new HashMap<>();
        result.put("testAgent1", 30L);
        result.put("testAgent2", 50L);
        when(agentStatDataCollector.getSystemCpuUsageRate()).thenReturn(result);
        SystemCpuUsageRateChecker systemCpuUsageRateChecker = new SystemCpuUsageRateChecker(agentStatDataCollector, rule);
        systemCpuUsageRateChecker.check();
        assertFalse(systemCpuUsageRateChecker.isDetected());
    }
}