package com.navercorp.pinpoint.inspector.web.controller;


import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.inspector.web.service.ApplicationStatService;
import com.navercorp.pinpoint.inspector.web.view.InspectorMetricView;
import com.navercorp.pinpoint.metric.common.model.Range;
import com.navercorp.pinpoint.metric.common.model.TimeWindow;
import com.navercorp.pinpoint.metric.common.util.TimeWindowSampler;
import com.navercorp.pinpoint.metric.common.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
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

public ApplicationStatV2Controller(ApplicationStatService applicationStatService, TenantProvider tenantProvider) {
        this.applicationStatService = Objects.requireNonNull(applicationStatService, "applicationStatService");
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
}
