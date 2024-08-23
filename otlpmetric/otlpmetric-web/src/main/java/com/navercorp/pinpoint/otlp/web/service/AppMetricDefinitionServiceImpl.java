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

package com.navercorp.pinpoint.otlp.web.service;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.otlp.common.web.defined.AppMetricDefinition;
import com.navercorp.pinpoint.otlp.common.web.defined.AppMetricDefinitionGroup;
import com.navercorp.pinpoint.otlp.web.dao.AppMetricDefinitionDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author minwoo-jung
 */
@Service
public class AppMetricDefinitionServiceImpl implements AppMetricDefinitionService {

    private final AppMetricDefinitionDao appMetricDefinitionDao;

    public AppMetricDefinitionServiceImpl(AppMetricDefinitionDao appMetricDefinitionDao) {
        this.appMetricDefinitionDao = appMetricDefinitionDao;
    }

    @Override
    public boolean existUserDefinedMetric(String applicationName, String metricName) {
        List<AppMetricDefinition> appMetricDefinitionList = appMetricDefinitionDao.selectAppMetricDefinitionList(applicationName);

        if (appMetricDefinitionList == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public AppMetricDefinitionGroup getUserDefinedMetric(String applicationName) {
        List<AppMetricDefinition> appMetricDefinitionList = appMetricDefinitionDao.selectAppMetricDefinitionList(applicationName);
        return new AppMetricDefinitionGroup(applicationName, appMetricDefinitionList);
    }

    @Override
    public void updateUserDefinedMetric(AppMetricDefinitionGroup appMetricDefinitionGroup) {
        List<AppMetricDefinition> appMetricDefinitionList = appMetricDefinitionGroup.getAppMetricDefinitionList();
        generateAndSetUniqueId(appMetricDefinitionGroup.getAppMetricDefinitionList());
        appMetricDefinitionDao.updateAppMetricDefinitionList(appMetricDefinitionGroup.getApplicationName(), appMetricDefinitionList);
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
