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
package com.navercorp.pinpoint.exceptiontrace.web.mapper;

import com.navercorp.pinpoint.common.server.mapper.MapStructUtils;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.exceptiontrace.web.model.ExceptionGroupSummary;
import com.navercorp.pinpoint.exceptiontrace.web.model.GroupedFieldName;
import com.navercorp.pinpoint.exceptiontrace.web.model.params.GroupFilterParams;
import com.navercorp.pinpoint.exceptiontrace.web.util.TimeSeriesUtils;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionChartValueView;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionChartView;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionGroupSummaryView;
import org.mapstruct.AfterMapping;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.util.List;

/**
 * @author intr3p1d
 */
@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {
        }
)
public interface ExceptionModelMapper {

    @Mappings({
            @Mapping(target = "chart", ignore = true),
    })
    ExceptionGroupSummaryView toSummaryView (
            ExceptionGroupSummary summary,
            TimeWindow timeWindow
    );

    @AfterMapping
    default void addChartView(
            ExceptionGroupSummary summary,
            TimeWindow timeWindow,
            @MappingTarget ExceptionGroupSummaryView summaryView
    ) {
        ExceptionChartValueView exceptionChartValueView = new ExceptionChartValueView(summary.getValues());
        exceptionChartValueView.setGroupedFieldName(summary.getGroupedFieldName());

        summaryView.setChart(TimeSeriesUtils.newChartView(
                "summary", timeWindow,
                List.of(exceptionChartValueView)
        ));
    }
}
