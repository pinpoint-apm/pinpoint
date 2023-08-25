/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.web.model;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
// TODO : (minwoo) It would be better to combine it with com.navercorp.pinpoint.metric.web.model.SystemMetricData.
public class InspectorMetricData {
    private final String title;
    private final List<Long> timestampList;
    private final List<InspectorMetricValue> metricValueList;
    public InspectorMetricData(String title, List<Long> timestampList, List<InspectorMetricValue> metricValueList) {
        this.title = StringPrecondition.requireHasLength(title, "title");
        this.timestampList = Objects.requireNonNull(timestampList, "timeStampList");
        this.metricValueList = Objects.requireNonNull(metricValueList, "metricValueList");
    }

    public String getTitle() {
        return title;
    }

    public List<Long> getTimestampList() {
        return timestampList;
    }

    public List<InspectorMetricValue> getMetricValueList() {
        return metricValueList;
    }
}
