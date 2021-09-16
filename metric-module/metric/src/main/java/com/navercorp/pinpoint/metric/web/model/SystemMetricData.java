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
    private final String unit;
    private final List<Long> timeStampList;
    private final List<MetricValueGroup<Y>> metricValueGroupList;

    public SystemMetricData(String title, String unit, List<Long> timeStampList, List<MetricValueGroup<Y>> metricValueGroupList) {
        this.title = StringPrecondition.requireHasLength(title, "title");
        this.unit = StringPrecondition.requireHasLength(unit, "unit");
        this.timeStampList = Objects.requireNonNull(timeStampList, "timeStampList");
        this.metricValueGroupList = Objects.requireNonNull(metricValueGroupList, "metricValueGroupList");
    }

    public String getUnit() {
        return unit;
    }

    public String getTitle() {
        return title;
    }

    public List<Long> getTimeStampList() {
        return timeStampList;
    }

    public List<MetricValueGroup<Y>> getMetricValueGroupList() {
        return metricValueGroupList;
    }

}
