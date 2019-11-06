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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinMemoryBo;
import com.navercorp.pinpoint.flink.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFJvmGc;

/**
 * @author minwoo.jung
 */
public class JoinMemoryBoMapper implements ThriftBoMapper<JoinMemoryBo, TFAgentStat> {

    public JoinMemoryBo map(TFAgentStat tFAgentStat) {
        if (!tFAgentStat.isSetGc()) {
            return JoinMemoryBo.EMPTY_JOIN_MEMORY_BO;
        }

        JoinMemoryBo joinMemoryBo = new JoinMemoryBo();
        joinMemoryBo.setId(tFAgentStat.getAgentId());

        TFJvmGc memory = tFAgentStat.getGc();
        joinMemoryBo.setTimestamp(tFAgentStat.getTimestamp());
        joinMemoryBo.setHeapUsed(memory.getJvmMemoryHeapUsed());
        joinMemoryBo.setMinHeapUsed(memory.getJvmMemoryHeapUsed());
        joinMemoryBo.setMaxHeapUsed(memory.getJvmMemoryHeapUsed());
        joinMemoryBo.setMinHeapAgentId(tFAgentStat.getAgentId());
        joinMemoryBo.setMaxHeapAgentId(tFAgentStat.getAgentId());
        joinMemoryBo.setNonHeapUsed(memory.getJvmMemoryNonHeapUsed());
        joinMemoryBo.setMinNonHeapUsed(memory.getJvmMemoryNonHeapUsed());
        joinMemoryBo.setMaxNonHeapUsed(memory.getJvmMemoryNonHeapUsed());
        joinMemoryBo.setMinNonHeapAgentId(tFAgentStat.getAgentId());
        joinMemoryBo.setMaxNonHeapAgentId(tFAgentStat.getAgentId());

        return joinMemoryBo;
    }
}
