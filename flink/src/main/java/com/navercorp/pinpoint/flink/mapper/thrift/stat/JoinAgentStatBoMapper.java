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

import com.navercorp.pinpoint.common.server.bo.stat.join.*;
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

    private final JoinCpuLoadBoMapper joinCpuLoadBoMapper = new JoinCpuLoadBoMapper();
    private final JoinMemoryBoMapper joinMemoryBoMapper = new JoinMemoryBoMapper();
    private final JoinTransactionBoMapper joinTransactionBoMapper = new JoinTransactionBoMapper();
    private final JoinActiveTraceBoMapper joinActiveTraceBoMapper = new JoinActiveTraceBoMapper();
    private final JoinResponseTimeBoMapper joinResponseTimeBoMapper = new JoinResponseTimeBoMapper();
    private final JoinDataSourceListBoMapper joinDataSourceListBoMapper = new JoinDataSourceListBoMapper();
    private final JoinFileDescriptorBoMapper joinFileDescriptorBoMapper = new JoinFileDescriptorBoMapper();
    private final JoinDirectBufferBoMapper joinDirectBufferBoMapper = new JoinDirectBufferBoMapper();

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
        List<JoinTransactionBo> joinTransactionBoList = new ArrayList<>(agentStatSize);
        List<JoinActiveTraceBo> joinActiveTraceBoList = new ArrayList<>(agentStatSize);
        List<JoinResponseTimeBo> joinResponseTimeBoList = new ArrayList<>(agentStatSize);
        List<JoinDataSourceListBo> joinDataSourceListBoList = new ArrayList<>(agentStatSize);
        List<JoinFileDescriptorBo> joinFileDescriptorBoList = new ArrayList<>(agentStatSize);
        List<JoinDirectBufferBo> joinDirectBufferBoList = new ArrayList<>(agentStatSize);

        for (TFAgentStat tFAgentStat : tFAgentStatBatch.getAgentStats()) {
            createAndAddJoinCpuLoadBo(tFAgentStat, joinCpuLoadBoList);
            createAndAddJoinMemoryBo(tFAgentStat, joinMemoryBoList);
            createAndAddJoinTransactionBo(tFAgentStat, joinTransactionBoList);
            createAndAddJoinActiveTraceBo(tFAgentStat, joinActiveTraceBoList);
            createAndAddJoinResponseTimeBo(tFAgentStat, joinResponseTimeBoList);
            createAndAddJoinDataSourceListBo(tFAgentStat, joinDataSourceListBoList);
            createAndAddJoinFileDescriptorBo(tFAgentStat, joinFileDescriptorBoList);
            createAndAddJoinDirectBufferBo(tFAgentStat, joinDirectBufferBoList);
        }

        joinAgentStatBo.setJoinCpuLoadBoList(joinCpuLoadBoList);
        joinAgentStatBo.setJoinMemoryBoList(joinMemoryBoList);
        joinAgentStatBo.setJoinTransactionBoList(joinTransactionBoList);
        joinAgentStatBo.setJoinActiveTraceBoList(joinActiveTraceBoList);
        joinAgentStatBo.setJoinResponseTimeBoList(joinResponseTimeBoList);
        joinAgentStatBo.setJoinDataSourceListBoList(joinDataSourceListBoList);
        joinAgentStatBo.setJoinFileDescriptorBoList(joinFileDescriptorBoList);
        joinAgentStatBo.setJoinDirectBufferBoList(joinDirectBufferBoList);
        joinAgentStatBo.setId(tFAgentStatBatch.getAgentId());
        joinAgentStatBo.setAgentStartTimestamp(tFAgentStatBatch.getStartTimestamp());
        joinAgentStatBo.setTimestamp(getTimeStamp(joinAgentStatBo));
        return joinAgentStatBo;
    }

    private void createAndAddJoinDataSourceListBo(TFAgentStat tFAgentStat, List<JoinDataSourceListBo> joinDataSourceListBoList) {
        JoinDataSourceListBo joinDataSourceListBo = joinDataSourceListBoMapper.map(tFAgentStat);

        if (joinDataSourceListBo == JoinDataSourceListBo.EMPTY_JOIN_DATA_SOURCE_LIST_BO) {
            return;
        }

        joinDataSourceListBoList.add(joinDataSourceListBo);
    }

    private void createAndAddJoinResponseTimeBo(TFAgentStat tFAgentStat, List<JoinResponseTimeBo> joinResponseTimeBoList) {
        JoinResponseTimeBo joinResponseTimeBo = joinResponseTimeBoMapper.map(tFAgentStat);

        if (joinResponseTimeBo == joinResponseTimeBo.EMPTY_JOIN_RESPONSE_TIME_BO) {
            return;
        }

        joinResponseTimeBoList.add(joinResponseTimeBo);
    }

    private void createAndAddJoinActiveTraceBo(TFAgentStat tFAgentStat, List<JoinActiveTraceBo> joinActiveTraceBoList) {
        JoinActiveTraceBo joinActiveTraceBo = joinActiveTraceBoMapper.map(tFAgentStat);

        if (joinActiveTraceBo == joinActiveTraceBo.EMPTY_JOIN_ACTIVE_TRACE_BO) {
            return;
        }

        joinActiveTraceBoList.add(joinActiveTraceBo);
    }

    private void createAndAddJoinTransactionBo(TFAgentStat tFAgentStat, List<JoinTransactionBo> joinTransactionBoList) {
        JoinTransactionBo joinTransactionBo = joinTransactionBoMapper.map(tFAgentStat);

        if (joinTransactionBo == JoinTransactionBo.EMPTY_JOIN_TRANSACTION_BO) {
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

        List<JoinActiveTraceBo> joinActiveTraceBoList = joinAgentStatBo.getJoinActiveTraceBoList();

        if (joinActiveTraceBoList.size() != 0) {
            return joinActiveTraceBoList.get(0).getTimestamp();
        }

        List<JoinResponseTimeBo> joinResponseTimeBoList = joinAgentStatBo.getJoinResponseTimeBoList();

        if (joinResponseTimeBoList.size() != 0) {
            return joinResponseTimeBoList.get(0).getTimestamp();
        }

        List<JoinDataSourceListBo> joinDataSourceListBoList = joinAgentStatBo.getJoinDataSourceListBoList();

        if (joinDataSourceListBoList.size() != 0) {
            return joinDataSourceListBoList.get(0).getTimestamp();
        }

        List<JoinFileDescriptorBo> joinFileDescriptorBoList = joinAgentStatBo.getJoinFileDescriptorBoList();

        if (joinFileDescriptorBoList.size() != 0) {
            return joinFileDescriptorBoList.get(0).getTimestamp();
        }

        List<JoinDirectBufferBo> joinDirectBufferBoList = joinAgentStatBo.getJoinDirectBufferBoList();

        if (joinDirectBufferBoList.size() != 0) {
            return joinDirectBufferBoList.get(0).getTimestamp();
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

    public void createAndAddJoinFileDescriptorBo(TFAgentStat tFAgentStat, List<JoinFileDescriptorBo> joinFileDescriptorBoList) {
        JoinFileDescriptorBo joinFileDescriptorBo = joinFileDescriptorBoMapper.map(tFAgentStat);

        if (joinFileDescriptorBo == JoinFileDescriptorBo.EMPTY_JOIN_FILE_DESCRIPTOR_BO) {
            return;
        }

        joinFileDescriptorBoList.add(joinFileDescriptorBo);
    }

    public void createAndAddJoinDirectBufferBo(TFAgentStat tFAgentStat, List<JoinDirectBufferBo> joinDirectBufferBoList) {
        JoinDirectBufferBo joinDirectBufferBo = joinDirectBufferBoMapper.map(tFAgentStat);

        if (joinDirectBufferBo == JoinDirectBufferBo.EMPTY_JOIN_DIRECT_BUFFER_BO) {
            return;
        }

        joinDirectBufferBoList.add(joinDirectBufferBo);
    }
}
