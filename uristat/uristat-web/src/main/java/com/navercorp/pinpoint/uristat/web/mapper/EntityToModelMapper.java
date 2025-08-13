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
package com.navercorp.pinpoint.uristat.web.mapper;

import com.navercorp.pinpoint.uristat.web.entity.UriApdexChartEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriHistogramEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriHistogramFailEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriLatencyChartEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriStatChartEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriStatSummaryEntity;
import com.navercorp.pinpoint.uristat.web.model.UriStatChartValue;
import com.navercorp.pinpoint.uristat.web.model.UriStatSummary;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.function.Function;

/**
 * @author intr3p1d
 */
@Mapper(
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        uses = {MapperUtils.class}
)
public interface EntityToModelMapper {


    @Retention(RetentionPolicy.CLASS)
    @Mapping(target = "apdex", source = "entity", qualifiedByName = "toApdex")
    @Mapping(target = "avgTimeMs", source = "entity", qualifiedByName = "toAvgTimeMs")
    public @interface ToStatSummary {
    }

    @ToStatSummary
    @Mapping(target = "chartValue", ignore = true)
    UriStatSummary toModel(UriStatSummaryEntity entity);

    default UriStatSummary toTotalSummary(List<UriStatSummaryEntity> entity) {
        return mergeSummary(entity, this::toTotalChart);
    }

    default UriStatSummary toFailureSummary(List<UriStatSummaryEntity> entity) {
        return mergeSummary(entity, this::toFailureChart);
    }

    default UriStatSummary toLatencySummary(List<UriStatSummaryEntity> entity) {
        return mergeSummary(entity, this::toLatencyChart);
    }

    default UriStatSummary toApdexSummary(List<UriStatSummaryEntity> entity) {
        return mergeSummary(entity, this::toApdexChart);
    }

    default UriStatSummary mergeSummary(
            List<UriStatSummaryEntity> entities,
            Function<UriStatChartEntity, UriStatChartValue> mapper
    ) {
        UriStatSummaryEntity mergedEntity = entities.get(0);
        List<UriStatChartValue> chartValues = entities.stream()
                .map(mapper)
                .toList();
        UriStatSummary summary = toModel(mergedEntity);
        summary.setChartValue(chartValues);
        return summary;
    }


    @Retention(RetentionPolicy.CLASS)
    @Mapping(target = "timestamp", source = "timestamp")
    @Mapping(target = "version", source = "version")
    public @interface ToChartValue {
    }

    @ToChartValue
    @Named("toTotalChart")
    @Mapping(target = "chartType", constant = "bar")
    @Mapping(target = "unit", constant = "count")
    @Mapping(target = "values", source = "totalHistogram")
    UriStatChartValue toTotalChart(UriHistogramEntity entity);

    @ToChartValue
    @Named("toFailureChart")
    @Mapping(target = "chartType", constant = "bar")
    @Mapping(target = "unit", constant = "count")
    @Mapping(target = "values", source = "failureHistogram")
    UriStatChartValue toFailureChart(UriHistogramFailEntity entity);


    @ToChartValue
    @Named("toTotalChart")
    @Mapping(target = "chartType", constant = "bar")
    @Mapping(target = "unit", constant = "count")
    @Mapping(target = "values", source = "totalHistogram")
    UriStatChartValue toTotalChart(UriStatChartEntity entity);

    @ToChartValue
    @Named("toFailureChart")
    @Mapping(target = "chartType", constant = "bar")
    @Mapping(target = "unit", constant = "count")
    @Mapping(target = "values", source = "totalHistogram")
    UriStatChartValue toFailureChart(UriStatChartEntity entity);

    @ToChartValue
    @Named("toLatencyChart")
    @Mapping(target = "chartType", constant = "line")
    @Mapping(target = "unit", constant = "ms")
    @Mapping(target = "values", source = "entity", qualifiedByName = "toSimpleLatency")
    UriStatChartValue toSimpleLatencyChart(UriLatencyChartEntity entity);

    @ToChartValue
    @Named("toLatencyChart")
    @Mapping(target = "chartType", constant = "line")
    @Mapping(target = "unit", constant = "ms")
    @Mapping(target = "values", source = "entity", qualifiedByName = "toLatency")
    UriStatChartValue toLatencyChart(UriStatChartEntity entity);


    @ToChartValue
    @Named("toApdexChart")
    @Mapping(target = "chartType", constant = "line")
    @Mapping(target = "unit", constant = "")
    @Mapping(target = "values", source = "entity", qualifiedByName = "toApdexList")
    UriStatChartValue toSimpleApdexChart(UriApdexChartEntity entity);

    @ToChartValue
    @Named("toApdexChart")
    @Mapping(target = "chartType", constant = "line")
    @Mapping(target = "unit", constant = "")
    @Mapping(target = "values", source = "entity", qualifiedByName = "toApdexList")
    UriStatChartValue toApdexChart(UriStatChartEntity entity);

}
