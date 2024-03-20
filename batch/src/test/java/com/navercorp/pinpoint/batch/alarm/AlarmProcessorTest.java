package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.collector.AgentStatDataCollector;
import com.navercorp.pinpoint.batch.alarm.vo.AppAlarmChecker;
import com.navercorp.pinpoint.batch.configuration.AlarmCheckerConfiguration;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.AlarmService;
import com.navercorp.pinpoint.web.service.ApplicationService;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(
        classes = AlarmCheckerConfiguration.class
)
public class AlarmProcessorTest {

    @Mock
    private DataCollectorFactory dataCollectorFactory;

    @Mock
    private AlarmService alarmService;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private AgentInfoService agentInfoService;

    @Mock
    private AgentStatDataCollector agentStatDataCollector;

    @Autowired
    CheckerRegistry checkerRegistry;

    private static final String SERVICE_NAME = "local_tomcat";

    private static final List<String> agentIds = List.of("agent0", "agent1", "agent2");

    @Test
    public void shouldSkipIfNoRule() {
        Application app = new Application(SERVICE_NAME, ServiceType.STAND_ALONE);

        when(alarmService.selectRuleByApplicationId(SERVICE_NAME)).thenReturn(List.of());

        AlarmProcessor proc = new AlarmProcessor(dataCollectorFactory, alarmService, applicationService, agentInfoService, checkerRegistry);
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
        when(dataCollectorFactory.createDataCollector(any(), any(), any(), anyLong())).thenReturn(agentStatDataCollector);
        when(agentStatDataCollector.getHeapUsageRate()).thenReturn(heapUsageRate);

        // Executions
        AlarmProcessor processor = new AlarmProcessor(dataCollectorFactory, alarmService, applicationService, agentInfoService, checkerRegistry);
        AppAlarmChecker appChecker = processor.process(application);

        // Validations
        verify(alarmService).selectRuleByApplicationId(SERVICE_NAME);
        verify(dataCollectorFactory).createDataCollector(any(), any(), any(), anyLong());

        assertNotNull(appChecker, "processed object is null");
        assertThat(appChecker.getChildren())
                .as("rules should be propagated").hasSize(2);
        assertThat(appChecker.getChildren().get(0).isDetected()).isTrue();
        assertThat(appChecker.getChildren().get(1).isDetected()).isFalse();
    }

}
