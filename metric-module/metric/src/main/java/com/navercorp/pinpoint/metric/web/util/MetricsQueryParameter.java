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
public class MetricsQueryParameter extends QueryParameter {
    private final String hostGroupName;
    private final String hostName;
    private final String metricName;
    private final String fieldName;

    private final List<Tag> tagList;

    public MetricsQueryParameter(Builder builder) {
        super(builder.range, builder.timePrecision, builder.limit);
        this.hostGroupName = builder.hostGroupName;
        this.hostName = builder.hostName;
        this.metricName = builder.metricName;
        this.fieldName = builder.fieldName;
        this.tagList = builder.tagList;
    }

    public String getHostGroupName() {
        return hostGroupName;
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

    public static class Builder extends QueryParameter.Builder {
        private String hostGroupName;
        private String hostName;
        private String metricName;
        private String fieldName;
        private List<Tag> tagList;

        public void setHostGroupName(String hostGroupName) {
            this.hostGroupName = Objects.requireNonNull(hostGroupName, "hostGroupName");
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

        public MetricsQueryParameter build() {
            if (timePrecision == null) {
                this.timePrecision = TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, 10000);
            }
            this.limit = estimateLimit();

            return new MetricsQueryParameter(this);
        }
    }
}
