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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinAgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFDirectBuffer;

/**
 * @author Roy Kim
 */
public class JoinDirectBufferBoMapper implements ThriftStatMapper<JoinDirectBufferBo, TFAgentStat> {

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
        final long directCount = tFDirectBuffer.getDirectCount();
        joinDirectBufferBo.setDirectCountJoinValue(new JoinLongFieldBo(directCount, directCount, agentId, directCount, agentId));

        final long directMemoryUsed = tFDirectBuffer.getDirectMemoryUsed();
        joinDirectBufferBo.setDirectMemoryUsedJoinValue(new JoinLongFieldBo(directMemoryUsed, directMemoryUsed, agentId, directMemoryUsed, agentId));

        final long mappedCount = tFDirectBuffer.getMappedCount();
        joinDirectBufferBo.setMappedCountJoinValue(new JoinLongFieldBo(mappedCount, mappedCount, agentId, mappedCount, agentId));

        final long mappedMemoryUsed = tFDirectBuffer.getMappedMemoryUsed();
        joinDirectBufferBo.setMappedMemoryUsedJoinValue(new JoinLongFieldBo(mappedMemoryUsed, mappedMemoryUsed, agentId, mappedMemoryUsed, agentId));

        return joinDirectBufferBo;
    }

    @Override
    public void build(TFAgentStat tFAgentStat, JoinAgentStatBo.Builder builder) {
        JoinDirectBufferBo joinDirectBufferBo = this.map(tFAgentStat);

        if (joinDirectBufferBo == JoinDirectBufferBo.EMPTY_JOIN_DIRECT_BUFFER_BO) {
            return;
        }

        builder.addDirectBuffer(joinDirectBufferBo);
    }
}
