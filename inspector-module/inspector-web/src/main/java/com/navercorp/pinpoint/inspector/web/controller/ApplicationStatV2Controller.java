package com.navercorp.pinpoint.inspector.web.controller;


import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricGroupData;
import com.navercorp.pinpoint.inspector.web.service.ApdexStatService;
import com.navercorp.pinpoint.inspector.web.service.ApplicationStatService;
import com.navercorp.pinpoint.inspector.web.view.InspectorMetricGroupDataView;
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
    private final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER_30M = new TimeWindowSlotCentricSampler(30000L, 200);

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
            @RequestParam("to") long to,
            @RequestParam(value="version", defaultValue="2") int version) {
        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = getTimeWindow(from, to, version);
        InspectorDataSearchKey inspectorDataSearchKey = new InspectorDataSearchKey(tenantId, applicationName, InspectorDataSearchKey.UNKNOWN_NAME, metricDefinitionId, timeWindow, version);

        InspectorMetricData inspectorMetricData =  applicationStatService.selectApplicationStat(inspectorDataSearchKey, timeWindow);
        return new InspectorMetricView(inspectorMetricData);
    }

    private TimeWindow getTimeWindow(long from, long to, int version) {
        if (version == 2) {
            return new TimeWindow(Range.newRange(from, to), DEFAULT_TIME_WINDOW_SAMPLER_30M);
        } else {
            return new TimeWindow(Range.newRange(from, to), DEFAULT_TIME_WINDOW_SAMPLER);
        }
    }

    @GetMapping(value = "/chart", params = "metricDefinitionId=apdex")
    public InspectorMetricView getApdexStatChart(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value="version", defaultValue="2") int version) {
        InspectorMetricData inspectorMetricData = apdexStatService.selectApplicationStat(applicationName, serviceTypeName, metricDefinitionId, from, to);
        return new InspectorMetricView(inspectorMetricData);
    }

    @GetMapping(value = "/chartList")
    public InspectorMetricGroupDataView getApplicationStatChartList(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value="version", defaultValue="2") int version) {
        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = getTimeWindow(from, to, version);
        InspectorDataSearchKey inspectorDataSearchKey = new InspectorDataSearchKey(tenantId, applicationName, InspectorDataSearchKey.UNKNOWN_NAME, metricDefinitionId, timeWindow, version);

        InspectorMetricGroupData inspectorMetricGroupData =  applicationStatService.selectApplicationStatWithGrouping(inspectorDataSearchKey, timeWindow);
        return new InspectorMetricGroupDataView(inspectorMetricGroupData);
    }
}
