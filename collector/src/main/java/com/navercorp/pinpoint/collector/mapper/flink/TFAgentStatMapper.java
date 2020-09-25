/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.collector.mapper.flink;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author minwoo.jung
 */
@Component
public class TFAgentStatMapper {
    private final FlinkStatMapper<?, ?>[] mappers;

    public TFAgentStatMapper(FlinkStatMapper<?, ?>[] mappers) {
        this.mappers = Objects.requireNonNull(mappers, "mappers");
    }

    public List<TFAgentStat> map(AgentStatBo agentStatBo) {

        TFAgentStatBuilder agentStat = new TFAgentStatBuilder(agentStatBo);
        return agentStat.build();
    }

    public class TFAgentStatBuilder {

        private final Map<Long, TFAgentStat> tFAgentStatMap = new TreeMap<>();
        private final AgentStatBo agentStat;

        public TFAgentStatBuilder(AgentStatBo agentStat) {
            this.agentStat = Objects.requireNonNull(agentStat, "agentStat");
        }

        public AgentStatBo getAgentStat() {
            return agentStat;
        }

        public <T extends AgentStatDataPoint> void build(List<T> dataPointList, FlinkStatMapper<T, TFAgentStat> mapper) {
            if (dataPointList == null) {
                return;
            }
            for (T point : dataPointList) {
                TFAgentStat tFAgentStat = getOrCreateTFAgentStat(point.getTimestamp());
                mapper.map(point, tFAgentStat);
            }
        }

        private TFAgentStat getOrCreateTFAgentStat(long timestamp) {
            TFAgentStat tFAgentStat = tFAgentStatMap.get(timestamp);
            if (tFAgentStat == null) {
                tFAgentStat = new TFAgentStat();
                tFAgentStat.setAgentId(this.agentStat.getAgentId());
                tFAgentStat.setStartTimestamp(this.agentStat.getStartTimestamp());
                tFAgentStat.setTimestamp(timestamp);
                tFAgentStatMap.put(timestamp, tFAgentStat);
            }

            return tFAgentStat;
        }

        public List<TFAgentStat> build() {
            for (FlinkStatMapper<?, ?> mapper : mappers) {
                mapper.build(this);
            }
            return new ArrayList<>(tFAgentStatMap.values());
        }
    }
}
