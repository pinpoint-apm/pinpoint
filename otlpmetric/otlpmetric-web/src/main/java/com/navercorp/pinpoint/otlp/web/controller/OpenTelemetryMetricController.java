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

import com.navercorp.pinpoint.otlp.web.service.OtlpMetricWebService;
import com.navercorp.pinpoint.otlp.web.view.OtlpChartView;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/otlpMetric")
public class OpenTelemetryMetricController {
    private final OtlpMetricWebService otlpMetricWebService;
    private static final String DEFAULT_SERVICE_ID = "00000000-0000-0000-0000-000000000001";
    @NotBlank
    private final String tenantId;

    public OpenTelemetryMetricController(OtlpMetricWebService otlpMetricWebService, TenantProvider tenantProvider) {
        this.otlpMetricWebService = Objects.requireNonNull(otlpMetricWebService, "otlpMetricWebService");
        Objects.requireNonNull(tenantProvider, "tenantProvider");
        tenantId = tenantProvider.getTenantId();
    }

    @GetMapping("/metricGroups")
    public List<String> getMetricGroups(@RequestParam("applicationId") @NotBlank String applicationId,
                                        @RequestParam(value = "agentId", required = false) String agentId) {
        return otlpMetricWebService.getMetricGroupList(tenantId, DEFAULT_SERVICE_ID, applicationId, agentId);
    }

    @GetMapping("/metrics")
    public List<String> getMetricGroups(@RequestParam("applicationId") @NotBlank String applicationId,
                                        @RequestParam(value = "agentId", required = false) String agentId,
                                        @RequestParam("metricGroupName") @NotBlank String metricGroupName) {
        return otlpMetricWebService.getMetricList(tenantId, DEFAULT_SERVICE_ID, applicationId, agentId, metricGroupName);
    }

    @GetMapping("/tags")
    public List<String> getTags(@RequestParam("applicationId") @NotBlank String applicationId,
                                @RequestParam(value = "agentId", required = false) String agentId,
                                @RequestParam("metricGroupName") @NotBlank String metricGroupName,
                                @RequestParam("metricName") @NotBlank String metricName) {
        return otlpMetricWebService.getTags(tenantId, DEFAULT_SERVICE_ID, applicationId, agentId, metricGroupName, metricName);
    }

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
}