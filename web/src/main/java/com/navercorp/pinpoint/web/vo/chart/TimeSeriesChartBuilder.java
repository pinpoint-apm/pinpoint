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

import com.navercorp.pinpoint.web.util.TimeWindow;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author HyunGil Jeong
 * @author minwoo.jung
 */
public class TimeSeriesChartBuilder<P extends Point> {

    private final TimeWindow timeWindow;
    private final Point.UncollectedPointCreator<P> uncollectedPointCreator;

    public TimeSeriesChartBuilder(TimeWindow timeWindow, Point.UncollectedPointCreator<P> uncollectedPointCreator) {
        if (timeWindow.getWindowRangeCount() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("range yields too many timeslots");
        }
        this.timeWindow = timeWindow;
        this.uncollectedPointCreator = Objects.requireNonNull(uncollectedPointCreator, "uncollectedPointCreator");
    }

    public Chart<P> build(List<P> sampledPoints) {
        if (CollectionUtils.isEmpty(sampledPoints)) {
            return new Chart<>(Collections.emptyList());
        }
        List<P> points = createInitialPoints();
        for (P sampledPoint : sampledPoints) {
            int timeslotIndex = this.timeWindow.getWindowIndex(sampledPoint.getXVal());
            if (timeslotIndex < 0 || timeslotIndex >= timeWindow.getWindowRangeCount()) {
                continue;
            }
            points.set(timeslotIndex, sampledPoint);
        }
        return new Chart<>(points);
    }

    public <S> Chart<P> build(List<S> sourceList, Function<S, P> function) {
        Objects.requireNonNull(function, "function");

        List<P> filter = applyFilter(sourceList, function);
        return build(filter);
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

    private List<P> createInitialPoints() {
        int numTimeslots = (int) this.timeWindow.getWindowRangeCount();
        List<P> points = new ArrayList<>(numTimeslots);
        for (long timestamp : this.timeWindow) {
            points.add(uncollectedPointCreator.createUnCollectedPoint(timestamp));
        }
        return points;
    }
}
