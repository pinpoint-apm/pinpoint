/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.collector.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFJvmGc;

import java.util.*;

/**
 * @author minwoo.jung
 */
public class TFAgentStatMapper {
    private static final TFCpuLoadMapper tFCpuLoadMapper = new TFCpuLoadMapper();
    private static final TFJvmGcMapper tFJvmGcMapper = new TFJvmGcMapper();
    private static final TFTransactionMapper tFTransactionMapper = new TFTransactionMapper();

    public List<TFAgentStat> map(AgentStatBo agentStatBo) {
        final TreeMap<Long, TFAgentStat> tFAgentStatMap = new TreeMap<>();
        final String agentId = agentStatBo.getAgentId();
        final long startTimestamp = agentStatBo.getStartTimestamp();

        insertTFCpuLoad(tFAgentStatMap, agentStatBo.getCpuLoadBos(), agentId, startTimestamp);
        insertTFJvmGc(tFAgentStatMap, agentStatBo.getJvmGcBos(), agentId, startTimestamp);
        insertTFTransaction(tFAgentStatMap, agentStatBo.getTransactionBos(), agentId, startTimestamp);

        return new ArrayList<>(tFAgentStatMap.values());
    }

    private void insertTFTransaction(TreeMap<Long, TFAgentStat> tFAgentStatMap, List<TransactionBo> transactionBoList, String agentId, long startTimestamp) {
        if (transactionBoList == null) {
            return;
        }

        for (TransactionBo transactionBo : transactionBoList) {
            TFAgentStat tfAgentStat = getOrCreateTFAgentStat(tFAgentStatMap, transactionBo.getTimestamp(), agentId, startTimestamp);
            tfAgentStat.setCollectInterval(transactionBo.getCollectInterval());
            tfAgentStat.setTransaction(tFTransactionMapper.map(transactionBo));
        }
    }

    private void insertTFJvmGc(TreeMap<Long, TFAgentStat> tFAgentStatMap, List<JvmGcBo> jvmGcBoList, String agentId, long startTimestamp) {
        if (jvmGcBoList == null) {
            return;
        }

        for (JvmGcBo jvmGcBo : jvmGcBoList) {
            TFAgentStat tfAgentStat = getOrCreateTFAgentStat(tFAgentStatMap, jvmGcBo.getTimestamp(), agentId, startTimestamp);
            tfAgentStat.setGc(tFJvmGcMapper.map(jvmGcBo));
        }
    }

    private void insertTFCpuLoad(Map<Long, TFAgentStat> tFAgentStatMap, List<CpuLoadBo> cpuLoadBoList, String agentId, long startTimestamp) {
        if (cpuLoadBoList == null) {
            return;
        }

        for (CpuLoadBo cpuLoadBo : cpuLoadBoList) {
            TFAgentStat tFAgentStat = getOrCreateTFAgentStat(tFAgentStatMap, cpuLoadBo.getTimestamp(), agentId, startTimestamp);
            tFAgentStat.setCpuLoad(tFCpuLoadMapper.map(cpuLoadBo));
        }
    }

    private TFAgentStat getOrCreateTFAgentStat(Map<Long, TFAgentStat> tFAgentStatMap, long timestamp, String agentId, long startTimestamp) {
        TFAgentStat tFAgentStat = tFAgentStatMap.get(timestamp);

        if (tFAgentStat == null) {
            tFAgentStat = new TFAgentStat();
            tFAgentStat.setAgentId(agentId);
            tFAgentStat.setStartTimestamp(startTimestamp);
            tFAgentStat.setTimestamp(timestamp);
            // TODO : (minwoo) need to set connectInterval value to use transaction info
            tFAgentStatMap.put(timestamp, tFAgentStat);
        }

        return tFAgentStat;
    }
}
