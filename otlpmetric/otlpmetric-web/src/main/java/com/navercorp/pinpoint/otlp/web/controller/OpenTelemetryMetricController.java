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

import com.navercorp.pinpoint.common.server.util.time.ForwardRangeValidator;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.time.RangeValidator;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.otlp.common.web.defined.AppMetricDefinitionUtil;
import com.navercorp.pinpoint.otlp.common.web.defined.PrimaryForFieldAndTagRelation;
import com.navercorp.pinpoint.otlp.common.web.definition.property.AggregationFunction;
import com.navercorp.pinpoint.otlp.common.web.definition.property.ChartType;
import com.navercorp.pinpoint.otlp.web.config.OtlpMetricProperties;
import com.navercorp.pinpoint.otlp.web.service.OtlpMetricWebService;
import com.navercorp.pinpoint.otlp.web.view.MetricDataRequestParameter;
import com.navercorp.pinpoint.otlp.web.view.MetricDataView;
import com.navercorp.pinpoint.otlp.web.view.legacy.OtlpChartView;
import com.navercorp.pinpoint.otlp.web.vo.MetricData;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/api/otlp")
public class OpenTelemetryMetricController {
    private final OtlpMetricWebService otlpMetricWebService;
    private static final String DEFAULT_SERVICE_NAME = "";
    @NotBlank
    private final String tenantId;
    private final RangeValidator rangeValidator;

    public OpenTelemetryMetricController(OtlpMetricWebService otlpMetricWebService, TenantProvider tenantProvider, OtlpMetricProperties otlpMetricProperties) {
        this.otlpMetricWebService = Objects.requireNonNull(otlpMetricWebService, "otlpMetricWebService");
        Objects.requireNonNull(otlpMetricProperties, "otlpMetricProperties");
        this.rangeValidator = new ForwardRangeValidator(Duration.ofDays(otlpMetricProperties.getOtlpMetricPeriodMax()));
        Objects.requireNonNull(tenantProvider, "tenantProvider");
        tenantId = tenantProvider.getTenantId();
    }

    @Deprecated
    @GetMapping("/metricGroups")
    public List<String> getMetricGroups(@RequestParam("applicationName") @NotBlank String applicationName,
                                        @RequestParam(value = "agentId", required = false) String agentId) {
        return otlpMetricWebService.getMetricGroupList(tenantId, DEFAULT_SERVICE_NAME, applicationName, agentId);
    }

    @Deprecated
    @GetMapping("/metrics")
    public List<String> getMetricGroups(@RequestParam("applicationName") @NotBlank String applicationName,
                                        @RequestParam(value = "agentId", required = false) String agentId,
                                        @RequestParam("metricGroupName") @NotBlank String metricGroupName) {
        return otlpMetricWebService.getMetricList(tenantId, DEFAULT_SERVICE_NAME, applicationName, agentId, metricGroupName);
    }

    @Deprecated
    @GetMapping("/tags")
    public List<String> getTags(@RequestParam("applicationName") @NotBlank String applicationName,
                                @RequestParam(value = "agentId", required = false) String agentId,
                                @RequestParam("metricGroupName") @NotBlank String metricGroupName,
                                @RequestParam("metricName") @NotBlank String metricName) {
        return otlpMetricWebService.getTags(tenantId, DEFAULT_SERVICE_NAME, applicationName, agentId, metricGroupName, metricName);
    }

    @Deprecated
    @GetMapping("/chart")
    public OtlpChartView getMetricChartData(@RequestParam("applicationName") @NotBlank String applicationName,
                                       @RequestParam(value = "agentId", required = false) String agentId,
                                       @RequestParam("metricGroupName") @NotBlank String metricGroupName,
                                       @RequestParam("metricName") @NotBlank String metricName,
                                       @RequestParam("tag") String tag,
                                       @RequestParam("from") @PositiveOrZero long from,
                                       @RequestParam("to") @PositiveOrZero long to) {
        return otlpMetricWebService.getMetricChartData(tenantId, DEFAULT_SERVICE_NAME, applicationName, agentId, metricGroupName, metricName, tag, from, to);
    }

    @PostMapping("/metricData")
    public MetricDataView getMetricChartDataV3(@Valid @RequestBody MetricDataRequestParameter parameter) {
        List<String> tagGroupList = parameter.getTagGroupList();
        List<String> fieldNameList = parameter.getFieldNameList();
        AppMetricDefinitionUtil.validateCountOfTagAndField(tagGroupList, fieldNameList);

        PrimaryForFieldAndTagRelation primaryForFieldAndTagRelation = PrimaryForFieldAndTagRelation.fromName(parameter.getPrimaryForFieldAndTagRelation());
        AggregationFunction aggregationFunction = AggregationFunction.fromAggregationFunctionName(parameter.getAggregationFunction());
        ChartType chartType = ChartType.fromChartName(parameter.getChartType());

        Range range = Range.between(parameter.getFrom(), parameter.getTo());
        rangeValidator.validate(range.getFromInstant(), range.getToInstant());
        TimeWindowSlotCentricSampler timeSampler = new TimeWindowSlotCentricSampler(TimeUnit.SECONDS.toMillis(parameter.getSamplingInterval()), 200);
        TimeWindow timeWindow = new TimeWindow(range, timeSampler);

        MetricData metricData = otlpMetricWebService.getMetricData(tenantId, DEFAULT_SERVICE_NAME, parameter.getApplicationName(), parameter.getAgentId(), parameter.getMetricGroupName(), parameter.getMetricName(), primaryForFieldAndTagRelation, tagGroupList, fieldNameList, chartType, aggregationFunction, timeWindow);
        return new MetricDataView(metricData);
    }
}