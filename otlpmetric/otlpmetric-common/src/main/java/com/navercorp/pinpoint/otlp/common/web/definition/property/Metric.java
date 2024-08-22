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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo-jung
 */
public class Metric {

    private final String metricName;
    private final Map<String, TagCluster> tagMap;

    public Metric(String metricName) {
        this.metricName = metricName;
        this.tagMap = new HashMap<>();
    }

    public void addTagAndUnit(String tag, String fieldName, String unit) {
        TagCluster tagCluster = tagMap.computeIfAbsent(tag, k -> new TagCluster(tag));
        tagCluster.addFieldAndUnit(fieldName, unit);
    }

    public String getMetricName() {
        return metricName;
    }

    public List<TagCluster> getTagClusterList() {
        return List.copyOf(tagMap.values());
    }

}
