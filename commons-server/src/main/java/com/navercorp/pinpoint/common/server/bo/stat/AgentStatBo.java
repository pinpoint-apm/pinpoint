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


import com.navercorp.pinpoint.common.server.util.FilterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class AgentStatBo {

    private final String agentId;
    private final long startTimestamp;

    private final List<JvmGcBo> jvmGcBos;
    private final List<JvmGcDetailedBo> jvmGcDetailedBos;
    private final List<CpuLoadBo> cpuLoadBos;
    private final List<TransactionBo> transactionBos;
    private final List<ActiveTraceBo> activeTraceBos;
    private final List<DataSourceListBo> dataSourceListBos;
    private final List<ResponseTimeBo> responseTimeBos;
    private final List<DeadlockThreadCountBo> deadlockThreadCountBos;
    private final List<FileDescriptorBo> fileDescriptorBos;
    private final List<DirectBufferBo> directBufferBos;
    private final List<TotalThreadCountBo> totalThreadCountBos;
    private final List<LoadedClassBo> loadedClassBos;


    public AgentStatBo(Builder builder) {
        this.agentId = builder.agentId;
        this.startTimestamp = builder.startTimestamp;
        this.jvmGcBos = FilterUtils.filter(builder.statList, JvmGcBo.class);
        this.jvmGcDetailedBos = FilterUtils.filter(builder.statList, JvmGcDetailedBo.class);
        this.cpuLoadBos = FilterUtils.filter(builder.statList, CpuLoadBo.class);
        this.transactionBos = FilterUtils.filter(builder.statList, TransactionBo.class);
        this.activeTraceBos = FilterUtils.filter(builder.statList, ActiveTraceBo.class);
        this.dataSourceListBos = FilterUtils.filter(builder.statList, DataSourceListBo.class);
        this.responseTimeBos = FilterUtils.filter(builder.statList, ResponseTimeBo.class);
        this.deadlockThreadCountBos = FilterUtils.filter(builder.statList, DeadlockThreadCountBo.class);
        this.fileDescriptorBos = FilterUtils.filter(builder.statList, FileDescriptorBo.class);
        this.directBufferBos = FilterUtils.filter(builder.statList, DirectBufferBo.class);
        this.totalThreadCountBos = FilterUtils.filter(builder.statList, TotalThreadCountBo.class);
        this.loadedClassBos = FilterUtils.filter(builder.statList, LoadedClassBo.class);
    }


    public long getStartTimestamp() {
        return startTimestamp;
    }

    public String getAgentId() {
        return agentId;
    }

    public List<JvmGcBo> getJvmGcBos() {
        return jvmGcBos;
    }

    public List<JvmGcDetailedBo> getJvmGcDetailedBos() {
        return jvmGcDetailedBos;
    }

    public List<CpuLoadBo> getCpuLoadBos() {
        return cpuLoadBos;
    }

    public List<TransactionBo> getTransactionBos() {
        return transactionBos;
    }

    public List<ActiveTraceBo> getActiveTraceBos() {
        return activeTraceBos;
    }

    public List<DataSourceListBo> getDataSourceListBos() {
        return dataSourceListBos;
    }

    public List<ResponseTimeBo> getResponseTimeBos() {
        return responseTimeBos;
    }


    public List<DeadlockThreadCountBo> getDeadlockThreadCountBos() {
        return deadlockThreadCountBos;
    }

    public List<FileDescriptorBo> getFileDescriptorBos() {
        return fileDescriptorBos;
    }

    public List<DirectBufferBo> getDirectBufferBos() {
        return directBufferBos;
    }

    public List<TotalThreadCountBo> getTotalThreadCountBos() {
        return totalThreadCountBos;
    }

    public List<LoadedClassBo> getLoadedClassBos() {
        return loadedClassBos;
    }

    public static Builder newBuilder(String agentId, long startTimestamp) {
        return new Builder(agentId, startTimestamp);
    }

    public static class Builder {
        private final String agentId;
        private final long startTimestamp;

        private final List<AgentStatDataPoint> statList = new ArrayList<>();

        public Builder(String agentId, long startTimestamp) {
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            this.startTimestamp = startTimestamp;
        }

        public StatBuilder newStatBuilder(long timestamp) {
            return new StatBuilder(timestamp);
        }

        public class StatBuilder {
            private final long timestamp;

            public StatBuilder(long timestamp) {
                this.timestamp = timestamp;
            }

            private void setBaseData(AgentStatDataPoint agentStatDataPoint) {
                agentStatDataPoint.setAgentId(agentId);
                agentStatDataPoint.setStartTimestamp(startTimestamp);
                agentStatDataPoint.setTimestamp(this.timestamp);
            }

            public void addJvmGc(JvmGcBo jvmGc) {
                Objects.requireNonNull(jvmGc, "jvmGc");
                setBaseData(jvmGc);
                statList.add(jvmGc);
            }

            public void addJvmGcDetailed(JvmGcDetailedBo jvmGcDetailed) {
                Objects.requireNonNull(jvmGcDetailed, "jvmGcDetailed");
                setBaseData(jvmGcDetailed);
                statList.add(jvmGcDetailed);
            }


            public void addCpuLoad(CpuLoadBo cpuLoad) {
                Objects.requireNonNull(cpuLoad, "cpuLoad");
                setBaseData(cpuLoad);
                statList.add(cpuLoad);
            }


            public void addTransaction(TransactionBo transaction) {
                Objects.requireNonNull(transaction, "transaction");
                setBaseData(transaction);
                statList.add(transaction);
            }


            public void addActiveTrace(ActiveTraceBo activeTrace) {
                Objects.requireNonNull(activeTrace, "activeTrace");
                setBaseData(activeTrace);
                statList.add(activeTrace);
            }

            public void addDataSourceList(DataSourceListBo dataSourceList) {
                Objects.requireNonNull(dataSourceList, "dataSourceList");
                setBaseData(dataSourceList);
                for (DataSourceBo dataSourceBo : dataSourceList.getList()) {
                    setBaseData(dataSourceBo);
                }
                statList.add(dataSourceList);
            }


            public void addResponseTime(ResponseTimeBo responseTime) {
                Objects.requireNonNull(responseTime, "responseTime");
                setBaseData(responseTime);
                statList.add(responseTime);
            }


            public void addDeadlockThreadCount(DeadlockThreadCountBo deadlockThreadCount) {
                Objects.requireNonNull(deadlockThreadCount, "deadlockThreadCount");
                setBaseData(deadlockThreadCount);
                statList.add(deadlockThreadCount);
            }


            public void addFileDescriptor(FileDescriptorBo fileDescriptor) {
                Objects.requireNonNull(fileDescriptor, "fileDescriptor");
                setBaseData(fileDescriptor);
                statList.add(fileDescriptor);
            }


            public void addDirectBuffer(DirectBufferBo directBuffer) {
                Objects.requireNonNull(directBuffer, "directBuffer");
                setBaseData(directBuffer);
                statList.add(directBuffer);
            }


            public void addTotalThreadCount(TotalThreadCountBo totalThreadCount) {
                Objects.requireNonNull(totalThreadCount, "totalThreadCount");
                setBaseData(totalThreadCount);
                statList.add(totalThreadCount);
            }


            public void addLoadedClass(LoadedClassBo loadedClass) {
                Objects.requireNonNull(loadedClass, "loadedClass");
                setBaseData(loadedClass);
                statList.add(loadedClass);
            }

        }

        public AgentStatBo build() {
            return new AgentStatBo(this);
        }
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
        sb.append(", loadedClassBos=").append(loadedClassBos);
        sb.append('}');
        return sb.toString();
    }

}
