/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.web.vo.stat.chart;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinMemoryBo;

/**
 * @author minwoo.jung
 */
public class MemoryPoint implements Point {

    private final long xVal;
    private final double yValForMin;
    private final String  agentIdForMin;
    private final double yValForMax;
    private final String agentIdForMax;
    private final double yValForAvg;

    public MemoryPoint(long xVal, double yValForMin, String agentIdForMin, double yValForMax, String agentIdForMax, double yValForAvg) {
        this.xVal = xVal;
        this.yValForMin = yValForMin;
        this.agentIdForMin = agentIdForMin;
        this.yValForMax = yValForMax;
        this.agentIdForMax = agentIdForMax;
        this.yValForAvg = yValForAvg;
    }

    public double getyValForMin() {
        return yValForMin;
    }

    public String getAgentIdForMin() {
        return agentIdForMin;
    }

    public double getyValForMax() {
        return yValForMax;
    }

    public String getAgentIdForMax() {
        return agentIdForMax;
    }

    public double getyValForAvg() {
        return yValForAvg;
    }

    @Override
    public long getxVal() {
        return this.xVal;
    }

    public static class UncollectedMemoryPointCreater implements UncollectedPointCreater {
        public Point createUnCollectedPoint(long xVal) {
            return new MemoryPoint(xVal, JoinMemoryBo.UNCOLLECTED_VALUE, JoinMemoryBo.UNKNOWN_AGENT, JoinMemoryBo.UNCOLLECTED_VALUE, JoinMemoryBo.UNKNOWN_AGENT, JoinMemoryBo.UNCOLLECTED_VALUE);
        }
    }
}
