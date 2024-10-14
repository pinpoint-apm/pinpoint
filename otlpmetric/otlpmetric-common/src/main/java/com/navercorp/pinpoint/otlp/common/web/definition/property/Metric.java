/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.common.web.definition.property;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author minwoo-jung
 */
public class Metric {

    private final String metricName;
    private final Map<String, TagCluster> tagMap;
    private final Map<String, FieldCluster> fieldMap;

    public Metric(String metricName) {
        this.metricName = metricName;
        this.tagMap = new TreeMap<>();
        this.fieldMap = new TreeMap<>();
    }

    public void addTagAndField(String tag, String fieldName, String unit) {
        TagCluster tagCluster = tagMap.computeIfAbsent(tag, k -> new TagCluster(tag));
        tagCluster.addFieldAndUnit(fieldName, unit);

        FieldCluster fieldCluster = fieldMap.computeIfAbsent(fieldName, k -> new FieldCluster(fieldName, unit));
        fieldCluster.addTagGroup(tag);
    }

    public String getMetricName() {
        return metricName;
    }

    public List<TagCluster> getTagClusterList() {
        return List.copyOf(tagMap.values());
    }

    public List<FieldCluster> getFieldClusterList() {
        return List.copyOf(fieldMap.values());
    }

}
