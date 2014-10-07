package com.nhn.pinpoint.web.alarm.filter;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentStatCpuLoadBo;
import com.nhn.pinpoint.common.bo.AgentStatMemoryGcBo;
import com.nhn.pinpoint.web.alarm.CheckerCategory;
import com.nhn.pinpoint.web.alarm.DataCollectorFactory;
import com.nhn.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;
import com.nhn.pinpoint.web.alarm.collector.AgentStatDataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;
import com.nhn.pinpoint.web.dao.AgentStatDao;
import com.nhn.pinpoint.web.dao.ApplicationIndexDao;
import com.nhn.pinpoint.web.vo.AgentStat;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;

public class JvmCpuUsageRateCheckerTest {

    private static final String SERVICE_NAME = "local_service";

    private static ApplicationIndexDao applicationIndexDao;
    
    private static AgentStatDao agentStatDao;
    
    @BeforeClass
    public static void before() {
        agentStatDao = new AgentStatDao() {

            @Override
            public List<AgentStat> scanAgentStatList(String agentId, Range range) {
                List<AgentStat> AgentStatList = new LinkedList<AgentStat>();
                
                for (int i = 0; i < 36; i++) {
                    AgentStatCpuLoadBo.Builder cpuLoadBoBuilder = new AgentStatCpuLoadBo.Builder("AGETNT_NAME", 0L, 1L);
                    cpuLoadBoBuilder.jvmCpuLoad(60);
                    AgentStatCpuLoadBo cpuLoadBo = cpuLoadBoBuilder.build();
                    
                    AgentStatMemoryGcBo.Builder memoryGcBobuilder = new AgentStatMemoryGcBo.Builder("AGETNT_NAME", 0L, 1L);
                    AgentStatMemoryGcBo memoryGcBo = memoryGcBobuilder.build();
                    
                    AgentStat stat = new AgentStat();
                    stat.setCpuLoad(cpuLoadBo);
                    stat.setMemoryGc(memoryGcBo);
                    
                    AgentStatList.add(stat);
                }
                
                return AgentStatList;
            }
        };
        
        applicationIndexDao = new ApplicationIndexDao() {

            @Override
            public List<Application> selectAllApplicationNames() {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<String> selectAgentIds(String applicationName) {
                if (SERVICE_NAME.equals(applicationName)) {
                    List<String> agentIds = new LinkedList<String>();
                    agentIds.add("local_tomcat");
                    return agentIds;
                }
                
                throw new IllegalArgumentException();
            }

            @Override
            public void deleteApplicationName(String applicationName) {
                throw new UnsupportedOperationException();
            }
            
        };
    }

    
    @Test
    public void checkTest1() {
        Rule rule = new Rule(SERVICE_NAME, CheckerCategory.JVM_CPU_USAGE_RATE.getName(), 60, "testGroup", false, false);
        Application application = new Application(SERVICE_NAME, ServiceType.TOMCAT);
        AgentStatDataCollector collector = new AgentStatDataCollector(DataCollectorCategory.AGENT_STAT, application, agentStatDao, applicationIndexDao, System.currentTimeMillis(), DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN);
        AgentChecker checker = new JvmCpuUsageRateChecker(collector, rule);
        
        checker.check();
        assertTrue(checker.isDetected());
    }
    
    @Test
    public void checkTest2() {
        Rule rule = new Rule(SERVICE_NAME, CheckerCategory.JVM_CPU_USAGE_RATE.getName(), 61, "testGroup", false, false);
        Application application = new Application(SERVICE_NAME, ServiceType.TOMCAT);
        AgentStatDataCollector collector = new AgentStatDataCollector(DataCollectorCategory.AGENT_STAT, application, agentStatDao, applicationIndexDao, System.currentTimeMillis(), DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN);
        AgentChecker checker = new JvmCpuUsageRateChecker(collector, rule);
        
        checker.check();
        assertFalse(checker.isDetected());
    }

}
