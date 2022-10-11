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

package com.navercorp.pinpoint.batch.alarm.checker;

import com.navercorp.pinpoint.batch.alarm.DataCollectorFactory;
import com.navercorp.pinpoint.batch.alarm.collector.AgentStatDataCollector;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HeapUsageRateCheckerTest {

    private static final String SERVICE_NAME = "local_service";
    private static final String SERVICE_TYPE = "tomcat";
    private static final List<String> mockAgentIds = List.of("local_tomcat");

    private static AgentStatDao<JvmGcBo> jvmGcDao;

    private static AgentStatDao<CpuLoadBo> cpuLoadDao;

    @BeforeAll
    public static void before() {
        jvmGcDao = new AgentStatDao<>() {

            @Override
            public String getChartType() {
                return AgentStatType.JVM_GC.getChartType();
            }

            @Override
            public List<JvmGcBo> getAgentStatList(String agentId, Range range) {
                List<JvmGcBo> jvmGcs = new ArrayList<>();

                for (int i = 0; i < 36; i++) {
                    JvmGcBo jvmGcBo = new JvmGcBo();
                    jvmGcBo.setHeapUsed(70L);
                    jvmGcBo.setHeapMax(100L);

                    jvmGcs.add(jvmGcBo);
                }

                return jvmGcs;
            }

            @Override
            public boolean agentStatExists(String agentId, Range range) {
                return true;
            }
        };

        cpuLoadDao = new AgentStatDao<>() {
            @Override
            public String getChartType() {
                return AgentStatType.CPU_LOAD.getChartType();
            }

            @Override
            public List<CpuLoadBo> getAgentStatList(String agentId, Range range) {
                return Collections.emptyList();
            }

            @Override
            public boolean agentStatExists(String agentId, Range range) {
                return false;
            }
        };
    }


    @Test
    public void checkTest1() {
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.HEAP_USAGE_RATE.getName(), 70, "testGroup", false, false, false, "");
        AgentStatDataCollector collector = new AgentStatDataCollector(DataCollectorCategory.AGENT_STAT, jvmGcDao, cpuLoadDao, mockAgentIds, System.currentTimeMillis(), DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN);
        AgentChecker<Long> checker = new HeapUsageRateChecker(collector, rule);

        checker.check();
        assertTrue(checker.isDetected());
    }

    @Test
    public void checkTest2() {
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.HEAP_USAGE_RATE.getName(), 71, "testGroup", false, false, false, "");
        AgentStatDataCollector collector = new AgentStatDataCollector(DataCollectorCategory.AGENT_STAT, jvmGcDao, cpuLoadDao, mockAgentIds, System.currentTimeMillis(), DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN);
        AgentChecker<Long> checker = new HeapUsageRateChecker(collector, rule);

        checker.check();
        assertFalse(checker.isDetected());
    }

}
