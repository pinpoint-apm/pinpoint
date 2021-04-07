/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.service;

import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import com.navercorp.pinpoint.metric.web.model.MetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.model.SystemMetricData;
import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricChart;
import com.navercorp.pinpoint.metric.web.util.QueryParameter;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;

import java.util.List;

/**
 * @author minwoo.jung
 */
public interface SystemMetricDataService {

    List<SystemMetric> getSystemMetricBoList(QueryParameter queryParameter);

    SystemMetricChart<? extends Number> getSystemMetricChart(TimeWindow timeWindow, QueryParameter queryParameter);

    SystemMetricData<? extends Number> getCollectedMetricData(MetricDataSearchKey metricDataSearchKey, TimeWindow timeWindow);
}
