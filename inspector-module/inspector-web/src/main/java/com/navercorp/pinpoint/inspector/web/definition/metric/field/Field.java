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

package com.navercorp.pinpoint.inspector.web.definition.metric.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.inspector.web.definition.AggregationFunction;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.TagUtils;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.MatchingRule;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class Field {

    private final String fieldName;
    private final String fieldAlias;
    private final List<Tag> tags;
    private final MatchingRule matchingRule;
    private final AggregationFunction aggregationFunction;
    private final String chartType;
    private final String unit;
    private final String postProcess;


    @JsonCreator
    public Field(@JsonProperty("fieldName") String fieldName,
                 @JsonProperty("fieldAlias") String fieldAlias,
                 @JsonProperty("tags") List<Tag> tags,
                 @JsonProperty("matchingRule") MatchingRule matchingRule,
                 @JsonProperty("aggregationFunction") AggregationFunction aggregationFunction,
                 @JsonProperty("chartType") String chartType,
                 @JsonProperty("unit") String unit,
                 @JsonProperty("postProcess") String postProcess){
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName");
        this.fieldAlias = StringUtils.defaultString(fieldAlias, fieldName);
        this.tags = TagUtils.defaultTags(tags);
        this.matchingRule = Objects.requireNonNull(matchingRule, "matchingRule");
        this.aggregationFunction = Objects.requireNonNull(aggregationFunction, "aggregationFunction");
        this.chartType = StringUtils.defaultString(chartType, "");
        this.unit = StringUtils.defaultString(unit, "");
        this.postProcess = StringUtils.defaultString(postProcess, EmptyPostProcessor.INSTANCE.getName());
    }


    public String getFieldAlias() {
        return fieldAlias;
    }

    public String getFieldName() {
        return fieldName;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public MatchingRule getMatchingRule() {
        return matchingRule;
    }

    public AggregationFunction getAggregationFunction() {
        return aggregationFunction;
    }

    public String getChartType() {
        return chartType;
    }

    public String getUnit() {
        return unit;
    }

    public String getPostProcess() {
        return postProcess;
    }
}
