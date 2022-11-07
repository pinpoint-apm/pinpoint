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

    private String tenantId;
    private String hostGroupName;
    private String hostName;
    private String metricName;
    private String fieldName;
    private List<Tag> tags;
    private long saveTime;

    public MetricTag() {
    }

    public MetricTag(String tenantId, String hostGroupName, String hostName, String metricName, String fieldName, List<Tag> tags, long saveTime) {
        this.tenantId = StringPrecondition.requireHasLength(tenantId, "tenantId");
        this.hostGroupName = StringPrecondition.requireHasLength(hostGroupName, "hostGroupName");
        this.hostName = StringPrecondition.requireHasLength(hostName, "hostName");
        this.metricName = StringPrecondition.requireHasLength(metricName, "metricName");
        this.fieldName = StringPrecondition.requireHasLength(fieldName, "fieldName");
        this.tags = Objects.requireNonNull(tags, "tags");
        this.saveTime = saveTime;
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

    public long getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(long saveTime) {
        this.saveTime = saveTime;
    }

    public String getTenantId() {
        return tenantId;
    }

    public MetricTag copy() {
        List<Tag> tagList = new ArrayList<>(this.tags);

        return new MetricTag(tenantId, hostGroupName, hostName, metricName, fieldName, tagList, saveTime);
    }


    @Override
    public String toString() {
        return "MetricTag{" +
                "tenantId='" + tenantId + '\'' +
                ", hostGroupName='" + hostGroupName + '\'' +
                ", hostName='" + hostName + '\'' +
                ", metricName='" + metricName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", tags=" + tags +
                ", saveTime=" + saveTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricTag metricTag = (MetricTag) o;
        return saveTime == metricTag.saveTime && tenantId.equals(metricTag.tenantId) && hostGroupName.equals(metricTag.hostGroupName) && hostName.equals(metricTag.hostName) && metricName.equals(metricTag.metricName) && fieldName.equals(metricTag.fieldName) && tags.equals(metricTag.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, hostGroupName, hostName, metricName, fieldName, tags, saveTime);
    }
}
