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
package com.navercorp.pinpoint.exceptiontrace.web.view;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.navercorp.pinpoint.exceptiontrace.web.model.Grouped;
import com.navercorp.pinpoint.exceptiontrace.web.model.GroupedFieldName;
import com.navercorp.pinpoint.exceptiontrace.web.model.params.GroupFilterParams;
import com.navercorp.pinpoint.metric.web.view.TimeSeriesValueView;

import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
public class ExceptionChartValueView implements TimeSeriesValueView, Grouped {

    public static final String TOTAL_FIELDNAME = "total";
    private GroupedFieldName groupedFieldName;
    private List<Integer> values;

    private int rowNum;

    public ExceptionChartValueView() {
    }

    public ExceptionChartValueView(List<Integer> values) {
        this.values = values;
    }

    @Override
    public String getFieldName() {
        if (groupedFieldName == null) {
            return TOTAL_FIELDNAME;
        }

        return Objects.toString(
                groupedFieldName.inAString(rowNum),
                TOTAL_FIELDNAME
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
    public GroupFilterParams getGroupFilterParams() {
        return null;
    }

    @Override
    @JsonIgnore
    public void setGroupFilterParams(GroupFilterParams groupFilterParams) {
        // do nothing
    }

    @Override
    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values;
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
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
