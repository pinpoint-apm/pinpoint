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
package com.navercorp.pinpoint.exceptiontrace.web.view;

import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.exceptiontrace.web.model.ErrorSummary;
import com.navercorp.pinpoint.exceptiontrace.web.model.params.GroupFilterParams;
import com.navercorp.pinpoint.exceptiontrace.web.model.GroupedFieldName;
import com.navercorp.pinpoint.exceptiontrace.web.model.params.TransactionSearchParams;
import com.navercorp.pinpoint.exceptiontrace.web.util.TimeSeriesUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
public class ErrorSummaryView {


    private long count;

    private String firstLineOfClassName;
    private String firstLineOfMethodName;

    private long lastTimestamp;

    private TransactionSearchParams lastTransactionSearchParams;
    private GroupedFieldName groupedFieldName;
    private GroupFilterParams groupFilterParams;
    private ExceptionChartView chart;

    public ErrorSummaryView(ErrorSummary errorSummary, TimeWindow timeWindow) {
        Objects.requireNonNull(errorSummary, "errorSummary");

        this.count = errorSummary.getCount();
        this.firstLineOfClassName = errorSummary.getFirstLineOfClassName();
        this.firstLineOfMethodName = errorSummary.getFirstLineOfMethodName();
        this.lastTimestamp = errorSummary.getLastTimestamp();

        this.lastTransactionSearchParams = errorSummary.getLastTransactionSearchParams();
        this.groupedFieldName = errorSummary.getGroupedFieldName();
        this.groupFilterParams = errorSummary.getGroupFilterParams();

        ExceptionChartValueView exceptionChartValueView = new ExceptionChartValueView(errorSummary.getValues());
        exceptionChartValueView.setGroupedFieldName(errorSummary.getGroupedFieldName());

        this.chart = TimeSeriesUtils.newChartView(
                "summary", timeWindow,
                List.of(exceptionChartValueView)
        );
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getFirstLineOfClassName() {
        return firstLineOfClassName;
    }

    public void setFirstLineOfClassName(String firstLineOfClassName) {
        this.firstLineOfClassName = firstLineOfClassName;
    }

    public String getFirstLineOfMethodName() {
        return firstLineOfMethodName;
    }

    public void setFirstLineOfMethodName(String firstLineOfMethodName) {
        this.firstLineOfMethodName = firstLineOfMethodName;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public TransactionSearchParams getLastTransactionSearchParams() {
        return lastTransactionSearchParams;
    }

    public void setLastTransactionSearchParams(TransactionSearchParams lastTransactionSearchParams) {
        this.lastTransactionSearchParams = lastTransactionSearchParams;
    }

    public GroupedFieldName getGroupedFieldName() {
        return groupedFieldName;
    }

    public void setGroupedFieldName(GroupedFieldName groupedFieldName) {
        this.groupedFieldName = groupedFieldName;
    }

    public GroupFilterParams getGroupFilterParams() {
        return groupFilterParams;
    }

    public void setGroupFilterParams(GroupFilterParams groupFilterParams) {
        this.groupFilterParams = groupFilterParams;
    }

    public ExceptionChartView getChart() {
        return chart;
    }

    public void setChart(ExceptionChartView chart) {
        this.chart = chart;
    }
}
