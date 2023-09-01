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
 *
 */

package com.navercorp.pinpoint.exceptiontrace.web.model;

import com.navercorp.pinpoint.metric.web.view.TimeSeriesValueView;
import org.junit.jupiter.api.Test;

import java.util.List;


class ExceptionTraceGroupTest {

    @Test
    void newGroupFromValueViews_casting() {
        List<ExceptionTraceValueView> view = List.of(
                new ExceptionTraceValueView(List.of(1, 2, 3)),
                new ExceptionTraceValueView(List.of(4, 5, 6))
        );
        ExceptionTraceGroup group = ExceptionTraceGroup.newGroupFromValueViews("test", view);
        @SuppressWarnings("unused")
        List<TimeSeriesValueView> metricValues = group.getMetricValues();
    }
}