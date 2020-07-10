/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.common.server.bo.metric.AgentCustomMetricBo;
import com.navercorp.pinpoint.common.server.bo.metric.EachCustomMetricBo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Taejin Koo
 */
@Service
public class AgentCustomMetricDispatchService {

    private final Logger logger = LoggerFactory.getLogger(HBaseAgentStatService.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    @Autowired
    private AgentCustomMetricServiceLocator agentCustomMetricServiceLocator;

    public void save(AgentCustomMetricBo agentCustomMetricBo) {
        String agentId = agentCustomMetricBo.getAgentId();

        for (final AgentCustomMetricService agentCustomMetricService : agentCustomMetricServiceLocator.getAgentCustomMetricService()) {
            if (agentCustomMetricService.isSupport(agentCustomMetricBo)) {
                if (isDebug) {
                    logger.debug("{} save started.", agentCustomMetricService);
                }
                final List<EachCustomMetricBo> eachCustomMetricBoList = agentCustomMetricService.map(agentCustomMetricBo);
                agentCustomMetricService.save(agentId, eachCustomMetricBoList);
            } else {
                if (isDebug) {
                    logger.debug("{} save skipped.", agentCustomMetricService);
                }
            }
        }
    }

}
