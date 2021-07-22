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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinAgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinResponseTimeBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFResponseTime;

/**
 * @author minwoo.jung
 */
public class JoinResponseTimeBoMapper implements ThriftStatMapper<JoinResponseTimeBo, TFAgentStat> {

    public JoinResponseTimeBo map(TFAgentStat tFAgentStat) {
        if (!tFAgentStat.isSetResponseTime()) {
            return JoinResponseTimeBo.EMPTY_JOIN_RESPONSE_TIME_BO;
        }

        final String agentId = tFAgentStat.getAgentId();
        final TFResponseTime tFResponseTime = tFAgentStat.getResponseTime();
        final long avg = tFResponseTime.getAvg();

        JoinResponseTimeBo joinResponseTimeBo = new JoinResponseTimeBo();
        joinResponseTimeBo.setId(agentId);
        joinResponseTimeBo.setTimestamp(tFAgentStat.getTimestamp());
        joinResponseTimeBo.setResponseTimeJoinValue(new JoinLongFieldBo(avg, avg, agentId, avg, agentId));

        return joinResponseTimeBo;
    }


    @Override
    public void build(TFAgentStat tFAgentStat, JoinAgentStatBo.Builder builder) {
        JoinResponseTimeBo joinResponseTimeBo = this.map(tFAgentStat);

        if (joinResponseTimeBo == joinResponseTimeBo.EMPTY_JOIN_RESPONSE_TIME_BO) {
            return;
        }

        builder.addResponseTime(joinResponseTimeBo);
    }
}
