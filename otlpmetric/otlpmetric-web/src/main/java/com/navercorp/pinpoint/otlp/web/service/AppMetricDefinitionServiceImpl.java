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

import com.navercorp.pinpoint.otlp.common.defined.AppMetricDefinition;
import com.navercorp.pinpoint.otlp.web.dao.AppMetricDefinitionDao;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
    public void addUserDefinedMetric(AppMetricDefinition appMetricDefinition) {
        List<AppMetricDefinition> appMetricDefinitionList = appMetricDefinitionDao.selectAppMetricDefinitionList(appMetricDefinition.getApplicationName());

        if (appMetricDefinitionList == null) {
            appMetricDefinitionList = new ArrayList<>();
        }

        appMetricDefinitionList.add(appMetricDefinition);
        appMetricDefinitionDao.insertAppMetricDefinitionList(appMetricDefinitionList);
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
    public List<AppMetricDefinition> getUserDefinedMetric(String applicationName) {
        return appMetricDefinitionDao.selectAppMetricDefinitionList(applicationName);
    }

    @Override
    public void updateUserDefinedMetric(List<AppMetricDefinition> appMetricDefinitionList) {
        appMetricDefinitionDao.updateAppMetricDefinitionList(appMetricDefinitionList);
    }
}
