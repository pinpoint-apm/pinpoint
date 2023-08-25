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
import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class InspectorMetricGroupData {
    private final String title;
    private final List<Long> timeStampList;
    private final Map<List<Tag>,List<InspectorMetricValue>> metricValueGroups;

    public InspectorMetricGroupData(String title, List<Long> timeStampList, Map<List<Tag>,List<InspectorMetricValue>> metricValueGroups) {
        this.title = StringPrecondition.requireHasLength(title, "title");
        this.timeStampList = Objects.requireNonNull(timeStampList, "timeStampList");
        this.metricValueGroups = Objects.requireNonNull(metricValueGroups, "metricValueGroups");
    }

    public String getTitle() {
        return title;
    }

    public List<Long> getTimeStampList() {
        return timeStampList;
    }

    public Map<List<Tag>, List<InspectorMetricValue>> getMetricValueGroups() {
        return metricValueGroups;
    }

}
