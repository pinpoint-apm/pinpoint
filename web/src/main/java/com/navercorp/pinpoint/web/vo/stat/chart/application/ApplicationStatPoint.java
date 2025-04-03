/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.common.server.timeseries.Point;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ApplicationStatPoint implements Point {

    private final long timestamp;
    private final double yValForMin;
    private final String agentIdForMin;
    private final double yValForMax;
    private final String agentIdForMax;
    private final double yValForAvg;

    public ApplicationStatPoint(long timestamp, double yValForMin, String agentIdForMin, double yValForMax, String agentIdForMax, double yValForAvg) {
        this.timestamp = timestamp;

        this.yValForMin = yValForMin;
        this.agentIdForMin = Objects.requireNonNull(agentIdForMin, "agentIdForMin");

        this.yValForMax = yValForMax;
        this.agentIdForMax = Objects.requireNonNull(agentIdForMax, "agentIdForMax");

        this.yValForAvg = yValForAvg;
    }


    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public double getYValForMin() {
        return yValForMin;
    }

    public String getAgentIdForMin() {
        return agentIdForMin;
    }

    public double getYValForMax() {
        return yValForMax;
    }

    public String getAgentIdForMax() {
        return agentIdForMax;
    }

    public double getYValForAvg() {
        return yValForAvg;
    }

    @Override
    public String toString() {
        return "ApplicationStatPoint{" +
                "xVal=" + timestamp +
                ", yValForMin=" + yValForMin +
                ", agentIdForMin='" + agentIdForMin + '\'' +
                ", yValForMax=" + yValForMax +
                ", agentIdForMax='" + agentIdForMax + '\'' +
                ", yValForAvg=" + yValForAvg +
                '}';
    }
}
