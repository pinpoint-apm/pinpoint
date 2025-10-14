/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo.stat;


import com.navercorp.pinpoint.common.server.util.FilterUtils;
import com.navercorp.pinpoint.common.server.util.NumberPrecondition;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class AgentStatBo {
    @NonNull
    private final String applicationName;
    @NonNull
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


    private AgentStatBo(Builder builder) {
        this.applicationName = builder.applicationName;
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

    public String getApplicationName() {
        return applicationName;
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

    public static Builder newBuilder(String applicationName, String agentId, long startTimestamp) {
        return new Builder(applicationName, agentId, startTimestamp);
    }

    public static class Builder {

        private final String applicationName;
        private final String agentId;
        private final long startTimestamp;

        private final List<StatDataPoint> statList = new ArrayList<>();

        Builder(String applicationName, String agentId, long startTimestamp) {
            this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
            this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
            this.startTimestamp = NumberPrecondition.requirePositiveOrZero(startTimestamp, "startTimestamp");
        }

        public StatBuilder newStatBuilder(long timestamp) {
            return new StatBuilder(timestamp);
        }

        public class StatBuilder {
            private final DataPoint dataPoint;

            public StatBuilder(long timestamp) {
                this.dataPoint = DataPoint.of(agentId, applicationName, startTimestamp, timestamp);
            }

            public DataPoint getDataPoint() {
                return dataPoint;
            }

            public void addPoint(StatDataPoint point) {
                Objects.requireNonNull(point, "point");
                statList.add(point);
            }
        }

        public AgentStatBo build() {
            return new AgentStatBo(this);
        }
    }

    @Override
    public String toString() {
        return "AgentStatBo{" +
                "agentId='" + agentId + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", jvmGcBos=" + jvmGcBos +
                ", jvmGcDetailedBos=" + jvmGcDetailedBos +
                ", cpuLoadBos=" + cpuLoadBos +
                ", transactionBos=" + transactionBos +
                ", activeTraceBos=" + activeTraceBos +
                ", dataSourceListBos=" + dataSourceListBos +
                ", responseTimeBos=" + responseTimeBos +
                ", deadlockThreadCountBos=" + deadlockThreadCountBos +
                ", fileDescriptorBos=" + fileDescriptorBos +
                ", directBufferBos=" + directBufferBos +
                ", totalThreadCountBos=" + totalThreadCountBos +
                ", loadedClassBos=" + loadedClassBos +
                '}';
    }
}
