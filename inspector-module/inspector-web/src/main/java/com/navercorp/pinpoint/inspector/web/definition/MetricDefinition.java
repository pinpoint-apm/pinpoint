/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.web.definition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.common.util.ObjectUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.inspector.web.definition.metric.EmptyPostProcessor;
import com.navercorp.pinpoint.inspector.web.definition.metric.EmptyPreProcessor;
import com.navercorp.pinpoint.inspector.web.definition.metric.field.Field;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.GroupingRule;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
//TODO : (minwoo) Need to integrate with com.navercorp.pinpoint.metric.web.mapping.Metric of metric module
public class MetricDefinition {


    private final String definitionId;
    private final String metricName;
    private final String title;
    private final String postProcess;
    private final String preProcess;
    private final GroupingRule groupingRule;
    private final List<Field> fields;


    @JsonCreator
    public MetricDefinition(@JsonProperty("definitionId") String definitionId,
                            @JsonProperty("metricName") String metricName,
                            @JsonProperty("title") String title,
                            @JsonProperty("grouping") GroupingRule groupingRule,
                            @JsonProperty("preProcess") String preProcess,
                            @JsonProperty("postProcess") String postProcess,
                            @JsonProperty("fields") List<Field> fields) {
        this.definitionId = Objects.requireNonNull(definitionId, "definitionId");
        this.metricName = Objects.requireNonNull(metricName, "metricName");
        this.title = Objects.requireNonNull(title, "title");
        this.groupingRule = ObjectUtils.defaultIfNull(groupingRule, GroupingRule.UNKNOWN);
        this.preProcess = StringUtils.defaultString(preProcess, EmptyPreProcessor.INSTANCE.getName());
        this.postProcess = StringUtils.defaultString(postProcess, EmptyPostProcessor.INSTANCE.getName());
        this.fields = Objects.requireNonNull(fields, "fields");
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getTitle() {
        return title;
    }

    public String getPostProcess() {
        return postProcess;
    }

    public String getPreProcess() {
        return preProcess;
    }

    public List<Field> getFields() {
        return fields;
    }

    public GroupingRule getGroupingRule() {
        return groupingRule;
    }
}
