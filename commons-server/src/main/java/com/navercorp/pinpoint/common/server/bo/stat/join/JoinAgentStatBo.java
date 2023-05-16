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
package com.navercorp.pinpoint.common.server.bo.stat.join;

import com.navercorp.pinpoint.common.server.util.FilterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class JoinAgentStatBo implements JoinStatBo {
    public static final JoinAgentStatBo EMPTY_JOIN_AGENT_STAT_BO = newEmptyAgentStatBo();

    private static JoinAgentStatBo newEmptyAgentStatBo() {
        return JoinAgentStatBo.newBuilder(UNKNOWN_AGENT, Long.MIN_VALUE, Long.MIN_VALUE).build();
    }

    private final String agentId;
    private final long agentStartTimestamp;
    private final long timestamp;
    private final List<JoinCpuLoadBo> joinCpuLoadBoList;
    private final List<JoinMemoryBo> joinMemoryBoList;
    private final List<JoinTransactionBo> joinTransactionBoList;
    private final List<JoinActiveTraceBo> joinActiveTraceBoList;
    private final List<JoinResponseTimeBo> joinResponseTimeBoList;
    private final List<JoinDataSourceListBo> joinDataSourceListBoList;
    private final List<JoinFileDescriptorBo> joinFileDescriptorBoList;
    private final List<JoinDirectBufferBo> joinDirectBufferBoList;
    private final List<JoinTotalThreadCountBo> joinTotalThreadCountBoList;
    private final List<JoinLoadedClassBo> joinLoadedClassBoList;

    protected JoinAgentStatBo(JoinAgentStatBo joinAgentStatBo) {
        Objects.requireNonNull(joinAgentStatBo, "joinAgentStatBo");

        this.agentId = joinAgentStatBo.getId();
        this.agentStartTimestamp = joinAgentStatBo.getAgentStartTimestamp();
        this.timestamp = joinAgentStatBo.getTimestamp();
        this.joinCpuLoadBoList = joinAgentStatBo.getJoinCpuLoadBoList();
        this.joinMemoryBoList = joinAgentStatBo.getJoinMemoryBoList();
        this.joinTransactionBoList = joinAgentStatBo.getJoinTransactionBoList();
        this.joinActiveTraceBoList = joinAgentStatBo.getJoinActiveTraceBoList();
        this.joinResponseTimeBoList = joinAgentStatBo.getJoinResponseTimeBoList();
        this.joinDataSourceListBoList = joinAgentStatBo.getJoinDataSourceListBoList();
        this.joinFileDescriptorBoList = joinAgentStatBo.getJoinFileDescriptorBoList();
        this.joinDirectBufferBoList = joinAgentStatBo.getJoinDirectBufferBoList();
        this.joinTotalThreadCountBoList = joinAgentStatBo.getJoinTotalThreadCountBoList();
        this.joinLoadedClassBoList = joinAgentStatBo.getJoinLoadedClassBoList();
    }

    private JoinAgentStatBo(JoinAgentStatBo.Builder joinAgentStatBo) {
        Objects.requireNonNull(joinAgentStatBo, "joinAgentStatBo");

        this.agentId = joinAgentStatBo.agentId;
        this.agentStartTimestamp = joinAgentStatBo.agentStartTimestamp;
        this.timestamp = joinAgentStatBo.timestamp;

        this.joinCpuLoadBoList = FilterUtils.filter(joinAgentStatBo.statList, JoinCpuLoadBo.class);
        this.joinMemoryBoList = FilterUtils.filter(joinAgentStatBo.statList, JoinMemoryBo.class);
        this.joinTransactionBoList = FilterUtils.filter(joinAgentStatBo.statList, JoinTransactionBo.class);
        this.joinActiveTraceBoList = FilterUtils.filter(joinAgentStatBo.statList, JoinActiveTraceBo.class);
        this.joinResponseTimeBoList = FilterUtils.filter(joinAgentStatBo.statList, JoinResponseTimeBo.class);
        this.joinDataSourceListBoList = FilterUtils.filter(joinAgentStatBo.statList, JoinDataSourceListBo.class);
        this.joinFileDescriptorBoList = FilterUtils.filter(joinAgentStatBo.statList, JoinFileDescriptorBo.class);
        this.joinDirectBufferBoList = FilterUtils.filter(joinAgentStatBo.statList, JoinDirectBufferBo.class);
        this.joinTotalThreadCountBoList = FilterUtils.filter(joinAgentStatBo.statList, JoinTotalThreadCountBo.class);
        this.joinLoadedClassBoList = FilterUtils.filter(joinAgentStatBo.statList, JoinLoadedClassBo.class);
    }

    public List<JoinResponseTimeBo> getJoinResponseTimeBoList() {
        return joinResponseTimeBoList;
    }

    public String getId() {
        return agentId;
    }


    public long getTimestamp() {
        return timestamp;
    }

    public List<JoinFileDescriptorBo> getJoinFileDescriptorBoList() {
        return joinFileDescriptorBoList;
    }

    public List<JoinCpuLoadBo> getJoinCpuLoadBoList() {
        return joinCpuLoadBoList;
    }

    public List<JoinDirectBufferBo> getJoinDirectBufferBoList() {
        return joinDirectBufferBoList;
    }

    public List<JoinTotalThreadCountBo> getJoinTotalThreadCountBoList() {
        return joinTotalThreadCountBoList;
    }

    public List<JoinLoadedClassBo> getJoinLoadedClassBoList() {
        return joinLoadedClassBoList;
    }


    public long getAgentStartTimestamp() {
        return agentStartTimestamp;
    }

    public static JoinAgentStatBo joinAgentStatBo(List<JoinAgentStatBo> joinAgentStatBoList) {
        if (joinAgentStatBoList.isEmpty()) {
            return newEmptyAgentStatBo();
        }

        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<>();
        for (JoinAgentStatBo joinAgentStatBo : joinAgentStatBoList) {
            joinCpuLoadBoList.addAll(joinAgentStatBo.getJoinCpuLoadBoList());
        }

        JoinCpuLoadBo joinCpuLoadBo = JoinCpuLoadBo.joinCpuLoadBoList(joinCpuLoadBoList, joinCpuLoadBoList.get(0).getTimestamp());

        JoinAgentStatBo.Builder builder = JoinAgentStatBo.newBuilder(joinCpuLoadBo.getId(), Long.MIN_VALUE, joinCpuLoadBo.getTimestamp());
        builder.addCpuLoadBo(joinCpuLoadBo);

        return builder.build();

    }

    public List<JoinMemoryBo> getJoinMemoryBoList() {
        return joinMemoryBoList;
    }

    public List<JoinTransactionBo> getJoinTransactionBoList() {
        return joinTransactionBoList;
    }

    public List<JoinActiveTraceBo> getJoinActiveTraceBoList() {
        return joinActiveTraceBoList;
    }

    public List<JoinDataSourceListBo> getJoinDataSourceListBoList() {
        return joinDataSourceListBoList;
    }

    public static Builder newBuilder(String agentId, long agentStartTimestamp, long timestamp) {
        return new Builder(agentId, agentStartTimestamp, timestamp);
    }

    public static class Builder {
        private final String agentId;
        private final long agentStartTimestamp;
        private final long timestamp;

        private final List<JoinStatBo> statList = new ArrayList<>();


        Builder(String agentId, long agentStartTimestamp, long timestamp) {
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            this.agentStartTimestamp = agentStartTimestamp;
            this.timestamp = timestamp;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void addCpuLoadBo(JoinCpuLoadBo cpuLoadBo) {
            Objects.requireNonNull(cpuLoadBo, "cpuLoadBo");
            this.statList.add(cpuLoadBo);
        }

        public void addDataSourceListBo(JoinDataSourceListBo dataSourceListBo) {
            Objects.requireNonNull(dataSourceListBo, "dataSourceListBo");
            this.statList.add(dataSourceListBo);
        }

        public void addResponseTime(JoinResponseTimeBo responseTime) {
            Objects.requireNonNull(responseTime, "responseTime");
            this.statList.add(responseTime);
        }

        public void addFileDescriptor(JoinFileDescriptorBo fileDescriptor) {
            Objects.requireNonNull(fileDescriptor, "fileDescriptor");
            this.statList.add(fileDescriptor);
        }

        public void addDirectBuffer(JoinDirectBufferBo directBuffer) {
            Objects.requireNonNull(directBuffer, "directBuffer");
            this.statList.add(directBuffer);
        }

        public void addTotalThreadCount(JoinTotalThreadCountBo totalThreadCount) {
            Objects.requireNonNull(totalThreadCount, "totalThreadCount");
            this.statList.add(totalThreadCount);
        }

        public void addLoadedClass(JoinLoadedClassBo loadedClass) {
            Objects.requireNonNull(loadedClass, "loadedClass");
            this.statList.add(loadedClass);
        }

        public void addMemory(JoinMemoryBo memory) {
            Objects.requireNonNull(memory, "memory");
            this.statList.add(memory);
        }


        public void addTransaction(JoinTransactionBo transaction) {
            Objects.requireNonNull(transaction, "transaction");
            this.statList.add(transaction);
        }


        public void addActiveTrace(JoinActiveTraceBo activeTrace) {
            Objects.requireNonNull(activeTrace, "activeTrace");
            this.statList.add(activeTrace);
        }

        public JoinAgentStatBo build() {
            return new JoinAgentStatBo(this);
        }
    }

    @Override
    public String toString() {
        return "JoinAgentStatBo{" +
                "agentId='" + agentId + '\'' +
                ", agentStartTimestamp=" + agentStartTimestamp +
                ", timestamp=" + timestamp +
                ", joinCpuLoadBoList=" + joinCpuLoadBoList +
                ", joinMemoryBoList=" + joinMemoryBoList +
                ", joinTransactionBoList=" + joinTransactionBoList +
                ", joinActiveTraceBoList=" + joinActiveTraceBoList +
                ", joinResponseTimeBoList=" + joinResponseTimeBoList +
                ", joinDataSourceListBoList=" + joinDataSourceListBoList +
                ", joinFileDescriptorBoList=" + joinFileDescriptorBoList +
                ", joinDirectBufferBoList=" + joinDirectBufferBoList +
                ", joinTotalThreadCountBoList=" + joinTotalThreadCountBoList +
                ", joinLoadedClassBoList=" + joinLoadedClassBoList +
                '}';
    }
}
