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
package com.navercorp.pinpoint.alarm.web.service;

import com.navercorp.pinpoint.alarm.service.AlarmDataSourceRegistry;
import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.vo.AlarmMetricDefinition;
import com.navercorp.pinpoint.alarm.web.model.AlarmTemplatePreset;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AlarmCatalogService {

    private final AlarmTemplatePresetLoader templatePresetLoader;
    private final AlarmDataSourceRegistry dataSourceRegistry;
    private final AlarmApplicationResolver applicationResolver;

    public AlarmCatalogService(AlarmTemplatePresetLoader templatePresetLoader,
                               AlarmDataSourceRegistry dataSourceRegistry,
                               AlarmApplicationResolver applicationResolver) {
        this.templatePresetLoader = Objects.requireNonNull(templatePresetLoader, "templatePresetLoader");
        this.dataSourceRegistry = Objects.requireNonNull(dataSourceRegistry, "dataSourceRegistry");
        this.applicationResolver = Objects.requireNonNull(applicationResolver, "applicationResolver");
    }

    /** With an application type, only the data sources a rule on that type may use. */
    public List<AlarmDataSource> getDataSources(String applicationType) {
        String category = applicationType == null ? null : applicationResolver.categoryOf(applicationType);
        return dataSourceRegistry.all().stream()
                .filter(dataSource -> category == null || category.equals(dataSource.category()))
                .toList();
    }

    public List<AlarmMetricDefinition> getMetrics(String dataSource) {
        return dataSourceRegistry.get(dataSource).metrics();
    }

    public List<AlarmTemplatePreset> getTemplatePresets() {
        return templatePresetLoader.getPresets();
    }
}
