/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.exceptiontrace.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.metric.web.view.TimeSeriesValueView;

import java.util.List;

/**
 * @author intr3p1d
 */
public class ExceptionTraceValueView implements TimeSeriesValueView {

    private static final String TOTAL_FIELDNAME = "total";
    private static final String EMPTY_STRING = "(empty error message)";
    private GroupedFieldName groupedFieldName;
    private List<Integer> values;

    public ExceptionTraceValueView() {
    }

    public ExceptionTraceValueView(List<Integer> values) {
        this.values = values;
    }

    @Override
    public String getFieldName() {
        if (groupedFieldName == null) {
            return TOTAL_FIELDNAME;
        }
        return StringUtils.defaultIfEmpty(
                StringUtils.defaultString(groupedFieldName.inAString(), TOTAL_FIELDNAME), EMPTY_STRING
        );
    }

    @JsonIgnore
    public GroupedFieldName getGroupedFieldName() {
        return groupedFieldName;
    }

    public void setGroupedFieldName(GroupedFieldName groupedFieldName) {
        this.groupedFieldName = groupedFieldName;
    }

    @Override
    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values;
    }

    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> getTags() {
        return null;
    }

    @Override
    public String toString() {
        return "ExceptionTraceValueView{" +
                "values=" + values +
                '}';
    }
}
