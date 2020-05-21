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

package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTotalThreadCountBo;
import com.navercorp.pinpoint.web.view.TotalThreadCountPointSerializer;
import com.navercorp.pinpoint.web.vo.chart.Point;

@JsonSerialize(using = TotalThreadCountPointSerializer.class)
public class TotalThreadCountPoint implements Point {
    private final long xVal;
    private final long yValForMin;
    private final String  agentIdForMin;
    private final long yValForMax;
    private final String agentIdForMax;
    private final long yValForAvg;

    public TotalThreadCountPoint(long xVal, long yValForMin, String agentIdForMin, long yValForMax, String agentIdForMax, long yValForAvg) {
        this.xVal = xVal;
        this.yValForMin = yValForMin;
        this.agentIdForMin = agentIdForMin;
        this.yValForMax = yValForMax;
        this.agentIdForMax = agentIdForMax;
        this.yValForAvg = yValForAvg;
    }

    @Override
    public long getXVal() { return xVal; }

    public long getYValForMin() { return yValForMin; }

    public String getAgentIdForMin() { return agentIdForMin; }

    public long getYValForMax() { return yValForMax; }

    public String getAgentIdForMax() { return agentIdForMax; }

    public long getYValForAvg() { return yValForAvg; }

    public static class UncollectedTotalThreadCountPointCreator implements UncollectedPointCreator<TotalThreadCountPoint> {

        @Override
        public TotalThreadCountPoint createUnCollectedPoint(long xVal) {
            return new TotalThreadCountPoint(xVal, JoinTotalThreadCountBo.UNCOLLECTED_VALUE,
                    JoinTotalThreadCountBo.UNKNOWN_AGENT, JoinTotalThreadCountBo.UNCOLLECTED_VALUE,
                    JoinTotalThreadCountBo.UNKNOWN_AGENT, JoinTotalThreadCountBo.UNCOLLECTED_VALUE);
        }
    }
}
