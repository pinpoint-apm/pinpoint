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

import com.google.common.primitives.Doubles;
import com.navercorp.pinpoint.common.util.MathUtils;
import com.navercorp.pinpoint.uristat.web.entity.UriStatChartEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriStatSummaryEntity;
import org.mapstruct.Named;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author intr3p1d
 */
public class MapperUtils {
    public static List<List<UriStatSummaryEntity>> groupByUriAndVersion(
            List<UriStatSummaryEntity> entities,
            long originalLimit
    ) {
        // Use LinkedHashMap to preserve the order of the entities as much as possible
        List<List<UriStatSummaryEntity>> grouped = entities.stream()
                .collect(
                        Collectors.groupingBy(
                                entity -> entity.getUri() + entity.getVersion(), LinkedHashMap::new, Collectors.toList()
                        )
                ).values().stream().toList();
        return subList(grouped, originalLimit);
    }

    static <T> List<T> subList(List<T> list, long limit) {
        return list.subList(0, (int) Math.min(list.size(), limit));
    }

    @Named("toApdex")
    public static double toApdex(UriStatSummaryEntity entity) {
        return MathUtils.average(entity.getTotalApdexRaw(), entity.getTotalCount());
    }

    @Named("toAvgTimeMs")
    public static double toAvgTimeMs(UriStatSummaryEntity entity) {
        return MathUtils.average(entity.getSumOfTotalTimeMs(), entity.getTotalCount());
    }

    @Named("toTotalHistogram")
    public static List<Double> toTotalHistogram(UriStatChartEntity entity) {
        return Doubles.asList(entity.toTotalHistogram());
    }

    @Named("toFailureHistogram")
    public static List<Double> toFailureHistogram(UriStatChartEntity entity) {
        return Doubles.asList(entity.toFailureHistogram());
    }

    @Named("toLatency")
    public static List<Double> toLatency(UriStatChartEntity entity) {
        return Doubles.asList((entity.getCount() == 0) ? -1 : (entity.getTotalTimeMs() / entity.getCount()), entity.getMaxLatencyMs());
    }

    @Named("toApdexList")
    public static List<Double> toApdexList(UriStatChartEntity entity) {
        return Doubles.asList((entity.getCount() == 0) ? -1 : (entity.getApdexRaw() / entity.getCount()));
    }
}
