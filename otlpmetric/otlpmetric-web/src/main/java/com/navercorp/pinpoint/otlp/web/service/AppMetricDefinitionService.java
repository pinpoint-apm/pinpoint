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

import com.navercorp.pinpoint.otlp.common.web.defined.AppMetricDefinition;
import com.navercorp.pinpoint.otlp.common.web.defined.AppMetricDefinitionGroup;

import java.util.List;

/**
 * @author minwoo-jung
 */
public interface AppMetricDefinitionService {

    boolean existUserDefinedMetric(String applicationName, String metricName);

    AppMetricDefinitionGroup getUserDefinedMetric(String applicationName);

    void updateUserDefinedMetric(AppMetricDefinitionGroup appMetricDefinitionGroup);
}
