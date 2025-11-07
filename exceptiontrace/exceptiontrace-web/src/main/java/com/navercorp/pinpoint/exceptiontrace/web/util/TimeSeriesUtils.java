/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.exceptiontrace.web.util;

import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.exceptiontrace.web.model.ExceptionChartGroup;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionChartValueView;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionChartView;
import com.navercorp.pinpoint.metric.web.view.TimeSeriesValueView;
import com.navercorp.pinpoint.metric.web.view.TimeseriesValueGroupView;

import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
public class TimeSeriesUtils {

    public static ExceptionChartGroup newGroupFromValueViews(
            String groupName,
            List<ExceptionChartValueView> exceptionChartValueViews
    ) {
        List<TimeSeriesValueView> list = (List<TimeSeriesValueView>) (List<? extends TimeSeriesValueView>) exceptionChartValueViews;
        return new ExceptionChartGroup(groupName, list);
    }

    public static ExceptionChartView newChartView(
            String groupName,
            TimeWindow timeWindow,
            List<ExceptionChartValueView> exceptionChartValueViews
    ) {
        Objects.requireNonNull(timeWindow, "timeWindow");
        Objects.requireNonNull(exceptionChartValueViews, "exceptionTraceValueViews");

        List<Long> timestampList = timeWindow.getTimeseriesWindows();
        List<TimeseriesValueGroupView> timeSeriesValueGroupViews = List.of(
                newGroupFromValueViews(groupName, exceptionChartValueViews)
        );

        return new ExceptionChartView(
                timestampList, timeSeriesValueGroupViews
        );
    }
}
