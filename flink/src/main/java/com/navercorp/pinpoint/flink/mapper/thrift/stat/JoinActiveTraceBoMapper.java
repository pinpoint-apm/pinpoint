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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinActiveTraceBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.thrift.dto.flink.TFActiveTrace;
import com.navercorp.pinpoint.thrift.dto.flink.TFActiveTraceHistogram;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class JoinActiveTraceBoMapper {

    public JoinActiveTraceBo map(TFAgentStat tFAgentStat) {
        if (!tFAgentStat.isSetActiveTrace()) {
            return JoinActiveTraceBo.EMPTY_JOIN_ACTIVE_TRACE_BO;
        }

        final TFActiveTrace tFactiveTrace = tFAgentStat.getActiveTrace();
        final String agentId = tFAgentStat.getAgentId();

        if (tFactiveTrace.isSetHistogram() == false) {
            return JoinActiveTraceBo.EMPTY_JOIN_ACTIVE_TRACE_BO;
        }

        final TFActiveTraceHistogram histogram = tFactiveTrace.getHistogram();
        final int totalCount = calculateTotalCount(tFactiveTrace.getHistogram());

        JoinActiveTraceBo joinActiveTraceBo = new JoinActiveTraceBo();
        joinActiveTraceBo.setId(agentId);
        joinActiveTraceBo.setTimestamp(tFAgentStat.getTimestamp());
        joinActiveTraceBo.setHistogramSchemaType(histogram.getHistogramSchemaType());
        joinActiveTraceBo.setVersion(histogram.getVersion());
        joinActiveTraceBo.setTotalCount(totalCount);
        joinActiveTraceBo.setMaxTotalCount(totalCount);
        joinActiveTraceBo.setMaxTotalCountAgentId(agentId);
        joinActiveTraceBo.setMinTotalCount(totalCount);
        joinActiveTraceBo.setMinTotalCountAgentId(agentId);
        return joinActiveTraceBo;
    }

    private int calculateTotalCount(TFActiveTraceHistogram histogram) {
        int totalCount = 0;

        List<Integer> activeTraceCountList = histogram.getActiveTraceCount();

        if (CollectionUtils.isEmpty(activeTraceCountList)) {
            return totalCount;
        }
        for (int activeTraceCount : activeTraceCountList) {
            totalCount += activeTraceCount;
        }

        return totalCount;
    }
}
