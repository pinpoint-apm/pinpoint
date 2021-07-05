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
import com.navercorp.pinpoint.metric.web.service.SystemMetricService;
import com.navercorp.pinpoint.metric.web.util.Range;
import com.navercorp.pinpoint.metric.web.util.TagParser;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;
import com.navercorp.pinpoint.metric.web.util.TimeWindowSampler;
import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricChart;
import com.navercorp.pinpoint.metric.web.util.TimeWindowSlotCentricSampler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Controller
@RequestMapping(value = "/systemMetric")
public class SystemMetricController {
    private final SystemMetricService systemMetricService;
    private final TagParser tagParser = new TagParser();

    public SystemMetricController(SystemMetricService systemMetricService) {
        this.systemMetricService = Objects.requireNonNull(systemMetricService, "systemMetricService");
    }

    @RequestMapping(value = "/list")
    @ResponseBody
    public List<SystemMetric> getSystemMetricBoList(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("hostName") String hostName,
            @RequestParam("metricName") String metricName,
            @RequestParam("fieldName") String fieldName,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam("from") long from,
            @RequestParam("to") long to){
        List<Tag> tagList = tagParser.parseTags(tags);
        return systemMetricService.getSystemMetricBoList(applicationName, hostName, metricName, fieldName, tagList, Range.newRange(from, to));
    }

    @RequestMapping(value = "/chart")
    @ResponseBody
    public SystemMetricChart getSystemMetricChart(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("hostName") String hostName,
            @RequestParam("metricName") String metricName,
            @RequestParam("fieldName") String fieldName,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam("from") long from,
            @RequestParam("to") long to){
        TimeWindowSampler sampler = new TimeWindowSlotCentricSampler();
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), sampler);
        List<Tag> tagList = tagParser.parseTags(tags);

        return systemMetricService.getSystemMetricChart(applicationName, hostName, metricName, fieldName, tagList, timeWindow);
    }

    @RequestMapping(value = "/chart", params = {"interval"})
    @ResponseBody
    public SystemMetricChart getSystemMetricChart(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("hostName") String hostName,
            @RequestParam("metricName") String metricName,
            @RequestParam("fieldName") String fieldName,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam("interval") Integer interval){
        final int minSamplingInterval = 10;
        final long intervalMs = interval < minSamplingInterval ? minSamplingInterval * 1000L : interval * 1000L;
        TimeWindowSampler sampler = new TimeWindowSampler() {
            @Override
            public long getWindowSize(Range range) {
                return intervalMs;
            }
        };
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), sampler);
        List<Tag> tagList = tagParser.parseTags(tags);

        return systemMetricService.getSystemMetricChart(applicationName, hostName, metricName, fieldName, tagList, timeWindow);
    }
}
