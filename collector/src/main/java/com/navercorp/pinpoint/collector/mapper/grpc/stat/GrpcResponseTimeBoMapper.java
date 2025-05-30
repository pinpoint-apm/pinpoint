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
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PResponseTime;
import org.springframework.stereotype.Component;

/**
 * @author Taejin Koo
 */
@Component
public class GrpcResponseTimeBoMapper implements GrpcStatMapper {

    public ResponseTimeBo map(DataPoint point, final PResponseTime tResponseTime) {
        return new ResponseTimeBo(point,
                tResponseTime.getAvg(),
                tResponseTime.getMax());
    }

    @Override
    public void map(AgentStatBo.Builder.StatBuilder builder, PAgentStat agentStat) {
        // response time
        if (agentStat.hasResponseTime()) {
            final PResponseTime responseTime = agentStat.getResponseTime();
            DataPoint point = builder.getDataPoint();
            final ResponseTimeBo responseTimeBo = this.map(point, responseTime);
            builder.addPoint(responseTimeBo);
        }
    }
}