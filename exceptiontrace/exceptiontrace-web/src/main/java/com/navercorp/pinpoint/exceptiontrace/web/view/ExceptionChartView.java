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

package com.navercorp.pinpoint.exceptiontrace.web.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.navercorp.pinpoint.metric.web.view.DefaultTimeSeriesView;
import com.navercorp.pinpoint.metric.web.view.TimeseriesValueGroupView;

import java.util.List;

/**
 * @author intr3p1d
 */
public class ExceptionChartView extends DefaultTimeSeriesView {

    private static final String TITLE = "exceptionTrace";

    public ExceptionChartView(List<Long> timestampList, List<TimeseriesValueGroupView> exceptionTrace) {
        super(TITLE, timestampList, exceptionTrace);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getUnit() {
        return null;
    }

}
