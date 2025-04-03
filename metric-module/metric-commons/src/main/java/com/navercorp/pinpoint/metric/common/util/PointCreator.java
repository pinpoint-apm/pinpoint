/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.metric.common.util;

import com.navercorp.pinpoint.common.timeseries.point.DataPoint;
import com.navercorp.pinpoint.common.timeseries.point.Points;
import com.navercorp.pinpoint.metric.common.model.chart.AvgMinMaxMetricPoint;
import com.navercorp.pinpoint.metric.common.model.chart.AvgMinMetricPoint;
import com.navercorp.pinpoint.metric.common.model.chart.MinMaxMetricPoint;

/**
 * @author minwoo.jung
 */
public class PointCreator {
    public static final long UNCOLLECTED_LONG = -1L;
    public static final double UNCOLLECTED_DOUBLE = -1D;


    public static DataPoint<Double> doublePoint(long xVal) {
        return Points.of(xVal, UNCOLLECTED_DOUBLE);
    }

    public static DataPoint<Long> longPoint(long xVal) {
        return Points.of(xVal, UNCOLLECTED_LONG);
    }

    //------------

    public static AvgMinMaxMetricPoint createAvgMinMaxMetricPoint(long xVal) {
        return new AvgMinMaxMetricPoint(xVal, UNCOLLECTED_DOUBLE, UNCOLLECTED_DOUBLE, UNCOLLECTED_DOUBLE);
    }

    public static MinMaxMetricPoint createMinMaxMetricPoint(long xVal) {
        return new MinMaxMetricPoint(xVal, UNCOLLECTED_DOUBLE, UNCOLLECTED_DOUBLE);
    }

    public static AvgMinMetricPoint createAvgMinMetricPoint(long xVal) {
        return new AvgMinMetricPoint(xVal, UNCOLLECTED_DOUBLE, UNCOLLECTED_DOUBLE);
    }


}
