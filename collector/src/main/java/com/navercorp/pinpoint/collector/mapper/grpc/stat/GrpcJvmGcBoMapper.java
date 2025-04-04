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

import com.navercorp.pinpoint.common.server.bo.JvmGcType;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PJvmGc;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Component
public class GrpcJvmGcBoMapper implements GrpcStatMapper {

    private final GrpcJvmGcTypeMapper jvmGcTypeMapper;

    public GrpcJvmGcBoMapper(GrpcJvmGcTypeMapper jvmGcTypeMapper) {
        this.jvmGcTypeMapper = Objects.requireNonNull(jvmGcTypeMapper, "jvmGcTypeMapper");
    }

    public JvmGcBo map(DataPoint point, final PJvmGc jvmGc) {
        JvmGcType gcType = this.jvmGcTypeMapper.map(jvmGc.getType());
        return new JvmGcBo(point, gcType,
                jvmGc.getJvmMemoryHeapUsed(),
                jvmGc.getJvmMemoryHeapMax(),
                jvmGc.getJvmMemoryNonHeapUsed(),
                jvmGc.getJvmMemoryNonHeapMax(),
                jvmGc.getJvmGcOldCount(),
                jvmGc.getJvmGcOldTime());
    }

    @Override
    public void map(AgentStatBo.Builder.StatBuilder builder, PAgentStat agentStat) {
        // jvmGc
        if (agentStat.hasGc()) {
            final PJvmGc jvmGc = agentStat.getGc();
            DataPoint point = builder.getDataPoint();
            final JvmGcBo jvmGcBo = this.map(point, jvmGc);
            builder.addPoint(jvmGcBo);
        }
    }
}