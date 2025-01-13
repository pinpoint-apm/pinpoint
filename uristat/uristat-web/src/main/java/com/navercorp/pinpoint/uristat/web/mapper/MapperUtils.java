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
import com.navercorp.pinpoint.uristat.web.model.UriStatChartValue;
import org.mapstruct.Named;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
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
    public static Double toApdex(UriStatSummaryEntity entity) {
        return MathUtils.average(entity.getApdexRaw(), entity.getTotalCount());
    }

    @Named("toAvgTimeMs")
    public static Double toAvgTimeMs(UriStatSummaryEntity entity) {
        return MathUtils.average(entity.getTotalTimeMs(), entity.getTotalCount());
    }

    @Named("toTotalHistogram")
    public static List<Double> toTotalHistogram(UriStatChartEntity entity) {
        return toHistogram(
                entity.getTot0(), entity.getTot1(), entity.getTot2(), entity.getTot3(),
                entity.getTot4(), entity.getTot5(), entity.getTot6(), entity.getTot7()
        );
    }

    @Named("toFailureHistogram")
    public static List<Double> toFailureHistogram(UriStatChartEntity entity) {
        return toHistogram(
                entity.getFail0(), entity.getFail1(), entity.getFail2(), entity.getFail3(),
                entity.getFail4(), entity.getFail5(), entity.getFail6(), entity.getFail7()
        );
    }

    public static List<Double> toHistogram(
            Double hist0, Double hist1, Double hist2, Double hist3,
            Double hist4, Double hist5, Double hist6, Double hist7
    ) {
        return Doubles.asList(hist0, hist1, hist2, hist3, hist4, hist5, hist6, hist7);
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
