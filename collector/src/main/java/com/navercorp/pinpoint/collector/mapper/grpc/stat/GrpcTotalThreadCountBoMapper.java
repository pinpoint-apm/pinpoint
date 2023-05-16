/*
 * Copyright 2020 NAVER Corp.
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
import com.navercorp.pinpoint.common.server.bo.stat.TotalThreadCountBo;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PTotalThread;
import org.springframework.stereotype.Component;

@Component
public class GrpcTotalThreadCountBoMapper implements GrpcStatMapper {
    public TotalThreadCountBo map(final PTotalThread tTotalThread) {
        final TotalThreadCountBo totalThreadCountBo = new TotalThreadCountBo();
        totalThreadCountBo.setTotalThreadCount(tTotalThread.getTotalThreadCount());
        return totalThreadCountBo;
    }

    @Override
    public void map(AgentStatBo.Builder.StatBuilder builder, PAgentStat agentStat) {
        // totalThreadCount
        if (agentStat.hasTotalThread()) {
            final PTotalThread totalThread = agentStat.getTotalThread();
            final TotalThreadCountBo totalThreadCountBo = this.map(totalThread);
            builder.addTotalThreadCount(totalThreadCountBo);
        }
    }
}
