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

import com.navercorp.pinpoint.common.util.MathUtils;
import com.navercorp.pinpoint.uristat.web.entity.UriApdexChartEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriLatencyChartEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriStatChartEntity;
import com.navercorp.pinpoint.uristat.web.entity.UriStatSummaryEntity;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author intr3p1d
 */
public class MapperUtils {
    public static List<List<UriStatSummaryEntity>> groupByUriAndVersion(
            List<UriStatSummaryEntity> entities,
            int originalLimit
    ) {

        Map<Key, List<UriStatSummaryEntity>> groupBy = groupBy(entities, MapperUtils::uriAndVersion);

        return groupBy.values()
                .stream()
                .limit(originalLimit)
                .toList();
    }

    public static <K, V> Map<K, List<V>> groupBy(
            List<V> entities,
            Function<V, K> groupFunction
    ) {
        // Use LinkedHashMap to preserve the order of the entities as much as possible
        Map<K, List<V>> map = new LinkedHashMap<>();
        for (V entity : entities) {
            final K key = groupFunction.apply(entity);
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(entity);
        }
        return map;
    }

    record Key(String uri, String version) {
    }

    private static Key uriAndVersion(UriStatSummaryEntity entity) {
        return new Key(entity.getUri(), entity.getVersion());
    }

    @Named("toApdex")
    public static double toApdex(UriStatSummaryEntity entity) {
        return MathUtils.average(entity.getTotalApdexRaw(), entity.getTotalCount());
    }

    @Named("toAvgTimeMs")
    public static double toAvgTimeMs(UriStatSummaryEntity entity) {
        return MathUtils.average(entity.getSumOfTotalTimeMs(), entity.getTotalCount());
    }

    @Named("toLatency")
    public static List<Double> toLatency(UriStatChartEntity entity) {
        return List.of((entity.getCount() == 0) ? -1 : (entity.getTotalTimeMs() / entity.getCount()), entity.getMaxLatencyMs());
    }

    @Named("toSimpleLatency")
    public static List<Double> toSimpleLatency(UriLatencyChartEntity entity) {
        return List.of((entity.getCount() == 0) ? -1 : (entity.getTotalTimeMs() / entity.getCount()), entity.getMaxLatencyMs());
    }

    @Named("toApdexList")
    public static List<Double> toSimpleApdexList(UriApdexChartEntity entity) {
        return List.of((entity.getCount() == 0) ? -1 : (entity.getApdexRaw() / entity.getCount()));
    }

    @Named("toApdexList")
    public static List<Double> toApdexList(UriStatChartEntity entity) {
        return List.of((entity.getCount() == 0) ? -1 : (entity.getApdexRaw() / entity.getCount()));
    }
}

