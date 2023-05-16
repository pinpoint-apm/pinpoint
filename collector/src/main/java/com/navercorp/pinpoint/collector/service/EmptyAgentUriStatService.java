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

import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

/**
 * @author Taejin Koo
 */
@ConditionalOnMissingBean(value = AgentUriStatService.class, ignored = EmptyAgentUriStatService.class)
@Service
public class EmptyAgentUriStatService implements AgentUriStatService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public void save(AgentUriStatBo agentUriStatBo) {
        if (logger.isDebugEnabled()) {
            logger.debug("save {}", agentUriStatBo);
        }
    }
}
