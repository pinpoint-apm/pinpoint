/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.controller;

import com.navercorp.pinpoint.common.server.util.time.ForwardRangeValidator;
import com.navercorp.pinpoint.common.server.util.time.RangeValidator;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSampler;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.config.SystemMetricProperties;
import com.navercorp.pinpoint.metric.web.model.MetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.model.MetricInfo;
import com.navercorp.pinpoint.metric.web.model.SystemMetricData;
import com.navercorp.pinpoint.metric.web.service.SystemMetricDataService;
import com.navercorp.pinpoint.metric.web.service.SystemMetricHostInfoService;
import com.navercorp.pinpoint.metric.web.service.YMLSystemMetricBasicGroupManager;
import com.navercorp.pinpoint.metric.common.util.TagUtils;
import com.navercorp.pinpoint.metric.web.view.SystemMetricView;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@RestController
@RequestMapping(value = "/api/systemMetric")
public class SystemMetricController {
    private final SystemMetricDataService systemMetricDataService;
    private final SystemMetricHostInfoService systemMetricHostInfoService;
    private final YMLSystemMetricBasicGroupManager systemMetricBasicGroupManager;
    private final TenantProvider tenantProvider;
    private final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(10000L, 200);
    private final RangeValidator rangeValidator;
    
    public SystemMetricController(SystemMetricDataService systemMetricDataService,
                                  SystemMetricHostInfoService systemMetricHostInfoService,
                                  YMLSystemMetricBasicGroupManager systemMetricBasicGroupManager,
                                  TenantProvider tenantProvider,
                                  SystemMetricProperties systemMetricProperties
                                  ) {
        this.systemMetricDataService = Objects.requireNonNull(systemMetricDataService, "systemMetricService");
        this.systemMetricHostInfoService = Objects.requireNonNull(systemMetricHostInfoService, "systemMetricHostInfoService");
        this.systemMetricBasicGroupManager = Objects.requireNonNull(systemMetricBasicGroupManager, "systemMetricBasicGroupManager");
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
        Objects.requireNonNull(systemMetricProperties, "systemMetricProperties");
        this.rangeValidator = new ForwardRangeValidator(Duration.ofDays(systemMetricProperties.getSystemMetricPeriodMax()));
    }

    @GetMapping(value = "/hostGroup")
    public List<String> getHostGroup() {
        String tenantId = tenantProvider.getTenantId();
        return systemMetricHostInfoService.getHostGroupNameList(tenantId);
    }

    @GetMapping(value = "/hostGroup/host")
    public List<String> getHostGroup(@RequestParam("hostGroupName") String hostGroupName) {
        String tenantId = tenantProvider.getTenantId();
        return systemMetricHostInfoService.getHostList(tenantId, hostGroupName);
    }

    @GetMapping(value = "/hostGroup/host/collectedMetricInfoV2")
    public List<MetricInfo> getCollectedMetricInfoV2(@RequestParam("hostGroupName") String hostGroupName, @RequestParam("hostName") String hostName) {
        String tenantId = tenantProvider.getTenantId();
        return systemMetricHostInfoService.getCollectedMetricInfoV2(tenantId, hostGroupName, hostName);
    }

    @GetMapping(value = "/hostGroup/host/collectedTags")
    public List<String> getCollectedTags(@RequestParam("hostGroupName") String hostGroupName,
                                         @RequestParam("hostName") String hostName,
                                         @RequestParam("metricDefinitionId") String metricDefinitionId) {
        String tenantId = tenantProvider.getTenantId();
        return systemMetricHostInfoService.getCollectedMetricInfoTags(tenantId, hostGroupName, hostName, metricDefinitionId);
    }

    @GetMapping(value = "/hostGroup/host/collectedMetricData")
    public SystemMetricView getCollectedMetricData(@RequestParam("hostGroupName") String hostGroupName,
                                                   @RequestParam("hostName") String hostName,
                                                   @RequestParam("metricDefinitionId") String metricDefinitionId,
                                                   @RequestParam("from") long from,
                                                   @RequestParam("to") long to,
                                                   @RequestParam(value = "tags", required = false) String tags) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range.getFromInstant(), range.getToInstant());

        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = new TimeWindow(Range.between(from, to), DEFAULT_TIME_WINDOW_SAMPLER);
        MetricDataSearchKey metricDataSearchKey = new MetricDataSearchKey(tenantId, hostGroupName, hostName, systemMetricBasicGroupManager.findMetricName(metricDefinitionId), metricDefinitionId, timeWindow);
        List<Tag> tagList = TagUtils.parseTags(tags);

        SystemMetricData<? extends Number> systemMetricData = systemMetricDataService.getCollectedMetricData(metricDataSearchKey, timeWindow, tagList);
        return new SystemMetricView(systemMetricData);
    }
}
