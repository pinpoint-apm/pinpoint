/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.mapper.grpc.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PCpuLoad;
import org.springframework.stereotype.Component;

/**
 * @author HyunGil Jeong
 */
@Component
public class GrpcCpuLoadBoMapper implements GrpcStatMapper {

    public CpuLoadBo map(final PCpuLoad cpuLoad) {
        final CpuLoadBo cpuLoadBo = new CpuLoadBo();
        cpuLoadBo.setJvmCpuLoad(cpuLoad.getJvmCpuLoad());
        cpuLoadBo.setSystemCpuLoad(cpuLoad.getSystemCpuLoad());
        return cpuLoadBo;
    }

    @Override
    public void map(AgentStatBo.Builder.StatBuilder builder, PAgentStat agentStat) {
        // cpuLoad
        if (agentStat.hasCpuLoad()) {
            final PCpuLoad cpuLoad = agentStat.getCpuLoad();
            final CpuLoadBo cpuLoadBo = this.map(cpuLoad);
            builder.addCpuLoad(cpuLoadBo);
        }
    }
}
