/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.metric.container;

/**
 * @author Hyunjoon Cho
 */
public interface ContainerMetric {
    double UNCOLLECTED_PERCENT_USAGE = -1D;
    long UNCOLLECTED_MEMORY = -1L;

    ContainerMetric UNSUPPORTED_CONTAINER_METRIC = new ContainerMetric() {

        private final ContainerMetricSnapshot uncollectedSnapshot = new ContainerMetricSnapshot(UNCOLLECTED_PERCENT_USAGE, UNCOLLECTED_PERCENT_USAGE, UNCOLLECTED_MEMORY, UNCOLLECTED_MEMORY);

        @Override
        public ContainerMetricSnapshot getSnapshot() {
            return uncollectedSnapshot;
        }

        @Override
        public String toString() {
            return "Unsupported ContainerMetric";
        }
    };

    ContainerMetricSnapshot getSnapshot();
}
