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

package com.navercorp.pinpoint.inspector.collector.service;

import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.inspector.collector.dao.AgentStatDao;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Service("pinotAgentStatService")
public class PinotAgentStatService implements AgentStatService {

    private final Logger logger = LogManager.getLogger(PinotAgentStatService.class.getName());

    private final AgentStatDao<?>[] agentStatDaoList;

    public PinotAgentStatService(AgentStatDao<?>[] agentStatDaoList) {
        this.agentStatDaoList = Objects.requireNonNull(agentStatDaoList, "agentStatDaoList");

        for (AgentStatDao<?> agentStatDao : agentStatDaoList) {
            logger.info("AgentStatDaoV2:{}", agentStatDao.getClass().getSimpleName());
        }
    }

    @Override
    public void save(@Valid AgentStatBo agentStatBo) {
        for (AgentStatDao agentStatDao : agentStatDaoList) {
            try {
                agentStatDao.dispatch(agentStatBo);
            } catch (Exception e) {
                logger.warn("Error inserting AgentStatBo to pinot. Caused:{}", e.getMessage(), e);
            }

        }
    }
}
