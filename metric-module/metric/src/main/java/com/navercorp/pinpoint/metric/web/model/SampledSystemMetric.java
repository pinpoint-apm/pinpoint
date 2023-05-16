/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.model;

import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.model.chart.Point;
import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricPoint;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
public class SampledSystemMetric<T extends Number> {
    public static final Number UNCOLLECTED_VALUE = -1;
    public static final Point.UncollectedPointCreator<SystemMetricPoint<Number>> UNCOLLECTED_POINT_CREATOR = new Point.UncollectedPointCreator<SystemMetricPoint<Number>>() {
        @Override
        public SystemMetricPoint<Number> createUnCollectedPoint(long xVal) {
            return new SystemMetricPoint<>(xVal, UNCOLLECTED_VALUE);
        }
    };

    private final SystemMetricPoint<T> systemMetricPoint;
    private final List<Tag> tags;

    public SampledSystemMetric(SystemMetricPoint<T> systemMetricPoint, List<Tag> tags) {
        this.systemMetricPoint = Objects.requireNonNull(systemMetricPoint, "systemMetricPoint");
        this.tags = Objects.requireNonNull(tags, "tags");
    }

    public SystemMetricPoint<T>  getPoint() {
        return systemMetricPoint;
    }

    public List<Tag> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledSystemMetric{");
        sb.append("systemMetricPoint=").append(systemMetricPoint);
        sb.append(", tags=").append(tags);
        sb.append('}');
        return sb.toString();
    }
}
