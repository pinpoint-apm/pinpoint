/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.scatter;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.view.ScatterDataSerializer;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Taejin Koo
 */
@JsonSerialize(using = ScatterDataSerializer.class)
public class ScatterData {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private final long from;
    private final int xGroupUnitMillis;
    private final int yGroupUnitMillis;

    private final Map<ScatterAgentInfo, AgentScatterData> agentScatterDataMap = new HashMap<>();
    private long oldestAcceptedTime = Long.MAX_VALUE;
    private long latestAcceptedTime = Long.MIN_VALUE;

    public ScatterData(long from, int xGroupUnitMillis, int yGroupUnitMillis) {
        this.from = from;
        this.xGroupUnitMillis = xGroupUnitMillis;
        this.yGroupUnitMillis = yGroupUnitMillis;
    }

    public void addDot(Dot dot) {
        if (dot == null) {
            return;
        }

        ScatterAgentInfo agentInfo = new ScatterAgentInfo(dot.getAgentId());
        AgentScatterData agentScatterData = agentScatterDataMap.get(agentInfo);
        if (agentScatterData == null) {
            agentScatterData = new AgentScatterData(agentInfo);
            agentScatterDataMap.put(agentInfo, agentScatterData);
        }

        long acceptedTimeDiff = dot.getAcceptedTime() - from;
        long x = acceptedTimeDiff - (acceptedTimeDiff  % xGroupUnitMillis);
        if (x < 0) {
            x = 0L;
        }
        int y = dot.getElapsedTime() - (dot.getElapsedTime() % yGroupUnitMillis);

        Coordinates coordinates = new Coordinates(x, y);
        agentScatterData.addDot(coordinates, new Dot(dot.getTransactionId(), acceptedTimeDiff, dot.getElapsedTime(), dot.getExceptionCode(), dot.getAgentId()));
        if (oldestAcceptedTime > dot.getAcceptedTime()) {
            oldestAcceptedTime = dot.getAcceptedTime();
        }

        if (latestAcceptedTime < dot.getAcceptedTime()) {
            latestAcceptedTime = dot.getAcceptedTime();
        }
    }

    public void merge(ScatterData scatterData) {
        if (scatterData == null) {
            return;
        }

        Map<ScatterAgentInfo, AgentScatterData> agentScatterDataMap = scatterData.getAgentScatterDataMap();

        for (Map.Entry<ScatterAgentInfo, AgentScatterData> entry : agentScatterDataMap.entrySet()) {
            ScatterAgentInfo key = entry.getKey();

            AgentScatterData agentScatterData = this.agentScatterDataMap.get(key);
            if (agentScatterData == null) {
                this.agentScatterDataMap.put(key, entry.getValue());
            } else {
                agentScatterData.merge(entry.getValue());
            }
        }

        if (oldestAcceptedTime > scatterData.getOldestAcceptedTime()) {
            oldestAcceptedTime = scatterData.getOldestAcceptedTime();
        }

        if (latestAcceptedTime < scatterData.getLatestAcceptedTime()) {
            latestAcceptedTime = scatterData.getLatestAcceptedTime();
        }
    }

    public Map<ScatterAgentInfo, AgentScatterData> getAgentScatterDataMap() {
        return agentScatterDataMap;
    }

    public long getOldestAcceptedTime() {
        return oldestAcceptedTime;
    }

    public long getLatestAcceptedTime() {
        return latestAcceptedTime;
    }

}
