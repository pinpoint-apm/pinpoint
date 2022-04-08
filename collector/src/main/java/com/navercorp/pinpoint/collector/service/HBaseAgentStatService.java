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
package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.AgentStatDao;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Service("hBaseAgentStatService")
public class HBaseAgentStatService implements AgentStatService {

    private final Logger logger = LogManager.getLogger(HBaseAgentStatService.class.getName());

    private final AgentStatDao<?>[] agentStatDaoList;

    public HBaseAgentStatService(AgentStatDao<?>[] agentStatDaoList) {
        this.agentStatDaoList = Objects.requireNonNull(agentStatDaoList, "agentStatDaoList");

        for (AgentStatDao<?> agentStatDao : agentStatDaoList) {
            logger.info("AgentStatDaoV2:{}", agentStatDao.getClass().getSimpleName());
        }
    }

    @Override
    public void save(AgentStatBo agentStatBo) {
        for (AgentStatDao<?> agentStatDao : agentStatDaoList) {
            try {
                agentStatDao.dispatch(agentStatBo);
            } catch (Exception e) {
                logger.warn("Error inserting AgentStatBo. Caused:{}", e.getMessage(), e);
            }
        }
    }

}
