/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.otlp.web.controller;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.time.RangeValidator;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSampler;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.otlp.common.web.definition.property.AggregationFunction;
import com.navercorp.pinpoint.otlp.common.web.definition.property.ChartType;
import com.navercorp.pinpoint.otlp.web.service.OtlpMetricWebService;
import com.navercorp.pinpoint.otlp.web.view.MetricDataView;
import com.navercorp.pinpoint.otlp.web.view.legacy.OtlpChartView;
import com.navercorp.pinpoint.otlp.web.vo.MetricData;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/otlp")
public class OpenTelemetryMetricController {
    private final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER_30M = new TimeWindowSlotCentricSampler(30000L, 200);
    private final OtlpMetricWebService otlpMetricWebService;
    private static final String DEFAULT_SERVICE_ID = "00000000-0000-0000-0000-000000000001";
    @NotBlank
    private final String tenantId;
    private final RangeValidator rangeValidator;

    public OpenTelemetryMetricController(OtlpMetricWebService otlpMetricWebService, TenantProvider tenantProvider, @Qualifier("rangeValidator14d") RangeValidator rangeValidator) {
        this.otlpMetricWebService = Objects.requireNonNull(otlpMetricWebService, "otlpMetricWebService");
        this.rangeValidator = Objects.requireNonNull(rangeValidator, "rangeValidator");
        Objects.requireNonNull(tenantProvider, "tenantProvider");
        tenantId = tenantProvider.getTenantId();

    }


    @Deprecated
    @GetMapping("/metricGroups")
    public List<String> getMetricGroups(@RequestParam("applicationId") @NotBlank String applicationId,
                                        @RequestParam(value = "agentId", required = false) String agentId) {
        return otlpMetricWebService.getMetricGroupList(tenantId, DEFAULT_SERVICE_ID, applicationId, agentId);
    }

    @Deprecated
    @GetMapping("/metrics")
    public List<String> getMetricGroups(@RequestParam("applicationId") @NotBlank String applicationId,
                                        @RequestParam(value = "agentId", required = false) String agentId,
                                        @RequestParam("metricGroupName") @NotBlank String metricGroupName) {
        return otlpMetricWebService.getMetricList(tenantId, DEFAULT_SERVICE_ID, applicationId, agentId, metricGroupName);
    }

    @Deprecated
    @GetMapping("/tags")
    public List<String> getTags(@RequestParam("applicationId") @NotBlank String applicationId,
                                @RequestParam(value = "agentId", required = false) String agentId,
                                @RequestParam("metricGroupName") @NotBlank String metricGroupName,
                                @RequestParam("metricName") @NotBlank String metricName) {
        return otlpMetricWebService.getTags(tenantId, DEFAULT_SERVICE_ID, applicationId, agentId, metricGroupName, metricName);
    }

    @Deprecated
    @GetMapping("/chart")
    public OtlpChartView getMetricChartData(@RequestParam("applicationId") @NotBlank String applicationId,
                                       @RequestParam(value = "agentId", required = false) String agentId,
                                       @RequestParam("metricGroupName") @NotBlank String metricGroupName,
                                       @RequestParam("metricName") @NotBlank String metricName,
                                       @RequestParam("tag") String tag,
                                       @RequestParam("from") @PositiveOrZero long from,
                                       @RequestParam("to") @PositiveOrZero long to) {
        return otlpMetricWebService.getMetricChartData(tenantId, DEFAULT_SERVICE_ID, applicationId, agentId, metricGroupName, metricName, tag, from, to);
    }

    @GetMapping("/metricData")
    public MetricDataView getMetricChartDataV2(@RequestParam("applicationName") @NotBlank String applicationName,
                                               @RequestParam(value = "agentId", required = false) String agentId,
                                               @RequestParam("metricGroupName") @NotBlank String metricGroupName,
                                               @RequestParam("metricName") @NotBlank String metricName,
                                               @RequestParam("tags") String tags,
                                               @RequestParam("fieldNameList") List<String> fieldNameList,
                                               @RequestParam("from") @PositiveOrZero long from,
                                               @RequestParam("to") @PositiveOrZero long to,
                                               @RequestParam("chartType") String chartTypeName,
                                               @RequestParam("aggregationFunction") String aggregationFunctionName) {
        ChartType chartType = ChartType.fromChartName(chartTypeName);
        AggregationFunction aggregationFunction = AggregationFunction.fromAggregationFunctionName(aggregationFunctionName);

        Range range = Range.between(from, to);
        rangeValidator.validate(range.getFromInstant(), range.getToInstant());
        TimeWindow timeWindow = new TimeWindow(range, DEFAULT_TIME_WINDOW_SAMPLER_30M);

        //TODO : (minwoo) remove tenantId, serviceId
        MetricData metricData = otlpMetricWebService.getMetricData(tenantId, DEFAULT_SERVICE_ID, applicationName, agentId, metricGroupName, metricName, tags, fieldNameList, chartType, aggregationFunction, timeWindow);
        return new MetricDataView(metricData);
    }
}