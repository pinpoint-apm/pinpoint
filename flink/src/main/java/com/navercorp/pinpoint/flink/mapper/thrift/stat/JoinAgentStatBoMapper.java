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

package com.navercorp.pinpoint.flink.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinAgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinMemoryBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;
import com.navercorp.pinpoint.flink.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStatBatch;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class JoinAgentStatBoMapper implements ThriftBoMapper<JoinAgentStatBo, TFAgentStatBatch> {

    private static JoinCpuLoadBoMapper joinCpuLoadBoMapper = new JoinCpuLoadBoMapper();
    private static JoinMemoryBoMapper joinMemoryBoMapper = new JoinMemoryBoMapper();
    private static JoinTransactionBoMapper joinTransactionBoMapper = new JoinTransactionBoMapper();

    @Override
    public JoinAgentStatBo map(TFAgentStatBatch tFAgentStatBatch) {
        if (!tFAgentStatBatch.isSetAgentStats()) {
            return JoinAgentStatBo.EMPTY_JOIN_AGENT_STAT_BO;
        }

        if (StringUtils.isEmpty(tFAgentStatBatch.getAgentId())) {
            return JoinAgentStatBo.EMPTY_JOIN_AGENT_STAT_BO;
        }

        JoinAgentStatBo joinAgentStatBo = new JoinAgentStatBo();
        int agentStatSize = tFAgentStatBatch.getAgentStats().size();
        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<>(agentStatSize);
        List<JoinMemoryBo> joinMemoryBoList = new ArrayList<>(agentStatSize);
        List<JoinTransactionBo> JoinTransactionBoList = new ArrayList<>(agentStatSize);
        for (TFAgentStat tFAgentStat : tFAgentStatBatch.getAgentStats()) {
            createAndAddJoinCpuLoadBo(tFAgentStat, joinCpuLoadBoList);
            createAndAddJoinMemoryBo(tFAgentStat, joinMemoryBoList);
            createAndAddJoinTransactionBo(tFAgentStat, JoinTransactionBoList);
        }

        joinAgentStatBo.setJoinCpuLoadBoList(joinCpuLoadBoList);
        joinAgentStatBo.setJoinMemoryBoList(joinMemoryBoList);
        joinAgentStatBo.setJoinTransactionBoList(JoinTransactionBoList);
        joinAgentStatBo.setId(tFAgentStatBatch.getAgentId());
        joinAgentStatBo.setAgentStartTimestamp(tFAgentStatBatch.getStartTimestamp());
        joinAgentStatBo.setTimestamp(getTimeStamp(joinAgentStatBo));
        return joinAgentStatBo;
    }

    private void createAndAddJoinTransactionBo(TFAgentStat tFAgentStat, List<JoinTransactionBo> joinTransactionBoList) {
        JoinTransactionBo joinTransactionBo = joinTransactionBoMapper.map(tFAgentStat);

        if (joinTransactionBo == JoinTransactionBo.EMPTY_TRANSACTION_BO) {
            return;
        }

        joinTransactionBoList.add(joinTransactionBo);
    }

    private long getTimeStamp(JoinAgentStatBo joinAgentStatBo) {
        List<JoinCpuLoadBo> joinCpuLoadBoList = joinAgentStatBo.getJoinCpuLoadBoList();

        if (joinCpuLoadBoList.size() != 0) {
            return joinCpuLoadBoList.get(0).getTimestamp();
        }

        List<JoinMemoryBo> joinMemoryBoList = joinAgentStatBo.getJoinMemoryBoList();

        if (joinMemoryBoList.size() != 0) {
            return joinMemoryBoList.get(0).getTimestamp();
        }

        List<JoinTransactionBo> joinTransactionBoList = joinAgentStatBo.getJoinTransactionBoList();

        if (joinTransactionBoList.size() != 0) {
            return joinTransactionBoList.get(0).getTimestamp();
        }

        return Long.MIN_VALUE;
    }

    public void createAndAddJoinCpuLoadBo(TFAgentStat tFAgentStat, List<JoinCpuLoadBo> joinCpuLoadBoList) {
        JoinCpuLoadBo joinCpuLoadBo = joinCpuLoadBoMapper.map(tFAgentStat);

        if (joinCpuLoadBo == JoinCpuLoadBo.EMPTY_JOIN_CPU_LOAD_BO) {
            return;
        }

        joinCpuLoadBoList.add(joinCpuLoadBo);
    }

    private void createAndAddJoinMemoryBo(TFAgentStat tFAgentStat, List<JoinMemoryBo> joinMemoryBoList) {
        JoinMemoryBo joinMemoryBo = joinMemoryBoMapper.map(tFAgentStat);

        if (joinMemoryBo == JoinMemoryBo.EMPTY_JOIN_MEMORY_BO) {
            return;
        }

        joinMemoryBoList.add(joinMemoryBo);
    }

}
