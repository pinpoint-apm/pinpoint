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

import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.model.MetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.model.MetricInfo;
import com.navercorp.pinpoint.metric.web.model.SystemMetricData;
import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricChart;
import com.navercorp.pinpoint.metric.web.service.SystemMetricDataService;
import com.navercorp.pinpoint.metric.web.service.SystemMetricHostInfoService;
import com.navercorp.pinpoint.metric.web.service.YMLSystemMetricBasicGroupManager;
import com.navercorp.pinpoint.metric.web.util.QueryParameter;
import com.navercorp.pinpoint.metric.web.util.Range;
import com.navercorp.pinpoint.metric.web.util.TagParser;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;
import com.navercorp.pinpoint.metric.web.util.TimeWindowSampler;
import com.navercorp.pinpoint.metric.web.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.metric.web.view.SystemMetricView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Hyunjoon Cho
 */
@RestController
@RequestMapping(value = "/systemMetric")
public class SystemMetricController {
    private final SystemMetricDataService systemMetricDataService;
    private final SystemMetricHostInfoService systemMetricHostInfoService;
    private final YMLSystemMetricBasicGroupManager systemMetricBasicGroupManager;

    private final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(10000L, 200);

    private final TagParser tagParser = new TagParser();

    public SystemMetricController(SystemMetricDataService systemMetricDataService, SystemMetricHostInfoService systemMetricHostInfoService, YMLSystemMetricBasicGroupManager systemMetricBasicGroupManager) {
        this.systemMetricDataService = Objects.requireNonNull(systemMetricDataService, "systemMetricService");
        this.systemMetricHostInfoService = Objects.requireNonNull(systemMetricHostInfoService, "systemMetricHostInfoService");
        this.systemMetricBasicGroupManager = Objects.requireNonNull(systemMetricBasicGroupManager, "systemMetricBasicGroupManager");
    }

    @Deprecated
    @GetMapping(value = "/list")
    public List<SystemMetric> getSystemMetricBoList(
            @RequestParam("hostGroupName") String hostGroupName,
            @RequestParam("hostName") String hostName,
            @RequestParam("metricName") String metricName,
            @RequestParam("fieldName") String fieldName,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {

        QueryParameter.Builder builder = new QueryParameter.Builder();
        builder.setHostGroupName(hostGroupName);
        builder.setHostName(hostName);
        builder.setMetricName(metricName);
        builder.setFieldName(fieldName);
        builder.setTagList(tagParser.parseTags(tags));
        builder.setRange(Range.newRange(from, to));
        QueryParameter queryParameter = builder.build();

        return systemMetricDataService.getSystemMetricBoList(queryParameter);
    }

    @Deprecated
    @GetMapping(value = "/chart")
    public SystemMetricChart getSystemMetricChart(
            @RequestParam("hostGroupName") String hostGroupName,
            @RequestParam("hostName") String hostName,
            @RequestParam("metricName") String metricName,
            @RequestParam("fieldName") String fieldName,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), DEFAULT_TIME_WINDOW_SAMPLER);

        QueryParameter.Builder builder = new QueryParameter.Builder();
        builder.setHostGroupName(hostGroupName);
        builder.setHostName(hostName);
        builder.setMetricName(metricName);
        builder.setFieldName(fieldName);
        builder.setTagList(tagParser.parseTags(tags));
        builder.setRange(Range.newRange(from, to));
        builder.setTimeSize((int) timeWindow.getWindowSlotSize());
        QueryParameter queryParameter = builder.build();

        return systemMetricDataService.getSystemMetricChart(timeWindow, queryParameter);
    }

    @Deprecated
    @GetMapping(value = "/chart", params = {"timeUnit", "timeSize"})
    public SystemMetricChart getSystemMetricChart(
            @RequestParam("hostGroupName") String hostGroupName,
            @RequestParam("hostName") String hostName,
            @RequestParam("metricName") String metricName,
            @RequestParam("fieldName") String fieldName,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam("timeUnit") String timeUnit,
            @RequestParam("timeSize") Integer timeSize) {
        TimePrecision timePrecision = TimePrecision.newTimePrecision(TimeUnit.valueOf(timeUnit.toUpperCase()), timeSize);

        final long minSamplingInterval = 10000L;
        final long inputInterval = timePrecision.getInterval();
        final long interval = Math.max(inputInterval, minSamplingInterval);
        TimeWindowSampler sampler = new TimeWindowSlotCentricSampler(interval, 200);
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), sampler);

        QueryParameter.Builder builder = new QueryParameter.Builder();
        builder.setHostGroupName(hostGroupName);
        builder.setHostName(hostName);
        builder.setMetricName(metricName);
        builder.setFieldName(fieldName);
        builder.setTagList(tagParser.parseTags(tags));
        builder.setRange(Range.newRange(from, to));
        builder.setTimePrecision(timePrecision);
        QueryParameter queryParameter = builder.build();

        return systemMetricDataService.getSystemMetricChart(timeWindow, queryParameter);
    }

    @GetMapping(value = "/hostGroup")
    public List<String> getHostGroup() {
        return systemMetricHostInfoService.getHostGroupNameList();
    }

    @GetMapping(value = "/hostGroup/host")
    public List<String> getHostGroup(@RequestParam("hostGroupName") String hostGroupName) {
        return systemMetricHostInfoService.getHostList(hostGroupName);
    }

    @Deprecated
    @GetMapping(value = "/hostGroup/host/collectedMetricInfo")
    public List<String> getCollectedMetricInfo(@RequestParam("hostGroupName") String hostGroupName, @RequestParam("hostName") String hostName) {
        return systemMetricHostInfoService.getCollectedMetricInfo(hostGroupName, hostName);
    }

    @GetMapping(value = "/hostGroup/host/collectedMetricInfoV2")
    public List<MetricInfo> getCollectedMetricInfoV2(@RequestParam("hostGroupName") String hostGroupName, @RequestParam("hostName") String hostName) {
        return systemMetricHostInfoService.getCollectedMetricInfoV2(hostGroupName, hostName);
    }

    @GetMapping(value = "/hostGroup/host/collectedTags")
    public List<String> getCollectedTags(@RequestParam("hostGroupName") String hostGroupName,
                                         @RequestParam("hostName") String hostName,
                                         @RequestParam("metricDefinitionId") String metricDefinitionId) {
        return systemMetricHostInfoService.getCollectedMetricInfoTags(hostGroupName, hostName, metricDefinitionId);
    }

    @GetMapping(value = "/hostGroup/host/collectedMetricData")
    public SystemMetricView getCollectedMetricData(@RequestParam("hostGroupName") String hostGroupName,
                                                   @RequestParam("hostName") String hostName,
                                                   @RequestParam("metricDefinitionId") String metricDefinitionId,
                                                   @RequestParam("from") long from,
                                                   @RequestParam("to") long to,
                                                   @RequestParam(value = "tags", required = false) String tags) {
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), DEFAULT_TIME_WINDOW_SAMPLER);
        MetricDataSearchKey metricDataSearchKey = new MetricDataSearchKey(hostGroupName, hostName, systemMetricBasicGroupManager.findMetricName(metricDefinitionId), metricDefinitionId, timeWindow);
        List<Tag> tagList = tagParser.parseTags(tags);

        SystemMetricData<? extends Number> systemMetricData = systemMetricDataService.getCollectedMetricData(metricDataSearchKey, timeWindow, tagList);
        return new SystemMetricView(systemMetricData);
    }
}
