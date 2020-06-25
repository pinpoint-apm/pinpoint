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

import com.navercorp.pinpoint.web.vo.chart.Point;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public abstract class ApplicationStatPoint<Y extends Number> implements Point {

    private final long xVal;
    private final Y yValForMin;
    private final String agentIdForMin;
    private final Y yValForMax;
    private final String agentIdForMax;
    private final Y yValForAvg;

    public ApplicationStatPoint(long xVal, Y yValForMin, String agentIdForMin, Y yValForMax, String agentIdForMax, Y yValForAvg) {
        this.xVal = xVal;

        this.yValForMin = yValForMin;
        this.agentIdForMin = Objects.requireNonNull(agentIdForMin, "agentIdForMin");

        this.yValForMax = yValForMax;
        this.agentIdForMax = Objects.requireNonNull(agentIdForMax, "agentIdForMax");

        this.yValForAvg = yValForAvg;
    }


    @Override
    public long getXVal() {
        return xVal;
    }

    public Y getYValForMin() {
        return yValForMin;
    }

    public String getAgentIdForMin() {
        return agentIdForMin;
    }

    public Y getYValForMax() {
        return yValForMax;
    }

    public String getAgentIdForMax() {
        return agentIdForMax;
    }

    public Y getYValForAvg() {
        return yValForAvg;
    }

    @Override
    public String toString() {
        return "ApplicationStatPoint{" +
                "xVal=" + xVal +
                ", yValForMin=" + yValForMin +
                ", agentIdForMin='" + agentIdForMin + '\'' +
                ", yValForMax=" + yValForMax +
                ", agentIdForMax='" + agentIdForMax + '\'' +
                ", yValForAvg=" + yValForAvg +
                '}';
    }

}
