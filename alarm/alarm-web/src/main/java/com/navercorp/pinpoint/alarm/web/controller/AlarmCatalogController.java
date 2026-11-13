/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.alarm.web.controller;

import com.navercorp.pinpoint.alarm.web.model.AlarmTemplatePreset;
import com.navercorp.pinpoint.alarm.web.service.AlarmCatalogService;
import com.navercorp.pinpoint.alarm.web.view.AlarmCatalogResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/alarm")
public class AlarmCatalogController {

    private final AlarmCatalogService catalogService;

    public AlarmCatalogController(AlarmCatalogService catalogService) {
        this.catalogService = Objects.requireNonNull(catalogService, "catalogService");
    }

    @GetMapping("/datasources")
    public List<AlarmCatalogResponse.DataSource> getDataSources(
            @RequestParam(value = "applicationType", required = false) String applicationType) {
        return catalogService.getDataSources(applicationType).stream()
                .map(AlarmCatalogResponse.DataSource::from)
                .toList();
    }

    @GetMapping("/metrics")
    public List<AlarmCatalogResponse.Metric> getMetrics(
            @RequestParam("dataSource") String dataSource) {
        return catalogService.getMetrics(dataSource).stream()
                .map(AlarmCatalogResponse.Metric::from)
                .toList();
    }

    @GetMapping("/template-presets")
    public List<AlarmTemplatePreset> getTemplatePresets() {
        return catalogService.getTemplatePresets();
    }
}
