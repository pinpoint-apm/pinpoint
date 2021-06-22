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

package com.navercorp.pinpoint.metric.web.dao.model;

import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.model.MetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.util.Range;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author minwoo.jung
 */
public class SystemMetricDataSearchKey {

    private final String hostGroupId;
    private final String hostName;
    private final String metricName;
    private final String fieldName;
    private final List<Tag> tagList;
    private final Range range;
    private final TimePrecision timePrecision;
    private final long limit;

    public SystemMetricDataSearchKey(MetricDataSearchKey metricDataSearchKey, MetricTag metricTag) {
        Objects.requireNonNull(metricDataSearchKey, "range");
        Objects.requireNonNull(metricTag, "metricTag");

        this.hostGroupId = metricDataSearchKey.getHostGroupId();
        this.hostName = metricDataSearchKey.getHostName();
        this.metricName = metricDataSearchKey.getMetricName();
        this.range = metricDataSearchKey.getRange();
        this.timePrecision = metricDataSearchKey.getTimePrecision();
        this.limit = metricDataSearchKey.getLimit();

        this.fieldName = metricTag.getFieldName();
        this.tagList = metricTag.getTags();
    }

    public String getHostGroupId() {
        return hostGroupId;
    }

    public String getHostName() {
        return hostName;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public List<Tag> getTagList() {
        return tagList;
    }

    public Range getRange() {
        return range;
    }

    public TimePrecision getTimePrecision() {
        return timePrecision;
    }

    public long getLimit() {
        return limit;
    }

}
