/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.handler;

import com.navercorp.pinpoint.collector.mapper.thrift.stat.AgentStatBatchMapper;
import com.navercorp.pinpoint.collector.mapper.thrift.stat.AgentStatMapper;
import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
@Service("agentStatHandlerV2")
public class AgentStatHandlerV2 implements SimpleHandler {

    private final Logger logger = LoggerFactory.getLogger(AgentStatHandlerV2.class.getName());

    @Autowired
    private AgentStatMapper agentStatMapper;

    @Autowired
    private AgentStatBatchMapper agentStatBatchMapper;

    @Autowired(required = false)
    private List<AgentStatService> agentStatServiceList = Collections.emptyList();

    @Override
    public void handleSimple(TBase<?, ?> tBase) {
        // FIXME (2014.08) Legacy - TAgentStat should not be sent over the wire.
        if (tBase instanceof TAgentStat) {
            TAgentStat tAgentStat = (TAgentStat)tBase;
            this.handleAgentStat(tAgentStat);
        } else if (tBase instanceof TAgentStatBatch) {
            TAgentStatBatch tAgentStatBatch = (TAgentStatBatch) tBase;
            this.handleAgentStatBatch(tAgentStatBatch);
        } else {
            throw new IllegalArgumentException("unexpected tbase:" + tBase + " expected:" + TAgentStat.class.getName() + " or " + TAgentStatBatch.class.getName());
        }
    }

    private void handleAgentStat(TAgentStat tAgentStat) {
        if (logger.isDebugEnabled()) {
            logger.debug("Received TAgentStat={}", tAgentStat);
        }

        AgentStatBo agentStatBo = this.agentStatMapper.map(tAgentStat);

        if (agentStatBo == null) {
            return;
        }
        for (AgentStatService agentStatService : agentStatServiceList) {
            agentStatService.save(agentStatBo);
        }
    }

    private void handleAgentStatBatch(TAgentStatBatch tAgentStatBatch) {
        if (logger.isDebugEnabled()) {
            logger.debug("Received TAgentStatBatch={}", tAgentStatBatch);
        }

        AgentStatBo agentStatBo = this.agentStatBatchMapper.map(tAgentStatBatch);

        if (agentStatBo == null) {
            return;
        }
        for (AgentStatService agentStatService : agentStatServiceList) {
            agentStatService.save(agentStatBo);
        }
    }

}
