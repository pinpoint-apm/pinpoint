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
import com.navercorp.pinpoint.common.server.bo.stat.DataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PJvmGc;
import com.navercorp.pinpoint.grpc.trace.PJvmGcDetailed;
import org.springframework.stereotype.Component;

/**
 * @author HyunGil Jeong
 */
@Component
public class GrpcJvmGcDetailedBoMapper implements GrpcStatMapper {

    public JvmGcDetailedBo map(DataPoint point, final PJvmGcDetailed jvmGcDetailed) {
        return new JvmGcDetailedBo(point,
                jvmGcDetailed.getJvmGcNewCount(),
                jvmGcDetailed.getJvmGcNewTime(),
                jvmGcDetailed.getJvmPoolCodeCacheUsed(),

                jvmGcDetailed.getJvmPoolNewGenUsed(),
                jvmGcDetailed.getJvmPoolOldGenUsed(),

                jvmGcDetailed.getJvmPoolSurvivorSpaceUsed(),
                jvmGcDetailed.getJvmPoolPermGenUsed(),
                jvmGcDetailed.getJvmPoolMetaspaceUsed());
    }

    @Override
    public void map(AgentStatBo.Builder.StatBuilder builder, PAgentStat agentStat) {
        // jvmGc
        if (agentStat.hasGc()) {
            final PJvmGc jvmGc = agentStat.getGc();

            // jvmGcDetailed
            if (jvmGc.hasJvmGcDetailed()) {
                final PJvmGcDetailed jvmGcDetailed = jvmGc.getJvmGcDetailed();
                DataPoint point = builder.getDataPoint();
                final JvmGcDetailedBo jvmGcDetailedBo = this.map(point, jvmGcDetailed);
                builder.addPoint(jvmGcDetailedBo);
            }
        }
    }
}