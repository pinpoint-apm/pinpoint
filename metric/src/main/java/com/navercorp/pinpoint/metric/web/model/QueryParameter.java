/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.model;


import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.util.Range;

import java.util.List;

/**
 * @author Hyunjoon Cho
 */
public class QueryParameter {
    private static final int TAG_SET_COUNT = 10;

    private String applicationName;
    private String hostName;
    private String metricName;
    private String fieldName;
    private List<Tag> tagList;
    private long interval = 10000L;
    private Range range;
    private long limit;

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setTagList(List<Tag> tagList) {
        this.tagList = tagList;
    }

    public List<Tag> getTagList() {
        return tagList;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public long getInterval() {
        return interval;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public Range getRange() {
        return range;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getLimit() {
        return limit;
    }

    public void estimateLimit() {
        this.limit = (range.getRange() / interval + 1) * TAG_SET_COUNT;
    }
}
