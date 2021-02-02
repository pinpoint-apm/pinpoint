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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.navercorp.pinpoint.metric.collector.model.SystemMetricJsonDeserializer;
import com.navercorp.pinpoint.metric.collector.service.SystemMetricService;
import com.navercorp.pinpoint.metric.common.model.DoubleCounter;
import com.navercorp.pinpoint.metric.common.model.LongCounter;
import com.navercorp.pinpoint.metric.common.model.MetricType;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import com.navercorp.pinpoint.metric.common.model.SystemMetricMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Controller
public class SystemMetricController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SystemMetricService<SystemMetric> systemMetricService;
    private final ObjectMapper objectMapper;
    private final SystemMetricMetadata systemMetricMetadata;

    public SystemMetricController(ObjectMapper objectMapper,
                                  SystemMetricService<SystemMetric> systemMetricService,
                                  SystemMetricMetadata systemMetricMetadata) {
        Objects.requireNonNull(objectMapper, "objectMapper");
        this.objectMapper = objectMapper.registerModule(deserializerModule());
        this.systemMetricService = Objects.requireNonNull(systemMetricService, "systemMetricService");
        this.systemMetricMetadata = Objects.requireNonNull(systemMetricMetadata, "systemMetricMetadata");
    }

    @RequestMapping(value = "/telegraf", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void saveSystemMetric(
            @RequestHeader(value = "Application-Name") String applicationName,
            @RequestBody String body) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(body).get("metrics");
        List<SystemMetric> systemMetrics = Arrays.asList(objectMapper.readValue(jsonNode.toString(), SystemMetric[].class));
        updateMetadata(systemMetrics);
        systemMetricService.insert(applicationName, systemMetrics);
    }

    private SimpleModule deserializerModule() {
        return new SimpleModule().addDeserializer(SystemMetric.class, new SystemMetricJsonDeserializer());
    }

    private void updateMetadata(List<SystemMetric> systemMetrics) {
        for (SystemMetric systemMetric : systemMetrics) {
            if (systemMetric instanceof LongCounter) {
                systemMetricMetadata.put(systemMetric.getMetricName(), systemMetric.getFieldName(), MetricType.LongCounter);
            } else if (systemMetric instanceof DoubleCounter) {
                systemMetricMetadata.put(systemMetric.getMetricName(), systemMetric.getFieldName(), MetricType.DoubleCounter);
            } else {
                throw new IllegalArgumentException("UnknownType:" + systemMetric.getClass().getName());
            }
        }
        systemMetricMetadata.save();
    }
}