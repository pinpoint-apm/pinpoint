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

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class MetricTag {

    private String applicationId;
    private String hostName;
    private String metricName;
    private String fieldName;
    private List<Tag> tags;

    public MetricTag() {
    }

    public MetricTag(String applicationId, String hostName, String metricName, String fieldName, List<Tag> tags) {
        if (StringUtils.isEmpty(applicationId)) {
            throw new IllegalArgumentException("applicationId must not be empty");
        }
        if (StringUtils.isEmpty(hostName)) {
            throw new IllegalArgumentException("hostName must not be empty");
        }
        if (StringUtils.isEmpty(metricName)) {
            throw new IllegalArgumentException("metricName must not be empty");
        }
        if (StringUtils.isEmpty(fieldName)) {
            throw new IllegalArgumentException("fieldName must not be empty");
        }
        this.applicationId = applicationId;
        this.hostName = hostName;
        this.metricName = metricName;
        this.fieldName = fieldName;
        this.tags = Objects.requireNonNull(tags, "tags");
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
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
        List<Tag> tagList = new ArrayList<Tag>(tags.size());

        for (Tag tag : tagList) {
            tagList.add(tag.copy());
        }

        return new MetricTag(applicationId, hostName, metricName, fieldName, tagList);
    }

    @Override
    public String toString() {
        return "MetricTag{" +
                "applicationId='" + applicationId + '\'' +
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
        return Objects.equals(applicationId, metricTag.applicationId) &&
                Objects.equals(hostName, metricTag.hostName) &&
                Objects.equals(metricName, metricTag.metricName) &&
                Objects.equals(fieldName, metricTag.fieldName) &&
                Objects.equals(tags, metricTag.tags);
    }
}
