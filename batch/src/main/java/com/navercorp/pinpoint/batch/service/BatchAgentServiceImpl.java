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
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.ApplicationService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
public class BatchAgentServiceImpl implements BatchAgentService {

    private final ApplicationService applicationService;
    private final AgentInfoService agentInfoService;

    public BatchAgentServiceImpl(
            ApplicationService applicationService,
            AgentInfoService agentInfoService
    ) {
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
    }

    @Override
    public List<String> getIds(UUID applicationId) {
        return this.applicationService.getAgents(applicationId);
    }

    @Override
    public boolean isActive(String agentId, Range range) {
        return this.agentInfoService.isActiveAgent(agentId, range);
    }

    @Override
    public void remove(UUID applicationId, String agentId) {
        this.applicationService.deleteAgent(applicationId, agentId);
    }
}
