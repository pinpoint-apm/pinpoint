package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.collector.AgentStatDataCollector;
import com.navercorp.pinpoint.batch.alarm.vo.AppAlarmChecker;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.AlarmService;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AlarmProcessorTest {


    @Mock
    private DataCollectorFactory dataCollectorFactory;

    @Mock
    private AlarmService alarmService;

    @Mock
    private ApplicationIndexDao applicationIndexDao;

    @Mock
    private AgentInfoService agentInfoService;

    @Mock
    private AgentStatDataCollector agentStatDataCollector;

    private static final String SERVICE_NAME = "local_tomcat";

    private static final List<String> agentIds = List.of("agent0", "agent1", "agent2");

    @Test
    public void shouldSkipIfNoRule() {
        Application app = new Application(SERVICE_NAME, ServiceType.STAND_ALONE);

        when(alarmService.selectRuleByApplicationId(SERVICE_NAME)).thenReturn(List.of());

        AlarmProcessor proc = new AlarmProcessor(dataCollectorFactory, alarmService, applicationIndexDao, agentInfoService);
        AppAlarmChecker checker = proc.process(app);

        assertNull(checker, "should be skipped");
    }

    @Test
    public void test() {
        // Assumptions
        Application application = new Application(SERVICE_NAME, ServiceType.STAND_ALONE);
        Rule rule1 = new Rule(SERVICE_NAME, ServiceType.STAND_ALONE.getName(), CheckerCategory.HEAP_USAGE_RATE.getName(), 70, "testGroup", false, false, false, "");
        Rule rule2 = new Rule(SERVICE_NAME, ServiceType.STAND_ALONE.getName(), CheckerCategory.HEAP_USAGE_RATE.getName(), 90, "testGroup", false, false, false, "");
        Map<String, Long> heapUsageRate = Map.of(agentIds.get(1), 80L, agentIds.get(2), 85L);

        when(alarmService.selectRuleByApplicationId(SERVICE_NAME)).thenReturn(List.of(rule1, rule2));
        when(applicationIndexDao.selectAgentIds(SERVICE_NAME)).thenReturn(agentIds);
        when(agentInfoService.isActiveAgent(anyString(), any())).then(invocation -> {
           String agentId = invocation.getArgument(0, String.class);
           return !agentId.equals("agent0");
        });
        when(dataCollectorFactory.createDataCollector(any(), any(), any(), anyLong())).thenReturn(agentStatDataCollector);
        when(agentStatDataCollector.getHeapUsageRate()).thenReturn(heapUsageRate);

        // Executions
        AlarmProcessor processor = new AlarmProcessor(dataCollectorFactory, alarmService, applicationIndexDao, agentInfoService);
        AppAlarmChecker appChecker = processor.process(application);

        // Validations
        verify(alarmService).selectRuleByApplicationId(SERVICE_NAME);
        verify(applicationIndexDao).selectAgentIds(SERVICE_NAME);
        verify(agentInfoService, times(3)).isActiveAgent(anyString(), any());
        verify(dataCollectorFactory).createDataCollector(any(), any(), any(), anyLong());

        assertNotNull(appChecker, "processed object is null");
        assertThat(appChecker.getChildren())
                .as("rules should be propagated").hasSize(2);
        assertTrue(appChecker.getChildren().get(0).isDetected());
        assertFalse(appChecker.getChildren().get(1).isDetected());
    }

}
