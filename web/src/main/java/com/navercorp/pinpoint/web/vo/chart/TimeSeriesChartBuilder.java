/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.web.vo.chart;

import com.navercorp.pinpoint.common.timeseries.point.Point;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * @author HyunGil Jeong
 * @author minwoo.jung
 */
public class TimeSeriesChartBuilder<P extends Point> {

    private final LongFunction<P> function;

    public TimeSeriesChartBuilder(LongFunction<P> function) {
        this.function = Objects.requireNonNull(function, "uncollectedPointCreator");
    }

    public Chart<P> build(TimeWindow timeWindow, List<P> sampledPoints) {
        Objects.requireNonNull(timeWindow, "timeWindow");

        if (CollectionUtils.isEmpty(sampledPoints)) {
            return new Chart<>(Collections.emptyList());
        }

        List<P> points = createInitialPoints(timeWindow, this.function);
        final int windowRangeCount = timeWindow.getWindowRangeCount();
        for (P sampledPoint : sampledPoints) {
            int timeslotIndex = timeWindow.getWindowIndex(sampledPoint.getTimestamp());
            if (timeslotIndex < 0 || timeslotIndex >= windowRangeCount) {
                continue;
            }
            points.set(timeslotIndex, sampledPoint);
        }
        return new Chart<>(points);
    }

    public <S> Chart<P> build(TimeWindow timeWindow, List<S> sourceList, Function<S, P> function) {
        Objects.requireNonNull(function, "function");

        List<P> filter = applyFilter(sourceList, function);
        return build(timeWindow, filter);
    }

    private <S> List<P> applyFilter(List<S> sourceList, Function<S, P> filter) {
        if (CollectionUtils.isEmpty(sourceList)) {
            return Collections.emptyList();
        }

        final List<P> result = new ArrayList<>(sourceList.size());
        for (S source : sourceList) {
            P apply = filter.apply(source);
            result.add(apply);
        }
        return result;
    }

    private List<P> createInitialPoints(TimeWindow timeWindow, LongFunction<P> function) {
        final int numTimeslots = timeWindow.getWindowRangeCount();
        List<P> points = new ArrayList<>(numTimeslots);
        for (long timestamp : timeWindow) {
            points.add(function.apply(timestamp));
        }
        return points;
    }
}
