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

package com.navercorp.pinpoint.metric.web.model;

import com.navercorp.pinpoint.metric.common.model.StringPrecondition;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class SystemMetricData<Y extends Number> {
    private final String title;
    private final List<Long> timeStampList;
    private final List<MetricValue<Y>> metricValueList;

    public SystemMetricData(String title, List<Long> timeStampList, List<MetricValue<Y>> metricValueList) {
        this.title = StringPrecondition.requireHasLength(title, "title");
        this.timeStampList = Objects.requireNonNull(timeStampList, "timeStampList");
        this.metricValueList = Objects.requireNonNull(metricValueList, "metricValueList");
    }

    public String getTitle() {
        return title;
    }

    public List<Long> getTimeStampList() {
        return timeStampList;
    }

    public List<MetricValue<Y>> getMetricValueList() {
        return metricValueList;
    }
}
