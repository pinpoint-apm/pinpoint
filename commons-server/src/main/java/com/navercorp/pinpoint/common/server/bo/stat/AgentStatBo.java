/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.common.server.bo.stat;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class AgentStatBo {

    private String agentId;

    private long startTimestamp;

    private List<JvmGcBo> jvmGcBos;
    private List<JvmGcDetailedBo> jvmGcDetailedBos;
    private List<CpuLoadBo> cpuLoadBos;
    private List<TransactionBo> transactionBos;
    private List<ActiveTraceBo> activeTraceBos;
    private List<DataSourceListBo> dataSourceListBos;
    private List<ResponseTimeBo> responseTimeBos;
    private List<DeadlockThreadCountBo> deadlockThreadCountBos;
    private List<FileDescriptorBo> fileDescriptorBos;
    private List<DirectBufferBo> directBufferBos;
    private List<TotalThreadCountBo> totalThreadCountBos;

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public List<JvmGcBo> getJvmGcBos() {
        return jvmGcBos;
    }

    public void setJvmGcBos(List<JvmGcBo> jvmGcBos) {
        this.jvmGcBos = jvmGcBos;
    }

    public List<JvmGcDetailedBo> getJvmGcDetailedBos() {
        return jvmGcDetailedBos;
    }

    public void setJvmGcDetailedBos(List<JvmGcDetailedBo> jvmGcDetailedBos) {
        this.jvmGcDetailedBos = jvmGcDetailedBos;
    }

    public List<CpuLoadBo> getCpuLoadBos() {
        return cpuLoadBos;
    }

    public void setCpuLoadBos(List<CpuLoadBo> cpuLoadBos) {
        this.cpuLoadBos = cpuLoadBos;
    }

    public List<TransactionBo> getTransactionBos() {
        return transactionBos;
    }

    public void setTransactionBos(List<TransactionBo> transactionBos) {
        this.transactionBos = transactionBos;
    }

    public List<ActiveTraceBo> getActiveTraceBos() {
        return activeTraceBos;
    }

    public void setActiveTraceBos(List<ActiveTraceBo> activeTraceBos) {
        this.activeTraceBos = activeTraceBos;
    }

    public List<DataSourceListBo> getDataSourceListBos() {
        return dataSourceListBos;
    }

    public void setDataSourceListBos(List<DataSourceListBo> dataSourceListBos) {
        this.dataSourceListBos = dataSourceListBos;
    }

    public List<ResponseTimeBo> getResponseTimeBos() {
        return responseTimeBos;
    }

    public void setResponseTimeBos(List<ResponseTimeBo> responseTimeBos) {
        this.responseTimeBos = responseTimeBos;
    }

    public List<DeadlockThreadCountBo> getDeadlockThreadCountBos() {
        return deadlockThreadCountBos;
    }

    public void setDeadlockThreadCountBos(List<DeadlockThreadCountBo> deadlockThreadCountBos) {
        this.deadlockThreadCountBos = deadlockThreadCountBos;
    }

    public List<FileDescriptorBo> getFileDescriptorBos() {
        return fileDescriptorBos;
    }

    public void setFileDescriptorBos(List<FileDescriptorBo> fileDescriptorBos) {
        this.fileDescriptorBos = fileDescriptorBos;
    }

    public List<DirectBufferBo> getDirectBufferBos() { return directBufferBos; }

    public void setDirectBufferBos(List<DirectBufferBo> directBufferBos) { this.directBufferBos = directBufferBos; }

    public List<TotalThreadCountBo> getTotalThreadCountBos() { return totalThreadCountBos; }

    public void setTotalThreadCountBos(List<TotalThreadCountBo> totalThreadCountBos) {
        this.totalThreadCountBos = totalThreadCountBos;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentStatBo{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", jvmGcBos=").append(jvmGcBos);
        sb.append(", jvmGcDetailedBos=").append(jvmGcDetailedBos);
        sb.append(", cpuLoadBos=").append(cpuLoadBos);
        sb.append(", transactionBos=").append(transactionBos);
        sb.append(", activeTraceBos=").append(activeTraceBos);
        sb.append(", dataSourceListBos=").append(dataSourceListBos);
        sb.append(", responseTimeBos=").append(responseTimeBos);
        sb.append(", deadlockThreadCountBos=").append(deadlockThreadCountBos);
        sb.append(", fileDescriptorBos=").append(fileDescriptorBos);
        sb.append(", directBufferBos=").append(directBufferBos);
        sb.append(", totalThreadCountBos=").append(totalThreadCountBos);
        sb.append('}');
        return sb.toString();
    }

}
