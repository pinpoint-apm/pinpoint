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

package com.navercorp.pinpoint.otlp.common.model;

import com.navercorp.pinpoint.common.server.timeseries.Point;

/**
 * @author minwoo-jung
 */
public interface MetricPoint<Y extends Number> extends Point {
    @Override
    long getTimestamp();

    Y getYVal();

    static MetricPoint<Double> of(long x, double y) {
        return new DoubleMetricPoint(x, y);
    }

    static MetricPoint<Long> of(long x, long y) {
        return new LongMetricPoint(x, y);
    }
}

