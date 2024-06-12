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

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.BeforeAll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GcCountCheckerTest {


    private static final String SERVICE_NAME = "local_service";
    private static final String SERVICE_TYPE = "tomcat";

    private static ApplicationIndexDao applicationIndexDao;

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

                for (int i = 36; i > 0; i--) {
                    JvmGcBo jvmGc = new JvmGcBo();
                    jvmGc.setGcOldCount(i);
                    jvmGcs.add(jvmGc);
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

        applicationIndexDao = new ApplicationIndexDao() {

            @Override
            public List<Application> selectAllApplications() {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<Application> selectApplicationByName(String applicationName) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<String> selectAgentIds(String applicationName) {
                if (SERVICE_NAME.equals(applicationName)) {
                    return List.of("local_tomcat");
                }

                throw new IllegalArgumentException();
            }

            @Override
            public void deleteApplicationName(String applicationName) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void deleteAgentIds(Map<String, List<String>> applicationAgentIdMap) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void deleteAgentId(String applicationName, String agentId) {
                throw new UnsupportedOperationException();
            }

        };
    }


//    @Test
//    public void checkTest1() {
//        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.GC_COUNT.getName(), 35, "testGroup", false, false, "");
//        Application application = new Application(SERVICE_NAME, ServiceType.STAND_ALONE);
//        AgentStatDataCollector collector = new AgentStatDataCollector(DataCollectorCategory.AGENT_STAT, application, jvmGcDao, cpuLoadDao, applicationIndexDao, System.currentTimeMillis(), DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN);
//        AgentChecker checker = new GcCountChecker(collector, rule);
//        
//        checker.check();
//        assertTrue(checker.isDetected());
//    }
//    
//    @Test
//    public void checkTest2() {
//        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.GC_COUNT.getName(), 36, "testGroup", false, false, "");
//        Application application = new Application(SERVICE_NAME, ServiceType.STAND_ALONE);
//        AgentStatDataCollector collector = new AgentStatDataCollector(DataCollectorCategory.AGENT_STAT, application, jvmGcDao, cpuLoadDao, applicationIndexDao, System.currentTimeMillis(), DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN);
//        AgentChecker checker = new GcCountChecker(collector, rule);
//        
//        checker.check();
//        assertFalse(checker.isDetected());
//    }

}
