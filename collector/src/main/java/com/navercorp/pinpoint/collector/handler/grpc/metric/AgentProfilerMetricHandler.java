/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.handler.grpc.metric;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.handler.grpc.GrpcMetricHandler;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentProfilerMetricMapper;
import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.common.server.bo.stat.ProfilerMetricBo;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.PProfilerMetric;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AgentProfilerMetricHandler implements GrpcMetricHandler {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final GrpcAgentProfilerMetricMapper agentProfilerMetricMapper;
    private final AgentStatService[] agentStatServiceList;

    public AgentProfilerMetricHandler(GrpcAgentProfilerMetricMapper agentProfilerMetricMapper,
                                  AgentStatService[] agentStatServiceList) {
        this.agentProfilerMetricMapper = Objects.requireNonNull(agentProfilerMetricMapper, "agentProfilerMetricMapper");
        this.agentStatServiceList = Objects.requireNonNull(agentStatServiceList, "agentStatServiceList");

        for (AgentStatService service : this.agentStatServiceList) {
            logger.info("{}:{}", AgentStatService.class.getSimpleName(), service.getClass().getSimpleName());
        }
    }

    @Override
    public boolean accept(GeneratedMessageV3 message) {
        return message instanceof PProfilerMetric;
    }

    @Override
    public void handle(GeneratedMessageV3 message) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handle PProfilerMetric={}", MessageFormatUtils.debugLog(message));
        }

        final PProfilerMetric profilerMetric = (PProfilerMetric) message;
        final ProfilerMetricBo profilerMetricBo = this.agentProfilerMetricMapper.map(profilerMetric);
        if (profilerMetricBo == null) {
            return;
        }

        handleProfilerMetric(profilerMetricBo);
    }

    private void handleProfilerMetric(ProfilerMetricBo profilerMetricBo) {
        for (AgentStatService agentStatService : agentStatServiceList) {
            try {
                agentStatService.save(profilerMetricBo);
            } catch (Exception e) {
                logger.warn("Failed to handle service={}, AgentStatBo={}", agentStatService, profilerMetricBo, e);
            }
        }
    }
}
