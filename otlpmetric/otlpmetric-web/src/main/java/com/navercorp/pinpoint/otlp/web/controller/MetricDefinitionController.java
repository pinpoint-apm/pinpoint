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

import com.navercorp.pinpoint.otlp.common.definition.MetricDefinitionProperty;
import com.navercorp.pinpoint.otlp.web.service.MetricDefinitionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author minwoo-jung
 */
@RestController
@RequestMapping(value = "/api/otlp")
public class MetricDefinitionController {

    private final MetricDefinitionService metricDefinitionService;

    public MetricDefinitionController(MetricDefinitionService metricDefinitionService) {
        this.metricDefinitionService = metricDefinitionService;
    }

    @GetMapping("/metricDefinition/info")
    public MetricDefinitionProperty getMetricDefinitionInfo(@RequestParam("applicationName") String applicationName) {
        return metricDefinitionService.getMetricDefinitionInfo(applicationName);
    }
}
