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

import com.navercorp.pinpoint.common.bo.AgentStatCpuLoadBo;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TCpuLoad;

/**
 * @author hyungil.jeong
 */
@Component
public class AgentStatCpuLoadBoMapper implements ThriftBoMapper<AgentStatCpuLoadBo, TAgentStat> {

    @Override
    public AgentStatCpuLoadBo map(TAgentStat thriftObject) {
        final String agentId = thriftObject.getAgentId();
        final long startTimestamp = thriftObject.getStartTimestamp();
        final long timestamp = thriftObject.getTimestamp();
        final TCpuLoad cpuLoad = thriftObject.getCpuLoad();

        final AgentStatCpuLoadBo.Builder builder = new AgentStatCpuLoadBo.Builder(agentId, startTimestamp, timestamp);
        // cpuLoad is optional (for now, null check is enough for non-primitives)
        if (cpuLoad != null) {
            // jvmCpuLoad is optional
            if (cpuLoad.isSetJvmCpuLoad()) {
                builder.jvmCpuLoad(cpuLoad.getJvmCpuLoad());
            }
            // systemCpuLoad is optional
            if (cpuLoad.isSetSystemCpuLoad()) {
                builder.systemCpuLoad(cpuLoad.getSystemCpuLoad());
            }
        }
        return builder.build();
    }

}
