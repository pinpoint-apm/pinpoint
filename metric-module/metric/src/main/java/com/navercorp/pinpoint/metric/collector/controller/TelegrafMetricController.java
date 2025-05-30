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
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.metric.collector.model.TelegrafMetric;
import com.navercorp.pinpoint.metric.collector.model.TelegrafMetrics;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricDataTypeService;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricService;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricTagService;
import com.navercorp.pinpoint.metric.common.model.DoubleMetric;
import com.navercorp.pinpoint.metric.common.model.Metrics;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.validation.SimpleErrorMessage;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Hyunjoon Cho
 */
@RestController
public class TelegrafMetricController {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final SystemMetricService systemMetricService;
    private final SystemMetricDataTypeService systemMetricMetadataService;
    private final SystemMetricTagService systemMetricTagService;
    private final TenantProvider tenantProvider;

    private static final String[] ignoreTags = {"host"};

    public TelegrafMetricController(SystemMetricService systemMetricService,
                                    SystemMetricDataTypeService systemMetricMetadataService,
                                    SystemMetricTagService systemMetricTagService,
                                    TenantProvider tenantProvider) {
        this.systemMetricService = Objects.requireNonNull(systemMetricService, "systemMetricService");
        this.systemMetricMetadataService = Objects.requireNonNull(systemMetricMetadataService, "systemMetricMetadataService");
        this.systemMetricTagService = Objects.requireNonNull(systemMetricTagService, "systemMetricTagService");
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
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
        if (StringUtils.isEmpty(hostName)) {
            // hostname null check
            logger.info("hostName is empty. hostGroupName={}", hostGroupName);
            return ResponseEntity.badRequest().build();
        }


        if (logger.isDebugEnabled()) {
            logger.debug("hostGroupName:{} host:{} size:{}", hostGroupName, hostName, telegrafMetrics.size());
        }

        String tenantId = tenantProvider.getTenantId();

        Metrics systemMetric = toMetrics(tenantId, hostGroupName, hostName, telegrafMetrics);

        updateMetadata(systemMetric);
        systemMetricService.insert(systemMetric);

        return ResponseEntity.ok().build();
    }

    private String getHost(TelegrafMetrics metrics) {
        List<TelegrafMetric> metricList = metrics.getMetrics();
        if (CollectionUtils.isEmpty(metricList)) {
            return null;
        }
        List<Tag> tags = metricList.get(0).getTags();
        Tag host = getHost(tags);
        if (host == null) {
            return null;
        }
        return host.getValue();
    }

    private Metrics toMetrics(String tenantId, String hostGroupName, String hostName, TelegrafMetrics telegrafMetrics) {
        List<TelegrafMetric> metrics = telegrafMetrics.getMetrics();

        List<DoubleMetric> metricList = toDoubleMetric(metrics);

        return new Metrics(tenantId, hostGroupName, hostName, metricList);
    }

    private @NotNull List<DoubleMetric> toDoubleMetric(List<TelegrafMetric> metrics) {
        List<DoubleMetric> result = new ArrayList<>();
        for (TelegrafMetric metric : metrics) {
            result.addAll(toMetric(metric));
        }
        return result;
    }

    List<DoubleMetric> toMetric(TelegrafMetric tMetric) {
        List<Tag> tTags = tMetric.getTags();

        final Tag hostTag = getHost(tTags);
        if (hostTag == null) {
            throw new RuntimeException("host tag not found");
        }

        List<Tag> tag = filterTag(tTags, ignoreTags);

        final long timestamp = TimeUnit.SECONDS.toMillis(tMetric.getTimestamp());

        List<TelegrafMetric.Field> fields = tMetric.getFields();

        List<DoubleMetric> list = new ArrayList<>(fields.size());
        for (TelegrafMetric.Field field : fields) {
            DoubleMetric doubleMetric = new DoubleMetric(tMetric.getName(), hostTag.getValue(), field.name(), field.value(), tag, timestamp);
            list.add(doubleMetric);
        }
        return list;
    }

    private Tag getHost(List<Tag> tTags) {
        for (Tag tag : tTags) {
            if ("host".equals(tag.getName())) {
                return tag;
            }
        }
        return null;
    }

    static List<Tag> filterTag(List<Tag> tTags, String[] ignoreTagName) {
        List<Tag> copy = new ArrayList<>(tTags.size());
        for (Tag entry : tTags) {
            if (!ArrayUtils.contains(ignoreTagName, entry.getName())) {
                copy.add(entry);
            }
        }
        return copy;
    }

    private void updateMetadata(Metrics systemMetrics) {
        for (DoubleMetric systemMetric : systemMetrics) {
            systemMetricMetadataService.saveMetricDataType(systemMetric);
            systemMetricTagService.saveMetricTag(systemMetrics.getTenantId(), systemMetrics.getHostGroupName(), systemMetric);
        }

    }
}