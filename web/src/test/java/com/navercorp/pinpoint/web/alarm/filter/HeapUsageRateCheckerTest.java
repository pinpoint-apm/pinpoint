package com.nhn.pinpoint.web.alarm.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentStatCpuLoadBo;
import com.nhn.pinpoint.common.bo.AgentStatMemoryGcBo;
import com.nhn.pinpoint.common.bo.AgentStatMemoryGcBo.Builder;
import com.nhn.pinpoint.web.alarm.CheckerCategory;
import com.nhn.pinpoint.web.alarm.DataCollectorFactory;
import com.nhn.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;
import com.nhn.pinpoint.web.alarm.checker.AgentChecker;
import com.nhn.pinpoint.web.alarm.checker.HeapUsageRateChecker;
import com.nhn.pinpoint.web.alarm.collector.AgentStatDataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;
import com.nhn.pinpoint.web.dao.AgentStatDao;
import com.nhn.pinpoint.web.dao.ApplicationIndexDao;
import com.nhn.pinpoint.web.vo.AgentStat;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration("classpath:applicationContext-test.xml")
public class HeapUsageRateCheckerTest {

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
                    AgentStatMemoryGcBo.Builder memoryBuilder = new AgentStatMemoryGcBo.Builder("AGETNT_NAME", 0L, 1L);
                    memoryBuilder.jvmMemoryHeapUsed(70L);
                    memoryBuilder.jvmMemoryHeapMax(100L);
                    AgentStatMemoryGcBo memoryBo = memoryBuilder.build();
                    AgentStatCpuLoadBo.Builder cpuBuilder = new AgentStatCpuLoadBo.Builder("AGETNT_NAME", 0L, 1L);
                    AgentStatCpuLoadBo cpuLoadBo = cpuBuilder.build();
                    
                    AgentStat stat = new AgentStat();
                    stat.setMemoryGc(memoryBo);
                    stat.setCpuLoad(cpuLoadBo);
                    
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
        Rule rule = new Rule(SERVICE_NAME, CheckerCategory.HEAP_USAGE_RATE.getName(), 70, "testGroup", false, false);
        Application application = new Application(SERVICE_NAME, ServiceType.TOMCAT);
        AgentStatDataCollector collector = new AgentStatDataCollector(DataCollectorCategory.AGENT_STAT, application, agentStatDao, applicationIndexDao, System.currentTimeMillis(), DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN);
        AgentChecker checker = new HeapUsageRateChecker(collector, rule);
        
        checker.check();
        assertTrue(checker.isDetected());
    }
    
    @Test
    public void checkTest2() {
        Rule rule = new Rule(SERVICE_NAME, CheckerCategory.HEAP_USAGE_RATE.getName(), 71, "testGroup", false, false);
        Application application = new Application(SERVICE_NAME, ServiceType.TOMCAT);
        AgentStatDataCollector collector = new AgentStatDataCollector(DataCollectorCategory.AGENT_STAT, application, agentStatDao, applicationIndexDao, System.currentTimeMillis(), DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN);
        AgentChecker checker = new HeapUsageRateChecker(collector, rule);
        
        checker.check();
        assertFalse(checker.isDetected());
    }

    
//    @Autowired
//    private HbaseAgentStatDao hbaseAgentStatDao ;
    
//    @Autowired
//    private HbaseApplicationIndexDao applicationIndexDao;
    
//    @Test
//    public void checkTest1() {
//        Rule rule = new Rule(SERVICE_NAME, CheckerCategory.HEAP_USAGE_RATE.getName(), 60, "testGroup", false, false);
//        Application application = new Application(SERVICE_NAME, ServiceType.TOMCAT);
//        AgentStatDataCollector collector = new AgentStatDataCollector(DataCollectorCategory.AGENT_STAT, application, hbaseAgentStatDao, applicationIndexDao, System.currentTimeMillis(), (long)300000);
//        AgentChecker checker = new HeapUsageRateChecker(collector, rule);
//        
//        checker.check();
//        assertTrue(checker.isDetected());
//    }

}
