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
import com.navercorp.pinpoint.web.view.AgentScatterDataSerializer;
import com.navercorp.pinpoint.web.vo.TransactionId;
import com.navercorp.pinpoint.web.vo.scatter.Dot;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Taejin Koo
 */
@JsonSerialize(using = AgentScatterDataSerializer.class)
public class AgentScatterData {

    private final Map<ScatterAgentInfo, TransactionAgentScatterData> transactionAgentScatterDataMap = new HashMap<>();

    public AgentScatterData(ScatterAgentInfo agentInfo) {
        if (agentInfo == null) {
            throw new NullPointerException("agentInfo may not be null.");
        }
    }

    void addDot(Coordinates coordinates, Dot dot) {
        if (dot == null) {
            return;
        }

        TransactionId transactionId = dot.getTransactionId();
        ScatterAgentInfo scatterAgentInfo = new ScatterAgentInfo(transactionId.getAgentId(), transactionId.getAgentStartTime());

        TransactionAgentScatterData transactionAgentScatterData = transactionAgentScatterDataMap.get(scatterAgentInfo);
        if (transactionAgentScatterData == null) {
            transactionAgentScatterData = new TransactionAgentScatterData();
            transactionAgentScatterDataMap.put(scatterAgentInfo, transactionAgentScatterData);
        }

        transactionAgentScatterData.addDot(coordinates, dot);
    }

    void merge(AgentScatterData agentScatterData) {
        if (agentScatterData == null) {
            return;
        }

        Map<ScatterAgentInfo, TransactionAgentScatterData> transactionAgentScatterDataMap = agentScatterData.getTransactionAgentScatterDataMap();

        for (Map.Entry<ScatterAgentInfo, TransactionAgentScatterData> entry : transactionAgentScatterDataMap.entrySet()) {
            ScatterAgentInfo key = entry.getKey();

            TransactionAgentScatterData transactionAgentScatterData = this.transactionAgentScatterDataMap.get(key);
            if (transactionAgentScatterData == null) {
                this.transactionAgentScatterDataMap.put(key, entry.getValue());
            } else {
                transactionAgentScatterData.merge(entry.getValue());
            }
        }
    }

    public Map<ScatterAgentInfo, TransactionAgentScatterData> getTransactionAgentScatterDataMap() {
        return transactionAgentScatterDataMap;
    }

    @Override
    public String toString() {
        return "AgentScatterData{" + transactionAgentScatterDataMap + '}';
    }
}
