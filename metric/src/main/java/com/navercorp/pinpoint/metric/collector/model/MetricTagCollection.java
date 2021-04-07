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

package com.navercorp.pinpoint.metric.collector.model;

import com.navercorp.pinpoint.metric.common.model.MetricTag;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class MetricTagCollection {

    //TODO : (minwoo) applicationId 이름을 다른 이름으로 써야할듯함.
    private final String applicationId;
    private final String metricName;
    private final String fieldName;

    private final List<MetricTag> metricTagList;

    public MetricTagCollection(String applicationId, String metricName, String fieldName, List<MetricTag> metricTagList) {
        this.applicationId = applicationId;
        this.metricName = metricName;
        this.fieldName = fieldName;
        this.metricTagList = metricTagList;
    }

    public List<MetricTag> getMetricTagList() {
        return metricTagList;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
