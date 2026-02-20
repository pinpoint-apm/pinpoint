package com.navercorp.pinpoint.inspector.web.controller;

import com.navercorp.pinpoint.common.timeseries.time.ForwardRangeValidator;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.time.RangeValidator;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowSampler;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.inspector.web.config.InspectorWebProperties;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricGroupData;
import com.navercorp.pinpoint.inspector.web.service.ApdexStatService;
import com.navercorp.pinpoint.inspector.web.service.ApplicationStatService;
import com.navercorp.pinpoint.inspector.web.view.InspectorMetricGroupDataView;
import com.navercorp.pinpoint.inspector.web.view.InspectorMetricView;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import com.navercorp.pinpoint.web.vo.Service;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Objects;

@RestController
@RequestMapping("/api/inspector/applicationStat")
public class ApplicationInspectorStatController {
    private final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER_30M = new TimeWindowSlotCentricSampler(30000L, 200);
    private final TenantProvider tenantProvider;
    private final ApplicationStatService applicationStatService;
    private final ApdexStatService apdexStatService;
    private final RangeValidator rangeValidator;

    public ApplicationInspectorStatController(ApplicationStatService applicationStatService, TenantProvider tenantProvider, ApdexStatService apdexStatService, InspectorWebProperties inspectorWebProperties) {
        this.applicationStatService = Objects.requireNonNull(applicationStatService, "applicationStatService");
        this.apdexStatService = Objects.requireNonNull(apdexStatService, "apdexStatService");
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
        Objects.requireNonNull(inspectorWebProperties, "inspectorWebProperties");
        this.rangeValidator = new ForwardRangeValidator(Duration.ofDays(inspectorWebProperties.getInspectorPeriodMax()));
    }

    @PreAuthorize("hasPermission(#applicationName, 'application', 'inspector')")
    @GetMapping(value = "/chart")
    public InspectorMetricView getApplicationStatChart(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range.getFromInstant(), range.getToInstant());

        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = getTimeWindow(range);
        InspectorDataSearchKey inspectorDataSearchKey = new InspectorDataSearchKey(tenantId, applicationName, InspectorDataSearchKey.UNKNOWN_NAME, metricDefinitionId, timeWindow);

        InspectorMetricData inspectorMetricData = applicationStatService.selectApplicationStat(inspectorDataSearchKey, timeWindow);
        return new InspectorMetricView(inspectorMetricData);
    }

    @PreAuthorize("hasPermission(#applicationName, 'application', 'inspector')")
    @GetMapping(value = "/chart", params = "metricDefinitionId=apdex")
    public InspectorMetricView getApdexStatChart(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range.getFromInstant(), range.getToInstant());

        InspectorMetricData inspectorMetricData = apdexStatService.selectApplicationStat(Service.DEFAULT, applicationName, serviceTypeName, metricDefinitionId, from, to);
        return new InspectorMetricView(inspectorMetricData);
    }

    @PreAuthorize("hasPermission(#applicationName, 'application', 'inspector')")
    @GetMapping(value = "/chartList")
    public InspectorMetricGroupDataView getApplicationStatChartList(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range.getFromInstant(), range.getToInstant());

        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = getTimeWindow(range);
        InspectorDataSearchKey inspectorDataSearchKey = new InspectorDataSearchKey(tenantId, applicationName, InspectorDataSearchKey.UNKNOWN_NAME, metricDefinitionId, timeWindow);

        InspectorMetricGroupData inspectorMetricGroupData = applicationStatService.selectApplicationStatWithGrouping(inspectorDataSearchKey, timeWindow);
        return new InspectorMetricGroupDataView(inspectorMetricGroupData);
    }

    private TimeWindow getTimeWindow(Range range) {
        return new TimeWindow(range, DEFAULT_TIME_WINDOW_SAMPLER_30M);
    }
}
