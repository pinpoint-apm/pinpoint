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

import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.otlp.common.web.defined.AppMetricDefinition;
import com.navercorp.pinpoint.otlp.common.web.defined.AppMetricDefinitionGroup;
import com.navercorp.pinpoint.otlp.common.web.definition.property.MetricDefinitionProperty;
import com.navercorp.pinpoint.otlp.web.service.AppMetricDefinitionService;
import com.navercorp.pinpoint.otlp.web.service.MetricMetadataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author minwoo-jung
 */
@RestController
@RequestMapping(value = "/api/otlp")
public class MetricDefinitionController {

    private final MetricMetadataService metricMetadataService;
    private final AppMetricDefinitionService appMetricDefinitionService;

    public MetricDefinitionController(MetricMetadataService metricMetadataService, AppMetricDefinitionService appMetricDefinitionService) {
        this.appMetricDefinitionService = appMetricDefinitionService;
        this.metricMetadataService = metricMetadataService;
    }

    @GetMapping("/metricDef/property")
    public MetricDefinitionProperty getMetricDefinitionProperty(@RequestParam("applicationName") String applicationName) {
        return metricMetadataService.getMetricDefinitionInfo(applicationName);
    }

    @GetMapping("/metricDef/userDefined")
    public AppMetricDefinitionGroup addUserDefinedMetric(@RequestParam("applicationName") String applicationName) {
        return appMetricDefinitionService.getUserDefinedMetric(applicationName);
    }

    @PatchMapping(value = "/metricDef/userDefined")
    public Response updateUserDefinedMetric(@RequestBody AppMetricDefinitionGroup appMetricDefinitionGroup) {
        List<AppMetricDefinition> appMetricDefinitionList = appMetricDefinitionGroup.getAppMetricDefinitionList();
        validate(appMetricDefinitionList);
        generateAndSetUniqueId(appMetricDefinitionGroup.getAppMetricDefinitionList());

        appMetricDefinitionService.updateUserDefinedMetric(appMetricDefinitionGroup);
        return SimpleResponse.ok();
    }

    private void validate(List<AppMetricDefinition> appMetricDefinitionList) {
        for (AppMetricDefinition appMetricDefinition : appMetricDefinitionList) {
            List<String> tagGroupList = appMetricDefinition.getTagGroupList();
            List<String> fieldNameList = appMetricDefinition.getFieldNameList();

            if (tagGroupList.size() > 1 && fieldNameList.size() > 1) {
                throw new IllegalArgumentException("N:N relationship between fields and tags is not allowed.");
            }

            if (tagGroupList.size() != 1 && fieldNameList.size() != 1) {
                throw new IllegalArgumentException("Either tagGroupList or fieldNameList must have a size of exactly one.");
            }
        }
    }

    private void generateAndSetUniqueId(List<AppMetricDefinition> appMetricDefinitionList) {
        Set<String> existingIds = appMetricDefinitionList.stream()
                .map(AppMetricDefinition::getId)
                .filter(StringUtils::hasLength)
                .collect(Collectors.toSet());

        appMetricDefinitionList.stream().filter(appMetricDefinition -> StringUtils.isEmpty(appMetricDefinition.getId()))
                .forEach(definition -> {
                    String newId;

                    do {
                        newId = UUID.randomUUID().toString().substring(0, 8);
                    } while (!existingIds.add(newId));

                    definition.setId(newId);
                });
    }
}
