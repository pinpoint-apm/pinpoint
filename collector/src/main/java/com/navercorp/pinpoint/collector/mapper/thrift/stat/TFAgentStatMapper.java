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

import com.navercorp.pinpoint.common.server.bo.stat.*;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;

import java.util.*;

/**
 * @author minwoo.jung
 */
public class TFAgentStatMapper {
    private static final TFCpuLoadMapper tFCpuLoadMapper = new TFCpuLoadMapper();
    private static final TFJvmGcMapper tFJvmGcMapper = new TFJvmGcMapper();
    private static final TFTransactionMapper tFTransactionMapper = new TFTransactionMapper();
    private static final TFActiveTraceMapper tFActiveTraceMapper = new TFActiveTraceMapper();
    private static final TFResponseTimeMapper tFResponseTimeMapper = new TFResponseTimeMapper();
    private static final TFDataSourceListBoMapper tFDataSourceListBoMapper = new TFDataSourceListBoMapper();
    private static final TFFileDescriptorMapper tFFileDescriptorBoMapper = new TFFileDescriptorMapper();
    private static final TFDirectBufferMapper tFDirectBufferMapper = new TFDirectBufferMapper();

    public List<TFAgentStat> map(AgentStatBo agentStatBo) {
        final TreeMap<Long, TFAgentStat> tFAgentStatMap = new TreeMap<>();
        final String agentId = agentStatBo.getAgentId();
        final long startTimestamp = agentStatBo.getStartTimestamp();

        insertTFCpuLoad(tFAgentStatMap, agentStatBo.getCpuLoadBos(), agentId, startTimestamp);
        insertTFJvmGc(tFAgentStatMap, agentStatBo.getJvmGcBos(), agentId, startTimestamp);
        insertTFTransaction(tFAgentStatMap, agentStatBo.getTransactionBos(), agentId, startTimestamp);
        insertTFActiveTrace(tFAgentStatMap, agentStatBo.getActiveTraceBos(), agentId, startTimestamp);
        insertTFResponseTime(tFAgentStatMap, agentStatBo.getResponseTimeBos(), agentId, startTimestamp);
        insertTFDataSourceList(tFAgentStatMap, agentStatBo.getDataSourceListBos(), agentId, startTimestamp);
        insertTFileDescriptorList(tFAgentStatMap, agentStatBo.getFileDescriptorBos(), agentId, startTimestamp);
        insertTDirectBufferList(tFAgentStatMap, agentStatBo.getDirectBufferBos(), agentId, startTimestamp);
        return new ArrayList<>(tFAgentStatMap.values());
    }

    private void insertTFDataSourceList(TreeMap<Long, TFAgentStat> tFAgentStatMap, List<DataSourceListBo> dataSourceListBoList, String agentId, long startTimestamp) {
        if (dataSourceListBoList == null) {
            return;
        }

        for (DataSourceListBo dataSourceListBo : dataSourceListBoList) {
            TFAgentStat tFAgentStat = getOrCreateTFAgentStat(tFAgentStatMap, dataSourceListBo.getTimestamp(), agentId, startTimestamp);
            tFAgentStat.setDataSourceList(tFDataSourceListBoMapper.map(dataSourceListBo));
        }
    }

    private void insertTFResponseTime(TreeMap<Long, TFAgentStat> tFAgentStatMap, List<ResponseTimeBo> responseTimeBoList, String agentId, long startTimestamp) {
        if (responseTimeBoList == null) {
            return;
        }

        for (ResponseTimeBo responseTimeBo : responseTimeBoList) {
            TFAgentStat tFAgentStat = getOrCreateTFAgentStat(tFAgentStatMap, responseTimeBo.getTimestamp(), agentId, startTimestamp);
            tFAgentStat.setResponseTime(tFResponseTimeMapper.map(responseTimeBo));
        }
    }

    private void insertTFActiveTrace(TreeMap<Long, TFAgentStat> tFAgentStatMap, List<ActiveTraceBo> activeTraceBoList, String agentId, long startTimestamp) {
        if (activeTraceBoList == null) {
            return;
        }

        for (ActiveTraceBo activeTraceBo : activeTraceBoList) {
            TFAgentStat tFAgentStat = getOrCreateTFAgentStat(tFAgentStatMap, activeTraceBo.getTimestamp(), agentId, startTimestamp);
            tFAgentStat.setActiveTrace(tFActiveTraceMapper.map(activeTraceBo));
        }
    }

    private void insertTFTransaction(TreeMap<Long, TFAgentStat> tFAgentStatMap, List<TransactionBo> transactionBoList, String agentId, long startTimestamp) {
        if (transactionBoList == null) {
            return;
        }

        for (TransactionBo transactionBo : transactionBoList) {
            TFAgentStat tFAgentStat = getOrCreateTFAgentStat(tFAgentStatMap, transactionBo.getTimestamp(), agentId, startTimestamp);
            tFAgentStat.setCollectInterval(transactionBo.getCollectInterval());
            tFAgentStat.setTransaction(tFTransactionMapper.map(transactionBo));
        }
    }

    private void insertTFJvmGc(TreeMap<Long, TFAgentStat> tFAgentStatMap, List<JvmGcBo> jvmGcBoList, String agentId, long startTimestamp) {
        if (jvmGcBoList == null) {
            return;
        }

        for (JvmGcBo jvmGcBo : jvmGcBoList) {
            TFAgentStat tFAgentStat = getOrCreateTFAgentStat(tFAgentStatMap, jvmGcBo.getTimestamp(), agentId, startTimestamp);
            tFAgentStat.setGc(tFJvmGcMapper.map(jvmGcBo));
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

    private void insertTFileDescriptorList(Map<Long, TFAgentStat> tFAgentStatMap, List<FileDescriptorBo> fileDescriptorBoList, String agentId, long startTimestamp) {
        if (fileDescriptorBoList == null) {
            return;
        }

        for (FileDescriptorBo fileDescriptorBo : fileDescriptorBoList) {
            TFAgentStat tFAgentStat = getOrCreateTFAgentStat(tFAgentStatMap, fileDescriptorBo.getTimestamp(), agentId, startTimestamp);
            tFAgentStat.setFileDescriptor(tFFileDescriptorBoMapper.map(fileDescriptorBo));
        }
    }

    private void insertTDirectBufferList(Map<Long, TFAgentStat> tFAgentStatMap, List<DirectBufferBo> directBufferBoList, String agentId, long startTimestamp) {
        if (directBufferBoList == null) {
            return;
        }

        for (DirectBufferBo directBufferBo : directBufferBoList) {
            TFAgentStat tFAgentStat = getOrCreateTFAgentStat(tFAgentStatMap, directBufferBo.getTimestamp(), agentId, startTimestamp);
            tFAgentStat.setDirectBuffer(tFDirectBufferMapper.map(directBufferBo));
        }
    }

    private TFAgentStat getOrCreateTFAgentStat(Map<Long, TFAgentStat> tFAgentStatMap, long timestamp, String agentId, long startTimestamp) {
        TFAgentStat tFAgentStat = tFAgentStatMap.get(timestamp);

        if (tFAgentStat == null) {
            tFAgentStat = new TFAgentStat();
            tFAgentStat.setAgentId(agentId);
            tFAgentStat.setStartTimestamp(startTimestamp);
            tFAgentStat.setTimestamp(timestamp);
            tFAgentStatMap.put(timestamp, tFAgentStat);
        }

        return tFAgentStat;
    }
}
