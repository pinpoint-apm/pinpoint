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
import com.navercorp.pinpoint.metric.web.util.QueryParameter;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;
import com.navercorp.pinpoint.metric.web.service.SystemMetricService;
import com.navercorp.pinpoint.metric.web.util.Range;
import com.navercorp.pinpoint.metric.web.util.TagParser;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;
import com.navercorp.pinpoint.metric.web.util.TimeWindowSampler;
import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricChart;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
            @RequestParam("to") long to) {

        QueryParameter.Builder builder = new QueryParameter.Builder();
        builder.setApplicationName(applicationName);
        builder.setHostName(hostName);
        builder.setMetricName(metricName);
        builder.setFieldName(fieldName);
        builder.setTagList(tagParser.parseTags(tags));
        builder.setRange(Range.newRange(from, to));
        QueryParameter queryParameter = builder.build();

        return systemMetricService.getSystemMetricBoList(queryParameter);
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
            @RequestParam("to") long to) {
        QueryParameter.Builder builder = new QueryParameter.Builder();
        builder.setApplicationName(applicationName);
        builder.setHostName(hostName);
        builder.setMetricName(metricName);
        builder.setFieldName(fieldName);
        builder.setTagList(tagParser.parseTags(tags));
        builder.setRange(Range.newRange(from, to));
        QueryParameter queryParameter = builder.build();

        TimeWindowSampler sampler = new TimeWindowSampler() {
            @Override
            public long getWindowSize(Range range) {
                return 10000L;
            }
        };
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), sampler);

        return systemMetricService.getSystemMetricChart(timeWindow, queryParameter);
    }

    @RequestMapping(value = "/chart", params = {"timeUnit", "timeSize"})
    @ResponseBody
    public SystemMetricChart getSystemMetricChart(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("hostName") String hostName,
            @RequestParam("metricName") String metricName,
            @RequestParam("fieldName") String fieldName,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam("timeUnit") String timeUnit,
            @RequestParam("timeSize") Integer timeSize) {
        TimePrecision timePrecision = TimePrecision.newTimePrecision(TimeUnit.valueOf(timeUnit.toUpperCase()), timeSize);

        QueryParameter.Builder builder = new QueryParameter.Builder();
        builder.setApplicationName(applicationName);
        builder.setHostName(hostName);
        builder.setMetricName(metricName);
        builder.setFieldName(fieldName);
        builder.setTagList(tagParser.parseTags(tags));
        builder.setRange(Range.newRange(from, to));
        builder.setTimePrecision(timePrecision);
        QueryParameter queryParameter = builder.build();

        final long minSamplingInterval = 10000L;
        final long inputInterval = timePrecision.getInterval();
        final long interval = inputInterval < minSamplingInterval ? minSamplingInterval : inputInterval;
        TimeWindowSampler sampler = new TimeWindowSampler() {
            @Override
            public long getWindowSize(Range range) {
                return interval;
            }
        };
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), sampler);

        return systemMetricService.getSystemMetricChart(timeWindow, queryParameter);
    }
}
