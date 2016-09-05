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

import com.navercorp.pinpoint.collector.dao.AgentStatDao;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
@Deprecated
@Service("agentStatHandler")
public class AgentStatHandler implements Handler {

    private final Logger logger = LoggerFactory.getLogger(AgentStatHandler.class.getName());

    @Autowired
    private AgentStatDao agentStatDao;

    @Override
    public void handle(TBase<?, ?> tbase) {
        // FIXME (2014.08) Legacy - TAgentStats should not be sent over the wire.
        if (tbase instanceof TAgentStat) {
            final TAgentStat agentStat = (TAgentStat)tbase;
            String agentId = agentStat.getAgentId();
            long startTimestamp = agentStat.getStartTimestamp();
            handleAgentStat(agentId, startTimestamp, agentStat);
        } else if (tbase instanceof TAgentStatBatch) {
            handleAgentStatBatch((TAgentStatBatch)tbase);
        } else {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + TAgentStat.class.getName() + " or " + TAgentStatBatch.class.getName());
        }
    }

    private <T extends TAgentStat> void handleAgentStat(String agentId, long startTimestamp, T agentStat) {
        try {
            agentStat.setAgentId(agentId);
            agentStat.setStartTimestamp(startTimestamp);
            agentStatDao.insert(agentStat);
        } catch (Exception e) {
            logger.warn("AgentStat handle error. Caused:{}", e.getMessage());
        }
    }

    private <T extends TAgentStatBatch> void handleAgentStatBatch(T agentStatBatch) {
        if (logger.isDebugEnabled()) {
            logger.debug("Received AgentStats={}", agentStatBatch);
        }
        String agentId = agentStatBatch.getAgentId();
        long startTimestamp = agentStatBatch.getStartTimestamp();
        for (TAgentStat agentStat : agentStatBatch.getAgentStats()) {
            handleAgentStat(agentId, startTimestamp, agentStat);
        }
    }
}
