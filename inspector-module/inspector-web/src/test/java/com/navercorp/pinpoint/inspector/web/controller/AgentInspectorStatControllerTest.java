package com.navercorp.pinpoint.inspector.web.controller;

import com.navercorp.pinpoint.common.timeseries.time.Timestamp;
import com.navercorp.pinpoint.inspector.web.config.InspectorWebProperties;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricGroupData;
import com.navercorp.pinpoint.inspector.web.service.AgentStatService;
import com.navercorp.pinpoint.inspector.web.service.ApdexStatService;
import com.navercorp.pinpoint.inspector.web.view.InspectorMetricGroupDataView;
import com.navercorp.pinpoint.inspector.web.view.InspectorMetricView;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import com.navercorp.pinpoint.service.web.vo.ServiceName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentInspectorStatControllerTest {

    @Mock
    private AgentStatService agentStatService;
    @Mock
    private ApdexStatService apdexStatService;
    @Mock
    private TenantProvider tenantProvider;
    @Mock
    private InspectorWebProperties inspectorWebProperties;

    private AgentInspectorStatController controller;

    private static final Timestamp FROM = Timestamp.ofEpochMilli(0);
    private static final Timestamp TO = Timestamp.ofEpochMilli(300000);

    @BeforeEach
    void setUp() {
        when(inspectorWebProperties.getInspectorPeriodMax()).thenReturn(42);
        controller = new AgentInspectorStatController(agentStatService, apdexStatService, tenantProvider, inspectorWebProperties);
    }

    @Test
    void getAgentStatChart() {
        when(tenantProvider.getTenantId()).thenReturn("pinpoint");
        InspectorMetricData metricData = new InspectorMetricData("cpu", Collections.emptyList(), Collections.emptyList());
        when(agentStatService.selectAgentStat(any(), any())).thenReturn(metricData);

        InspectorMetricView result = controller.getAgentStatChart(new ServiceName("DEFAULT"), "testApp", "agent-01", "cpu", FROM, TO);

        assertThat(result.getTitle()).isEqualTo("cpu");
        verify(agentStatService).selectAgentStat(any(), any());
    }

    @Test
    void getAgentStatChart_withNonDefaultService() {
        when(tenantProvider.getTenantId()).thenReturn("pinpoint");
        InspectorMetricData metricData = new InspectorMetricData("cpu", Collections.emptyList(), Collections.emptyList());
        when(agentStatService.selectAgentStat(any(), any())).thenReturn(metricData);

        InspectorMetricView result = controller.getAgentStatChart(new ServiceName("my-service"), "testApp", "agent-01", "cpu", FROM, TO);

        assertThat(result.getTitle()).isEqualTo("cpu");
    }

    @Test
    void getAgentStatChartList() {
        when(tenantProvider.getTenantId()).thenReturn("pinpoint");
        InspectorMetricGroupData groupData = new InspectorMetricGroupData("cpu", Collections.emptyList(), Collections.emptyMap());
        when(agentStatService.selectAgentStatWithGrouping(any(), any())).thenReturn(groupData);

        InspectorMetricGroupDataView result = controller.getAgentStatChartList(new ServiceName("DEFAULT"), "testApp", "agent-01", "cpu", FROM, TO);

        assertThat(result.getTitle()).isEqualTo("cpu");
        verify(agentStatService).selectAgentStatWithGrouping(any(), any());
    }

    @Test
    void getAgentStatChartGroupedByAgentId() {
        when(tenantProvider.getTenantId()).thenReturn("pinpoint");
        InspectorMetricGroupData groupData = new InspectorMetricGroupData("cpu", Collections.emptyList(), Collections.emptyMap());
        when(agentStatService.selectAgentStatGroupedByAgentId(any(), any(), any(), any(), any())).thenReturn(groupData);

        InspectorMetricGroupDataView result = controller.getAgentStatChartGroupedByAgentId(
                new ServiceName("DEFAULT"), "testApp", List.of("agent-01", "agent-02"), "cpu", FROM, TO);

        assertThat(result.getTitle()).isEqualTo("cpu");
        verify(agentStatService).selectAgentStatGroupedByAgentId(any(), any(), any(), any(), any());
    }

    @Test
    void getAgentStatChartListGroupedByAgentId() {
        when(tenantProvider.getTenantId()).thenReturn("pinpoint");
        InspectorMetricGroupData groupData = new InspectorMetricGroupData("cpu", Collections.emptyList(), Collections.emptyMap());
        when(agentStatService.selectAgentStatGroupedByAgentId(any(), any(), any(), any(), any())).thenReturn(groupData);

        InspectorMetricGroupDataView result = controller.getAgentStatChartListGroupedByAgentId(
                new ServiceName("DEFAULT"), "testApp", List.of("agent-01"), "cpu", FROM, TO);

        assertThat(result.getTitle()).isEqualTo("cpu");
    }
}
