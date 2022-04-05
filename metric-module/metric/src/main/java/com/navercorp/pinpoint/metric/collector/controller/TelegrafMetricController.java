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

package com.navercorp.pinpoint.metric.collector.controller;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.metric.collector.model.TelegrafMetric;
import com.navercorp.pinpoint.metric.collector.model.TelegrafMetrics;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricDataTypeService;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricService;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricTagService;
import com.navercorp.pinpoint.metric.common.model.DoubleMetric;
import com.navercorp.pinpoint.metric.common.model.Metrics;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.validation.SimpleErrorMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Hyunjoon Cho
 */
@RestController
public class TelegrafMetricController {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final SystemMetricService systemMetricService;
    private final SystemMetricDataTypeService systemMetricMetadataService;
    private final SystemMetricTagService systemMetricTagService;

    private static final List<String> ignoreTags = Collections.singletonList("host");

    public TelegrafMetricController(SystemMetricService systemMetricService,
                                    SystemMetricDataTypeService systemMetricMetadataService,
                                    SystemMetricTagService systemMetricTagService) {
        this.systemMetricService = Objects.requireNonNull(systemMetricService, "systemMetricService");
        this.systemMetricMetadataService = Objects.requireNonNull(systemMetricMetadataService, "systemMetricMetadataService");
        this.systemMetricTagService = Objects.requireNonNull(systemMetricTagService, "systemMetricTagService");
    }


    @PostMapping(value = "/telegraf")
    public ResponseEntity<Void> saveSystemMetric(
            @RequestHeader(value = "hostGroupName") String hostGroupName,
            @RequestBody TelegrafMetrics telegrafMetrics, BindingResult bindingResult
    ) throws BindException {
        if (bindingResult.hasErrors()) {
            SimpleErrorMessage simpleErrorMessage = new SimpleErrorMessage(bindingResult);
            logger.warn("metric binding error. header=hostGroupName:{} errorCount:{} {}", hostGroupName, bindingResult.getErrorCount(), simpleErrorMessage);
            throw new BindException(bindingResult);
        }

        String hostName = getHost(telegrafMetrics);

        if (logger.isDebugEnabled()) {
            logger.debug("hostGroupName:{} host:{} size:{}", hostGroupName, hostName, telegrafMetrics.size());
        }

        Metrics systemMetric = toMetrics(hostGroupName, hostName, telegrafMetrics);

        updateMetadata(systemMetric);
        systemMetricService.insert(systemMetric);

        return ResponseEntity.ok().build();
    }

    private String getHost(TelegrafMetrics metrics) {
        List<TelegrafMetric> metricList = metrics.getMetrics();
        if (CollectionUtils.isEmpty(metricList)) {
            return "";
        }
        List<Tag> tags = metricList.get(0).getTags();
        Tag host = getHost(tags);
        if (host == null) {
            return null;
        }
        return host.getValue();
    }

    private Metrics toMetrics(String hostGroupName, String hostName, TelegrafMetrics telegrafMetrics) {
        List<TelegrafMetric> metrics = telegrafMetrics.getMetrics();

        List<SystemMetric> metricList = metrics.stream()
                .flatMap(this::toMetric)
                .collect(Collectors.toList());

        return new Metrics(hostGroupName, hostName, metricList);
    }

    Stream<SystemMetric> toMetric(TelegrafMetric tMetric) {
        List<Tag> tTags = tMetric.getTags();

        final Tag hostTag = getHost(tTags);
        if (hostTag == null) {
            throw new RuntimeException("host tag not found");
        }

        List<Tag> tag = filterTag(tTags, ignoreTags);

        final long timestamp = TimeUnit.SECONDS.toMillis(tMetric.getTimestamp());


        List<TelegrafMetric.Field> fields = tMetric.getFields();
        return fields.stream()
                .map(field -> new DoubleMetric(tMetric.getName(), hostTag.getValue(), field.getName(), field.getValue(), tag, timestamp));
    }

    private Tag getHost(List<Tag> tTags) {
        return tTags.stream()
                .filter(tag -> tag.getName().equals("host"))
                .findFirst().orElse(null);
    }

    private List<Tag> filterTag(List<Tag> tTags, List<String> ignoreTagName) {
        return tTags.stream()
                .filter(entry -> !ignoreTagName.contains(entry.getName()))
                .collect(Collectors.toList());
    }

    private void updateMetadata(Metrics systemMetrics) {
        for (SystemMetric systemMetric : systemMetrics) {
            systemMetricMetadataService.saveMetricDataType(systemMetric);
            systemMetricTagService.saveMetricTag(systemMetrics.getHostGroupName(), systemMetric);
        }

    }
}