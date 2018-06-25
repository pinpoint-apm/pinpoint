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

package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceListBo;
import com.navercorp.pinpoint.web.view.DataSourcePointSerializer;
import com.navercorp.pinpoint.web.vo.chart.Point;

/**
 * @author minwoo.jung
 */
@JsonSerialize(using = DataSourcePointSerializer.class)
public class DataSourcePoint implements Point {

    private final long xVal;
    private final int yValForMin;
    private final String agentIdForMin;
    private final int yValForMax;
    private final String agentIdForMax;
    private final int yValForAvg;

    public DataSourcePoint(long xVal, int yValForMin, String agentIdForMin, int yValForMax, String agentIdForMax, int yValForAvg) {
        this.xVal = xVal;
        this.yValForMin = yValForMin;
        this.agentIdForMin = agentIdForMin;
        this.yValForMax = yValForMax;
        this.agentIdForMax = agentIdForMax;
        this.yValForAvg = yValForAvg;
    }

    @Override
    public long getXVal() {
        return xVal;
    }

    public int getYValForMin() {
        return yValForMin;
    }

    public String getAgentIdForMin() {
        return agentIdForMin;
    }

    public int getYValForMax() {
        return yValForMax;
    }

    public String getAgentIdForMax() {
        return agentIdForMax;
    }

    public double getYValForAvg() {
        return yValForAvg;
    }

    public static class UncollectedDataSourcePointCreator implements UncollectedPointCreator<DataSourcePoint> {
        @Override
        public DataSourcePoint createUnCollectedPoint(long xVal) {
            return new DataSourcePoint(xVal, JoinDataSourceBo.UNCOLLECTED_VALUE, JoinDataSourceListBo.UNKNOWN_AGENT, JoinDataSourceBo.UNCOLLECTED_VALUE, JoinDataSourceListBo.UNKNOWN_AGENT, JoinDataSourceBo.UNCOLLECTED_VALUE);
        }

    }
}
