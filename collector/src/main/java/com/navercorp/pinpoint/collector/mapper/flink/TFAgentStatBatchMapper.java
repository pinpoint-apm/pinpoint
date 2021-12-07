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
package com.navercorp.pinpoint.collector.mapper.flink;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStatBatch;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Component
public class TFAgentStatBatchMapper {
    private final Logger logger = LogManager.getLogger(this.getClass());
    public final TFAgentStatMapper tFAgentStatMapper;

    public TFAgentStatBatchMapper(TFAgentStatMapper tFAgentStatMapper) {
        this.tFAgentStatMapper = Objects.requireNonNull(tFAgentStatMapper, "tFAgentStatMapper");
    }

    public TFAgentStatBatch map(AgentStatBo agentStatBo) {
        try {
            List<TFAgentStat> tFAgentstatList = tFAgentStatMapper.map(agentStatBo);
            long startTimestamp = getStartTimestamp(agentStatBo);
            TFAgentStatBatch tFAgentStatBatch = new TFAgentStatBatch(agentStatBo.getAgentId(), startTimestamp, tFAgentstatList);
            return tFAgentStatBatch;
        } catch (Exception e) {
            logger.error("not create thrift object to send flink server. : " + agentStatBo, e);
        }

        return null;
    }

    private long getStartTimestamp(AgentStatBo agentStatBo) {
        List<CpuLoadBo> cpuLoadBos = agentStatBo.getCpuLoadBos();

        if (CollectionUtils.hasLength(cpuLoadBos)) {
            CpuLoadBo cpuLoadBo = cpuLoadBos.get(0);

            if (cpuLoadBo != null) {
                return cpuLoadBo.getStartTimestamp();
            }
        }

        return Long.MIN_VALUE;
    }
}
