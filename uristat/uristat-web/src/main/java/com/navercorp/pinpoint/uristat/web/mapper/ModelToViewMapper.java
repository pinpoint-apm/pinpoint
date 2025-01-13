/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.uristat.web.mapper;

import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.uristat.web.chart.UriStatChartType;
import com.navercorp.pinpoint.uristat.web.model.UriStatSummary;
import com.navercorp.pinpoint.uristat.web.view.UriStatSummaryView;
import com.navercorp.pinpoint.uristat.web.view.UriStatView;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author intr3p1d
 */
@Mapper(
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        uses = {MapperUtils.class}
)
public interface ModelToViewMapper {

    default UriStatSummaryView toSummaryView(
            UriStatSummary model, TimeWindow timeWindow, UriStatChartType chartType
    ) {
        UriStatView uriStatView = toChartView(model, timeWindow, chartType);
        UriStatSummaryView miniChartView = this.toSummaryView(model);
        miniChartView.setChart(uriStatView);
        return miniChartView;
    }

    @Mapping(target = "chart", ignore = true)
    UriStatSummaryView toSummaryView(UriStatSummary model);

    default UriStatView toChartView(
            UriStatSummary model, TimeWindow timeWindow, UriStatChartType chartType
    ) {
        return new UriStatView(model.getUri(), timeWindow, model.getChartValue(), chartType);
    }
}
