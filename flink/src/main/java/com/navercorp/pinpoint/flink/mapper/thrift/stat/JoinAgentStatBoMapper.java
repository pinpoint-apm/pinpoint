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
import com.navercorp.pinpoint.flink.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStatBatch;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class JoinAgentStatBoMapper implements ThriftBoMapper<JoinAgentStatBo, TFAgentStatBatch> {
    private final ThriftStatMapper<?, ?>[] mappers = new ThriftStatMapper[] {
            new JoinCpuLoadBoMapper(),
            new JoinMemoryBoMapper(),
            new JoinTransactionBoMapper(),
            new JoinActiveTraceBoMapper(),
            new JoinResponseTimeBoMapper(),
            new JoinDataSourceListBoMapper(),
            new JoinFileDescriptorBoMapper(),
            new JoinDirectBufferBoMapper(),
            new JoinTotalThreadCountBoMapper(),
            new JoinLoadedClassBoMapper()
    };

    public JoinAgentStatBoMapper() {
    }

    @Override
    public JoinAgentStatBo map(TFAgentStatBatch tFAgentStatBatch) {
        if (!tFAgentStatBatch.isSetAgentStats()) {
            return JoinAgentStatBo.EMPTY_JOIN_AGENT_STAT_BO;
        }

        if (StringUtils.isEmpty(tFAgentStatBatch.getAgentId())) {
            return JoinAgentStatBo.EMPTY_JOIN_AGENT_STAT_BO;
        }

        final String agentId = tFAgentStatBatch.getAgentId();
        final long startTimestamp = tFAgentStatBatch.getStartTimestamp();
        final long timeStamp = getTimeStamp(tFAgentStatBatch);

        JoinAgentStatBo.Builder builder = JoinAgentStatBo.newBuilder(agentId, startTimestamp, timeStamp);
        for (TFAgentStat agentStat : tFAgentStatBatch.getAgentStats()) {
            for (ThriftStatMapper<?, ?> mapper : mappers) {
                mapper.build(agentStat, builder);
            }
        }

        return builder.build();
    }


    private long getTimeStamp(TFAgentStatBatch joinAgentStatBo) {
        List<TFAgentStat> agentStats = joinAgentStatBo.getAgentStats();
        for (TFAgentStat agentStat : agentStats) {
            return agentStat.getTimestamp();
        }
        return Long.MIN_VALUE;
    }

}
