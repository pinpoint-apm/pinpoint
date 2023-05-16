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

package com.navercorp.pinpoint.metric.web.model.basic.metric.group;

import com.navercorp.pinpoint.metric.common.model.StringPrecondition;
import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */

public class ElementOfBasicGroup {
    private final String metricName;
    private final String fieldName;
    private final List<Tag> tagList;
    private final MatchingRule matchingRule;

    public ElementOfBasicGroup(String metricName, String fieldName, List<Tag> tagList, MatchingRule matchingRule) {
        this.metricName = StringPrecondition.requireHasLength(metricName, "metricName");
        this.fieldName = StringPrecondition.requireHasLength(fieldName, "fieldName");
        this.tagList = Objects.requireNonNull(tagList, "tagList");
        this.matchingRule = Objects.requireNonNull(matchingRule, "matchingRule");
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

    public MatchingRule getMatchingRule() {
        return matchingRule;
    }
}
