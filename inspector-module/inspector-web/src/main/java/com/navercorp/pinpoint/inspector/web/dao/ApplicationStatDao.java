/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.inspector.web.dao;

import com.navercorp.pinpoint.common.model.TagInformation;
import com.navercorp.pinpoint.common.timeseries.point.DataPoint;
import com.navercorp.pinpoint.inspector.web.definition.metric.field.Field;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.chart.AvgMinMaxMetricPoint;
import com.navercorp.pinpoint.metric.common.model.chart.AvgMinMetricPoint;
import com.navercorp.pinpoint.metric.common.model.chart.MinMaxMetricPoint;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author minwoo-jung
 */
public interface ApplicationStatDao {
    CompletableFuture<List<AvgMinMaxMetricPoint>> selectStatAvgMinMax(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field);

    CompletableFuture<List<MinMaxMetricPoint>>  selectStatMinMax(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field);

    CompletableFuture<List<DataPoint<Double>>>  selectStatSum(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field);

    CompletableFuture<List<AvgMinMetricPoint>> selectStatAvgMin(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field);

    CompletableFuture<List<DataPoint<Double>>> selectStatMax(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field);

    List<Tag> getTagInfo(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field);

    TagInformation getTagInfoContainedSpecificTag(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field, Tag tag);
}
