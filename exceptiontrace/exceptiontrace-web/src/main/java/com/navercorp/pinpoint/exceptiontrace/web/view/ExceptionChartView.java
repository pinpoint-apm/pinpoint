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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.navercorp.pinpoint.metric.web.view.TimeSeriesView;
import com.navercorp.pinpoint.metric.web.view.TimeseriesValueGroupView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author intr3p1d
 */
public class ExceptionChartView implements TimeSeriesView {

    private static final String TITLE = "exceptionTrace";

    private final List<Long> timestampList;

    private final List<TimeseriesValueGroupView> exceptionTrace = new ArrayList<>();

    public ExceptionChartView(List<Long> timestampList, List<TimeseriesValueGroupView> exceptionTraces) {
        this.timestampList = timestampList;
        this.exceptionTrace.addAll(exceptionTraces);
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getUnit() {
        return null;
    }

    @Override
    public List<Long> getTimestamp() {
        return timestampList;
    }

    @Override
    public List<TimeseriesValueGroupView> getMetricValueGroups() {
        return this.exceptionTrace;
    }
}
