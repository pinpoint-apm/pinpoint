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

package com.navercorp.pinpoint.metric.common.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class MetricTag {

    private String hostGroupName;
    private String hostName;
    private String metricName;
    private String fieldName;
    private List<Tag> tags;

    public MetricTag() {
    }

    public MetricTag(String hostGroupName, String hostName, String metricName, String fieldName, List<Tag> tags) {
        this.hostGroupName = StringPrecondition.requireHasLength(hostGroupName, "hostGroupName");
        this.hostName = StringPrecondition.requireHasLength(hostName, "hostName");
        this.metricName = StringPrecondition.requireHasLength(metricName, "metricName");
        this.fieldName = StringPrecondition.requireHasLength(fieldName, "fieldName");
        this.tags = Objects.requireNonNull(tags, "tags");
    }

    public String getHostGroupName() {
        return hostGroupName;
    }

    public void setHostGroupName(String hostGroupName) {
        this.hostGroupName = hostGroupName;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public MetricTag copy() {
        List<Tag> tagList = new ArrayList<>(this.tags);

        return new MetricTag(hostGroupName, hostName, metricName, fieldName, tagList);
    }

    @Override
    public String toString() {
        return "MetricTag{" +
                "hostGroupName='" + hostGroupName + '\'' +
                ", hostName='" + hostName + '\'' +
                ", metricName='" + metricName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", tags=" + tags +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetricTag metricTag = (MetricTag) o;

        if (hostGroupName != null ? !hostGroupName.equals(metricTag.hostGroupName) : metricTag.hostGroupName != null)
            return false;
        if (hostName != null ? !hostName.equals(metricTag.hostName) : metricTag.hostName != null) return false;
        if (metricName != null ? !metricName.equals(metricTag.metricName) : metricTag.metricName != null) return false;
        if (fieldName != null ? !fieldName.equals(metricTag.fieldName) : metricTag.fieldName != null) return false;
        return tags != null ? tags.equals(metricTag.tags) : metricTag.tags == null;
    }

    @Override
    public int hashCode() {
        int result = hostGroupName != null ? hostGroupName.hashCode() : 0;
        result = 31 * result + (hostName != null ? hostName.hashCode() : 0);
        result = 31 * result + (metricName != null ? metricName.hashCode() : 0);
        result = 31 * result + (fieldName != null ? fieldName.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        return result;
    }
}
