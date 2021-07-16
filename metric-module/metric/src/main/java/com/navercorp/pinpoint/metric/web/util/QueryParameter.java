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

package com.navercorp.pinpoint.metric.web.util;


import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Hyunjoon Cho
 */
public class QueryParameter {
    private static final int TAG_SET_COUNT = 10;

    private final String applicationName;
    private final String hostName;
    private final String metricName;
    private final String fieldName;
    private final List<Tag> tagList;
    private final Range range;
    private final TimePrecision timePrecision;
    private final long limit;

    public QueryParameter(Builder builder) {
        this.applicationName = builder.applicationName;
        this.hostName = builder.hostName;
        this.metricName = builder.metricName;
        this.fieldName = builder.fieldName;
        this.tagList = builder.tagList;
        this.range = builder.range;
        this.timePrecision = builder.timePrecision;
        this.limit = builder.limit;
    }

    public String getApplicationName() {
        return applicationName;
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

    public static class Builder {
        private String applicationName;
        private String hostName;
        private String metricName;
        private String fieldName;
        private List<Tag> tagList;
        private Range range;
        private TimePrecision timePrecision;
        private long limit;

        public void setApplicationName(String applicationName) {
            this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        }

        public void setHostName(String hostName) {
            this.hostName = Objects.requireNonNull(hostName, "hostName");
        }

        public void setMetricName(String metricName) {
            this.metricName = Objects.requireNonNull(metricName, "metricName");
        }

        public void setFieldName(String fieldName) {
            this.fieldName = Objects.requireNonNull(fieldName, "fieldName");
        }

        public void setTagList(List<Tag> tagList) {
            this.tagList = tagList;
        }

        public void setRange(Range range) {
            this.range = range;
        }

        public void setTimePrecision(TimePrecision timePrecision) {
            this.timePrecision = timePrecision;
        }

        public long estimateLimit() {
            return (range.getRange() / timePrecision.getInterval() + 1) * TAG_SET_COUNT;
        }

        public QueryParameter build() {
            if (timePrecision == null) {
                this.timePrecision = TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, 10000);
            }
            this.limit = estimateLimit();

            return new QueryParameter(this);
        }
    }
}
