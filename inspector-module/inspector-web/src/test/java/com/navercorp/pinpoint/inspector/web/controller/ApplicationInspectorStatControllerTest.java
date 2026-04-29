package com.navercorp.pinpoint.inspector.web.controller;

import com.navercorp.pinpoint.common.timeseries.time.Timestamp;
import com.navercorp.pinpoint.inspector.web.config.InspectorWebProperties;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricGroupData;
import com.navercorp.pinpoint.inspector.web.service.ApdexStatService;
import com.navercorp.pinpoint.inspector.web.service.ApplicationStatService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationInspectorStatControllerTest {

    @Mock
    private ApplicationStatService applicationStatService;
    @Mock
    private ApdexStatService apdexStatService;
    @Mock
    private TenantProvider tenantProvider;
    @Mock
    private InspectorWebProperties inspectorWebProperties;

    private ApplicationInspectorStatController controller;

    private static final Timestamp FROM = Timestamp.ofEpochMilli(0);
    private static final Timestamp TO = Timestamp.ofEpochMilli(300000);

    @BeforeEach
    void setUp() {
        when(inspectorWebProperties.getInspectorPeriodMax()).thenReturn(42);
        controller = new ApplicationInspectorStatController(applicationStatService, tenantProvider, apdexStatService, inspectorWebProperties);
    }

    @Test
    void getApplicationStatChart() {
        when(tenantProvider.getTenantId()).thenReturn("pinpoint");
        InspectorMetricData metricData = new InspectorMetricData("jvmCpu", Collections.emptyList(), Collections.emptyList());
        when(applicationStatService.selectApplicationStat(any(), any())).thenReturn(metricData);

        InspectorMetricView result = controller.getApplicationStatChart(new ServiceName("DEFAULT"), "testApp", "jvmCpu", FROM, TO);

        assertThat(result.getTitle()).isEqualTo("jvmCpu");
        verify(applicationStatService).selectApplicationStat(any(), any());
    }

    @Test
    void getApplicationStatChart_withNonDefaultService() {
        when(tenantProvider.getTenantId()).thenReturn("pinpoint");
        InspectorMetricData metricData = new InspectorMetricData("jvmCpu", Collections.emptyList(), Collections.emptyList());
        when(applicationStatService.selectApplicationStat(any(), any())).thenReturn(metricData);

        InspectorMetricView result = controller.getApplicationStatChart(new ServiceName("my-service"), "testApp", "jvmCpu", FROM, TO);

        assertThat(result.getTitle()).isEqualTo("jvmCpu");
    }

    @Test
    void getApplicationStatChartList() {
        when(tenantProvider.getTenantId()).thenReturn("pinpoint");
        InspectorMetricGroupData groupData = new InspectorMetricGroupData("jvmCpu", Collections.emptyList(), Collections.emptyMap());
        when(applicationStatService.selectApplicationStatWithGrouping(any(), any())).thenReturn(groupData);

        InspectorMetricGroupDataView result = controller.getApplicationStatChartList(new ServiceName("DEFAULT"), "testApp", "jvmCpu", FROM, TO);

        assertThat(result.getTitle()).isEqualTo("jvmCpu");
        verify(applicationStatService).selectApplicationStatWithGrouping(any(), any());
    }
}
