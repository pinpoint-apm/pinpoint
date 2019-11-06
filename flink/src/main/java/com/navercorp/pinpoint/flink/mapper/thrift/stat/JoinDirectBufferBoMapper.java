/*
 * Copyright 2018 Naver Corp.
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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDirectBufferBo;
import com.navercorp.pinpoint.flink.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFDirectBuffer;

/**
 * @author Roy Kim
 */
public class JoinDirectBufferBoMapper implements ThriftBoMapper<JoinDirectBufferBo, TFAgentStat> {

    @Override
    public JoinDirectBufferBo map(TFAgentStat tFAgentStat) {
        if (!tFAgentStat.isSetDirectBuffer()) {
            return JoinDirectBufferBo.EMPTY_JOIN_DIRECT_BUFFER_BO;
        }

        JoinDirectBufferBo joinDirectBufferBo = new JoinDirectBufferBo();

        final String agentId = tFAgentStat.getAgentId();
        joinDirectBufferBo.setId(agentId);
        joinDirectBufferBo.setTimestamp(tFAgentStat.getTimestamp());

        TFDirectBuffer tFDirectBuffer = tFAgentStat.getDirectBuffer();
        joinDirectBufferBo.setAvgDirectCount(tFDirectBuffer.getDirectCount());
        joinDirectBufferBo.setMinDirectCountAgentId(agentId);
        joinDirectBufferBo.setMinDirectCount(tFDirectBuffer.getDirectCount());
        joinDirectBufferBo.setMaxDirectCountAgentId(agentId);
        joinDirectBufferBo.setMaxDirectCount(tFDirectBuffer.getDirectCount());

        joinDirectBufferBo.setAvgDirectMemoryUsed(tFDirectBuffer.getDirectMemoryUsed());
        joinDirectBufferBo.setMinDirectMemoryUsedAgentId(agentId);
        joinDirectBufferBo.setMinDirectMemoryUsed(tFDirectBuffer.getDirectMemoryUsed());
        joinDirectBufferBo.setMaxDirectMemoryUsedAgentId(agentId);
        joinDirectBufferBo.setMaxDirectMemoryUsed(tFDirectBuffer.getDirectMemoryUsed());

        joinDirectBufferBo.setAvgMappedCount(tFDirectBuffer.getMappedCount());
        joinDirectBufferBo.setMinMappedCountAgentId(agentId);
        joinDirectBufferBo.setMinMappedCount(tFDirectBuffer.getMappedCount());
        joinDirectBufferBo.setMaxMappedCountAgentId(agentId);
        joinDirectBufferBo.setMaxMappedCount(tFDirectBuffer.getMappedCount());

        joinDirectBufferBo.setAvgMappedMemoryUsed(tFDirectBuffer.getMappedMemoryUsed());
        joinDirectBufferBo.setMinMappedMemoryUsedAgentId(agentId);
        joinDirectBufferBo.setMinMappedMemoryUsed(tFDirectBuffer.getMappedMemoryUsed());
        joinDirectBufferBo.setMaxMappedMemoryUsedAgentId(agentId);
        joinDirectBufferBo.setMaxMappedMemoryUsed(tFDirectBuffer.getMappedMemoryUsed());

        return joinDirectBufferBo;
    }
}
