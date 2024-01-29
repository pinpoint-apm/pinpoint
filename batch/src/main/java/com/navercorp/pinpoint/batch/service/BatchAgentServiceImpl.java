/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch.service;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.service.AgentInfoService;

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class BatchAgentServiceImpl implements BatchAgentService {

    private final ApplicationIndexDao applicationIndexDao;
    private final AgentInfoService agentInfoService;

    public BatchAgentServiceImpl(
            ApplicationIndexDao applicationIndexDao,
            AgentInfoService agentInfoService
    ) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
    }

    @Override
    public List<String> getIds(String applicationName) {
        return this.applicationIndexDao.selectAgentIds(applicationName);
    }

    @Override
    public boolean isActive(String agentId, Range range) {
        return this.agentInfoService.isActiveAgent(agentId, range);
    }

    @Override
    public void remove(String applicationName, String agentId) {
        this.applicationIndexDao.deleteAgentId(applicationName, agentId);
    }
}
