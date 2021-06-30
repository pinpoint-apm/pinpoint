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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricDataTypeService;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricService;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricTagService;
import com.navercorp.pinpoint.metric.common.model.Metrics;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Controller
public class SystemMetricController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper objectMapper;

    private final SystemMetricService<SystemMetric> systemMetricService;
    private final SystemMetricDataTypeService systemMetricMetadataService;
    private final SystemMetricTagService systemMetricTagService;

    public SystemMetricController(ObjectMapper objectMapper,
                                  SystemMetricService<SystemMetric> systemMetricService,
                                  SystemMetricDataTypeService systemMetricMetadataService,
                                  SystemMetricTagService systemMetricTagService) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");

        this.systemMetricService = Objects.requireNonNull(systemMetricService, "systemMetricService");
        this.systemMetricMetadataService = Objects.requireNonNull(systemMetricMetadataService, "systemMetricMetadataService");
        this.systemMetricTagService = Objects.requireNonNull(systemMetricTagService, "systemMetricTagService");
    }

    @RequestMapping(value = "/telegraf", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void saveSystemMetric(
            @RequestHeader(value = "Application-Name") String applicationName,
            @RequestBody String body) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Application-Name : {}", applicationName);
            logger.debug("body : {}", body);
        }

        Metrics metrics = objectMapper.readValue(body, Metrics.class);
        List<SystemMetric> metricList = metrics.getMetrics();

        updateMetadata(applicationName, metricList);
        systemMetricService.insert(applicationName, metricList);
    }

    private void updateMetadata(String applicationName, List<SystemMetric> systemMetrics) {
        for (SystemMetric systemMetric : systemMetrics) {
            systemMetricMetadataService.saveMetricDataType(systemMetric);
            systemMetricTagService.saveMetricTag(applicationName, systemMetric);
        }
    }
}