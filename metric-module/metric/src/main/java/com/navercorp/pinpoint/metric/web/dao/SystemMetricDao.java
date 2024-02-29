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

package com.navercorp.pinpoint.metric.web.dao;

import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.metric.web.model.MetricDataSearchKey;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Hyunjoon Cho
 */
public interface SystemMetricDao<T extends Number> {

    CompletableFuture<List<SystemMetricPoint<T>>> getAsyncSampledSystemMetricData(MetricDataSearchKey metricDataSearchKey, MetricTag metricTag);
}
