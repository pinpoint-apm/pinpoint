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

package com.navercorp.pinpoint.metric.web.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.metric.web.model.basic.metric.group.GroupingRule;

import java.util.List;
import java.util.Objects;

public class Metric {
    private final String name;
    private final String title;
    private final String definitionId;
    private final GroupingRule grouping;
    private final String unit;
    private final List<Field> fields;

    @JsonCreator
    public Metric(@JsonProperty("name") String name,
                  @JsonProperty("title") String title,
                  @JsonProperty("definitionId") String definitionId,
                  @JsonProperty("grouping") GroupingRule grouping,
                  @JsonProperty("unit") String unit,
                  @JsonProperty("fields") List<Field> fields) {
        this.name = Objects.requireNonNull(name, "name");
        this.title = Objects.requireNonNull(title, "title");
        this.definitionId = Objects.requireNonNull(definitionId, "definitionId");
        this.grouping = Objects.requireNonNull(grouping, "grouping");
        this.unit = Objects.requireNonNull(unit, "unit");
        this.fields = Objects.requireNonNull(fields, "fields");
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public GroupingRule getGrouping() {
        return grouping;
    }

    public String getUnit() {
        return unit;
    }

    public List<Field> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return "Metric{" +
                "name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", definitionId='" + definitionId + '\'' +
                ", grouping=" + grouping +
                ", unit='" + unit + '\'' +
                ", fields=" + fields +
                '}';
    }
}
