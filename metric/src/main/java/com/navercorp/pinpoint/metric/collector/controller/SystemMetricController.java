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

import com.navercorp.pinpoint.metric.collector.service.SystemMetricDataTypeService;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricService;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricTagService;
import com.navercorp.pinpoint.metric.common.model.Metrics;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import com.navercorp.pinpoint.metric.common.model.validation.SimpleErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@RestController
public class SystemMetricController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SystemMetricService<SystemMetric> systemMetricService;
    private final SystemMetricDataTypeService systemMetricMetadataService;
    private final SystemMetricTagService systemMetricTagService;

    public SystemMetricController(SystemMetricService<SystemMetric> systemMetricService,
                                  SystemMetricDataTypeService systemMetricMetadataService,
                                  SystemMetricTagService systemMetricTagService) {
        this.systemMetricService = Objects.requireNonNull(systemMetricService, "systemMetricService");
        this.systemMetricMetadataService = Objects.requireNonNull(systemMetricMetadataService, "systemMetricMetadataService");
        this.systemMetricTagService = Objects.requireNonNull(systemMetricTagService, "systemMetricTagService");
    }

    @PostMapping(value = "/telegraf")
    public ResponseEntity<Void> saveSystemMetric(
            @RequestHeader(value = "Application-Name") String applicationName,
            @RequestBody @Valid Metrics metrics, BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            SimpleErrorMessage simpleErrorMessage = new SimpleErrorMessage(bindingResult);
            logger.warn("metric binding error. header=Application-Name:{} errorCount:{} {}", applicationName, bindingResult.getErrorCount(), simpleErrorMessage);
            throw new BindException(bindingResult);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Application-Name:{} size:{}", applicationName, metrics.size());
        }

        updateMetadata(applicationName, metrics);
        systemMetricService.insert(applicationName, metrics);

        return ResponseEntity.ok().build();
    }

    private void updateMetadata(String applicationName, Metrics systemMetrics) {
        for (SystemMetric systemMetric : systemMetrics) {
            systemMetricMetadataService.saveMetricDataType(systemMetric);
            systemMetricTagService.saveMetricTag(applicationName, systemMetric);
        }
    }
}