package com.navercorp.pinpoint.inspector.web.controller;


import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricGroupData;
import com.navercorp.pinpoint.inspector.web.service.ApdexStatService;
import com.navercorp.pinpoint.inspector.web.service.ApplicationStatService;
import com.navercorp.pinpoint.inspector.web.view.InspectorMetricGroupDataVeiw;
import com.navercorp.pinpoint.inspector.web.view.InspectorMetricView;
import com.navercorp.pinpoint.metric.common.model.Range;
import com.navercorp.pinpoint.metric.common.model.TimeWindow;
import com.navercorp.pinpoint.metric.common.util.TimeWindowSampler;
import com.navercorp.pinpoint.metric.common.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/getApplicationStatV2")
public class ApplicationStatV2Controller {

    private final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(5000L, 200);

    private final TenantProvider tenantProvider;
    private final ApplicationStatService applicationStatService;

    private final ApdexStatService apdexStatService;

public ApplicationStatV2Controller(ApplicationStatService applicationStatService, TenantProvider tenantProvider, ApdexStatService apdexStatService) {
        this.applicationStatService = Objects.requireNonNull(applicationStatService, "applicationStatService");
        this.apdexStatService = Objects.requireNonNull(apdexStatService, "apdexStatService");
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
    }

    @GetMapping(value = "/chart")
    public InspectorMetricView getApplicationStatChart(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), DEFAULT_TIME_WINDOW_SAMPLER);
        InspectorDataSearchKey inspectorDataSearchKey = new InspectorDataSearchKey(tenantId, applicationName, InspectorDataSearchKey.UNKNOWN_NAME, metricDefinitionId, timeWindow);

        InspectorMetricData inspectorMetricData =  applicationStatService.selectApplicationStat(inspectorDataSearchKey, timeWindow);
        return new InspectorMetricView(inspectorMetricData);
    }

    @GetMapping(value = "/chart", params = "metricDefinitionId=apdex")
    public InspectorMetricView getApdexStatChart(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        InspectorMetricData inspectorMetricData = apdexStatService.selectApplicationStat(applicationName, serviceTypeName, metricDefinitionId, from, to);
        return new InspectorMetricView(inspectorMetricData);
    }

    @GetMapping(value = "/chartList")
    public InspectorMetricGroupDataVeiw getApplicationStatChartList(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), DEFAULT_TIME_WINDOW_SAMPLER);
        InspectorDataSearchKey inspectorDataSearchKey = new InspectorDataSearchKey(tenantId, applicationName, InspectorDataSearchKey.UNKNOWN_NAME, metricDefinitionId, timeWindow);

        InspectorMetricGroupData inspectorMetricGroupData =  applicationStatService.selectApplicationStatWithGrouping(inspectorDataSearchKey, timeWindow);
        return new InspectorMetricGroupDataVeiw(inspectorMetricGroupData);
    }
}
