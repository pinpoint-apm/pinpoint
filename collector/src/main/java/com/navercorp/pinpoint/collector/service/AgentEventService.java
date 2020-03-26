/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.AgentEventDao;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AgentEventService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AgentEventDao agentEventDao;

    public AgentEventService(AgentEventDao agentEventDao) {
        this.agentEventDao = Objects.requireNonNull(agentEventDao, "agentEventDao");
    }

    public void insert(final AgentEventBo agentEventBo) {
        Objects.requireNonNull(agentEventBo, "agentEventBo");
        if (logger.isDebugEnabled()) {
            logger.debug("Insert {}", agentEventBo);
        }
        agentEventDao.insert(agentEventBo);
    }
}