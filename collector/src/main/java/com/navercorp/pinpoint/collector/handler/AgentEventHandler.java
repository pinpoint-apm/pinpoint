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

package com.navercorp.pinpoint.collector.handler;

import com.navercorp.pinpoint.collector.mapper.thrift.event.AgentEventBatchMapper;
import com.navercorp.pinpoint.collector.mapper.thrift.event.AgentEventMapper;
import com.navercorp.pinpoint.collector.service.AgentEventService;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Taejin Koo
 */
@Service("agentEventHandler")
public class AgentEventHandler implements SimpleHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AgentEventMapper agentEventMapper;

    @Autowired
    private AgentEventBatchMapper agentEventBatchMapper;

    @Autowired
    private AgentEventService agentEventService;

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

        AgentEventBo agentEventBo = this.agentEventMapper.map(tAgentStat);
        if (agentEventBo == null) {
            return;
        }

        agentEventService.service(agentEventBo);
    }

    private void handleAgentStatBatch(TAgentStatBatch tAgentStatBatch) {
        if (logger.isDebugEnabled()) {
            logger.debug("Received TAgentStatBatch={}", tAgentStatBatch);
        }

        List<AgentEventBo> agentEventBoList = this.agentEventBatchMapper.map(tAgentStatBatch);
        if (CollectionUtils.isEmpty(agentEventBoList)) {
            return;
        }

        for (AgentEventBo agentEventBo : agentEventBoList) {
            agentEventService.service(agentEventBo);
        }
    }

}
