/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.mapper.thrift;

import org.springframework.stereotype.Component;

import com.navercorp.pinpoint.common.bo.AgentStatMemoryGcBo;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TJvmGc;
import com.navercorp.pinpoint.thrift.dto.TJvmGcType;

/**
 * @author hyungil.jeong
 */
@Component
public class AgentStatMemoryGcBoMapper implements ThriftBoMapper<AgentStatMemoryGcBo, TAgentStat> {

    @Override
    public AgentStatMemoryGcBo map(TAgentStat thriftObject) {
        final String agentId = thriftObject.getAgentId();
        final long startTimestamp = thriftObject.getStartTimestamp();
        final long timestamp = thriftObject.getTimestamp();
        final TJvmGc gc = thriftObject.getGc();

        final AgentStatMemoryGcBo.Builder builder = new AgentStatMemoryGcBo.Builder(agentId, startTimestamp, timestamp);
        // gc is optional (for now, null check is enough for non-primitives)
        if (gc != null) {
            builder.gcType(gc.getType().name());
            builder.jvmMemoryHeapUsed(gc.getJvmMemoryHeapUsed());
            builder.jvmMemoryHeapMax(gc.getJvmMemoryHeapMax());
            builder.jvmMemoryNonHeapUsed(gc.getJvmMemoryNonHeapUsed());
            builder.jvmMemoryNonHeapMax(gc.getJvmMemoryNonHeapMax());
            builder.jvmGcOldCount(gc.getJvmGcOldCount());
            builder.jvmGcOldTime(gc.getJvmGcOldTime());
        } else {
            builder.gcType(TJvmGcType.UNKNOWN.name());
        }
        return builder.build();
    }

}
