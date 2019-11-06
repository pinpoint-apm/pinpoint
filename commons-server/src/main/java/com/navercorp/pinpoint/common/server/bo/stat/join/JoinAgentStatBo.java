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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class JoinAgentStatBo implements JoinStatBo {
    public static final JoinAgentStatBo EMPTY_JOIN_AGENT_STAT_BO = new JoinAgentStatBo();


    private String agentId = UNKNOWN_AGENT;
    private long agentStartTimestamp = Long.MIN_VALUE;
    private long timestamp = Long.MIN_VALUE;
    private List<JoinCpuLoadBo> joinCpuLoadBoList = Collections.emptyList();
    private List<JoinMemoryBo> joinMemoryBoList = Collections.emptyList();
    private List<JoinTransactionBo> joinTransactionBoList = Collections.emptyList();
    private List<JoinActiveTraceBo> joinActiveTraceBoList = Collections.emptyList();
    private List<JoinResponseTimeBo> joinResponseTimeBoList = Collections.emptyList();
    private List<JoinDataSourceListBo> joinDataSourceListBoList = Collections.emptyList();
    private List<JoinFileDescriptorBo> joinFileDescriptorBoList = Collections.emptyList();
    private List<JoinDirectBufferBo> joinDirectBufferBoList = Collections.emptyList();

    protected JoinAgentStatBo(JoinAgentStatBo joinAgentStatBo) {
        if (joinAgentStatBo == null) {
            throw new IllegalArgumentException("joinAgentStatBo cannot be null");
        }

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
    }

    public JoinAgentStatBo() {
    }

    public List<JoinResponseTimeBo> getJoinResponseTimeBoList() {
        return joinResponseTimeBoList;
    }

    public void setJoinResponseTimeBoList(List<JoinResponseTimeBo> joinResponseTimeBoList) {
        this.joinResponseTimeBoList = joinResponseTimeBoList;
    }

    public void setId(String id) {
        this.agentId = id;
    }

    public void setJoinCpuLoadBoList(List<JoinCpuLoadBo> joinCpuLoadBoList) {
        this.joinCpuLoadBoList = joinCpuLoadBoList;
    }

    public void setJoinDataSourceListBoList(List<JoinDataSourceListBo> joinDataSourceListBoList) {
        this.joinDataSourceListBoList = joinDataSourceListBoList;
    }

    public String getId() {
        return agentId;
    }

    public void setTimestamp(long timeStamp) {
        this.timestamp = timeStamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<JoinFileDescriptorBo> getJoinFileDescriptorBoList() {
        return joinFileDescriptorBoList;
    }

    public void setJoinFileDescriptorBoList(List<JoinFileDescriptorBo> joinFileDescriptorBoList) {
        this.joinFileDescriptorBoList = joinFileDescriptorBoList;
    }

    public List<JoinCpuLoadBo> getJoinCpuLoadBoList() {
        return joinCpuLoadBoList;
    }

    public List<JoinDirectBufferBo> getJoinDirectBufferBoList() {
        return joinDirectBufferBoList;
    }

    public void setJoinDirectBufferBoList(List<JoinDirectBufferBo> joinDirectBufferBoList) {
        this.joinDirectBufferBoList = joinDirectBufferBoList;
    }

    public long getAgentStartTimestamp() {
        return agentStartTimestamp;
    }

    public void setAgentStartTimestamp(long agentStartTimestamp) {
        this.agentStartTimestamp = agentStartTimestamp;
    }

    public static JoinAgentStatBo joinAgentStatBo(List<JoinAgentStatBo> joinAgentStatBoList) {
        JoinAgentStatBo newJoinAgentStatBo = new JoinAgentStatBo();
        int boCount = joinAgentStatBoList.size();
        if (boCount == 0) {
            return newJoinAgentStatBo;
        }

        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
        for (JoinAgentStatBo joinAgentStatBo : joinAgentStatBoList) {
            joinCpuLoadBoList.addAll(joinAgentStatBo.getJoinCpuLoadBoList());
        }

        JoinCpuLoadBo joinCpuLoadBo = JoinCpuLoadBo.joinCpuLoadBoList(joinCpuLoadBoList, joinCpuLoadBoList.get(0).getTimestamp());
        List<JoinCpuLoadBo> newJoinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
        newJoinCpuLoadBoList.add(joinCpuLoadBo);
        newJoinAgentStatBo.setJoinCpuLoadBoList(newJoinCpuLoadBoList);
        newJoinAgentStatBo.setId(joinCpuLoadBo.getId());
        newJoinAgentStatBo.setTimestamp(joinCpuLoadBo.getTimestamp());

        return newJoinAgentStatBo;

    }

    public void setJoinMemoryBoList(List<JoinMemoryBo> joinMemoryBoList) {
        this.joinMemoryBoList = joinMemoryBoList;
    }

    public List<JoinMemoryBo> getJoinMemoryBoList() {
        return joinMemoryBoList;
    }

    public void setJoinTransactionBoList(List<JoinTransactionBo> joinTransactionBoList) {
        this.joinTransactionBoList = joinTransactionBoList;
    }

    public List<JoinTransactionBo> getJoinTransactionBoList() {
        return joinTransactionBoList;
    }

    public void setJoinActiveTraceBoList(List<JoinActiveTraceBo> joinActiveTraceBoList) {
        this.joinActiveTraceBoList = joinActiveTraceBoList;
    }

    public List<JoinActiveTraceBo> getJoinActiveTraceBoList() {
        return joinActiveTraceBoList;
    }

    public List<JoinDataSourceListBo> getJoinDataSourceListBoList() {
        return joinDataSourceListBoList;
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
            '}';
    }
}
