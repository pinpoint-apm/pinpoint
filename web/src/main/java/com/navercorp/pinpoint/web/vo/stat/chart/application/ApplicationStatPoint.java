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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.web.vo.chart.Point;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ApplicationStatPoint implements Point {

    private final long xVal;
    private final double yValForMin;
    private final String agentIdForMin;
    private final double yValForMax;
    private final String agentIdForMax;
    private final double yValForAvg;

    public ApplicationStatPoint(long xVal, double yValForMin, String agentIdForMin, double yValForMax, String agentIdForMax, double yValForAvg) {
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
                "xVal=" + xVal +
                ", yValForMin=" + yValForMin +
                ", agentIdForMin='" + agentIdForMin + '\'' +
                ", yValForMax=" + yValForMax +
                ", agentIdForMax='" + agentIdForMax + '\'' +
                ", yValForAvg=" + yValForAvg +
                '}';
    }

    public static final double UNCOLLECTED_VALUE = -1L;

    public static class UncollectedCreator implements UncollectedPointCreator<ApplicationStatPoint> {

        private final double uncollectedValue;

        public UncollectedCreator() {
            this(UNCOLLECTED_VALUE);
        }

        public UncollectedCreator(double uncollectedValue) {
            this.uncollectedValue = uncollectedValue;
        }

        @Override
        public ApplicationStatPoint createUnCollectedPoint(long xVal) {
            return new ApplicationStatPoint(xVal, uncollectedValue,
                    JoinStatBo.UNKNOWN_AGENT, uncollectedValue,
                    JoinStatBo.UNKNOWN_AGENT, uncollectedValue);
        }

    }


}
