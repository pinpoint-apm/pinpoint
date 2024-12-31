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
package com.navercorp.pinpoint.uristat.web.entity;

/**
 * @author intr3p1d
 */
public class LatencyChartEntity extends ChartCommonEntity {
    private Double totalTimeMs;
    private Double maxLatencyMs;
    private Double count;

    public LatencyChartEntity() {
    }

    public Double getTotalTimeMs() {
        return totalTimeMs;
    }

    public void setTotalTimeMs(Double totalTimeMs) {
        this.totalTimeMs = totalTimeMs;
    }

    public Double getMaxLatencyMs() {
        return maxLatencyMs;
    }

    public void setMaxLatencyMs(Double maxLatencyMs) {
        this.maxLatencyMs = maxLatencyMs;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }
}
