/*
 * Copyright 2020 Naver Corp.
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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTotalThreadCountBo;
import com.navercorp.pinpoint.flink.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;

public class JoinTotalThreadCountBoMapper implements ThriftBoMapper<JoinTotalThreadCountBo, TFAgentStat> {
    @Override
    public JoinTotalThreadCountBo map(TFAgentStat tFAgentStat) {
        if(!tFAgentStat.isSetTotalThreadCount()) {
            return JoinTotalThreadCountBo.EMPTY_TOTAL_THREAD_COUNT_BO;
        }
        JoinTotalThreadCountBo joinTotalThreadCountBo = new JoinTotalThreadCountBo();

        final String agentId = tFAgentStat.getAgentId();
        final long totalThreadCount = tFAgentStat.getTotalThreadCount().getTotalThreadCount();
        joinTotalThreadCountBo.setId(agentId);
        joinTotalThreadCountBo.setTimestamp(tFAgentStat.getTimestamp());
        joinTotalThreadCountBo.setAvgTotalThreadCount(totalThreadCount);
        joinTotalThreadCountBo.setMinTotalThreadCount(totalThreadCount);
        joinTotalThreadCountBo.setMinTotalThreadCountAgentId(agentId);
        joinTotalThreadCountBo.setMaxTotalThreadCount(totalThreadCount);
        joinTotalThreadCountBo.setMaxTotalThreadCountAgentId(agentId);
        return joinTotalThreadCountBo;
    }
}
