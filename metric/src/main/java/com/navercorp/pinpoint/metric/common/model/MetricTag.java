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
    private String metricName;
    private String fieldName;
    private List<Tag> tags;

    public MetricTag() {
    }

    public MetricTag(String applicationId, String metricName, String fieldName, List<Tag> tags) {
        if (StringUtils.isEmpty(applicationId)) {
            throw new IllegalArgumentException("applicationId must not be empty");
        }
        if (StringUtils.isEmpty(metricName)) {
            throw new IllegalArgumentException("metricName must not be empty");
        }
        if (StringUtils.isEmpty(fieldName)) {
            throw new IllegalArgumentException("fieldName must not be empty");
        }
        this.applicationId = applicationId;
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

    public MetricTag copy() {
        List<Tag> tagList = new ArrayList<Tag>(tags.size());

        for (Tag tag : tagList) {
            tagList.add(tag.copy());
        }

        return new MetricTag(applicationId, metricName, fieldName, tagList);
    }

}
