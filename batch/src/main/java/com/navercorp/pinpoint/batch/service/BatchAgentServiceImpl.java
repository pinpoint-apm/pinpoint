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

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.service.component.ActiveAgentValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
@Service
public class BatchAgentServiceImpl implements BatchAgentService {

    private final ActiveAgentValidator activeAgentValidator;
    private final BatchApplicationIndexService batchApplicationIndexService;

    public BatchAgentServiceImpl(
            ActiveAgentValidator activeAgentValidator,
            BatchApplicationIndexService batchApplicationIndexService) {
        this.activeAgentValidator = Objects.requireNonNull(activeAgentValidator, "activeAgentValidator");
        this.batchApplicationIndexService = Objects.requireNonNull(batchApplicationIndexService, "batchApplicationIndexService");
    }

    @Override
    public List<String> getIds(String applicationName) {
        return this.batchApplicationIndexService.selectAgentIds(applicationName);
    }

    @Override
    public boolean isActive(String agentId, Range range) {
        return this.activeAgentValidator.isActiveAgent(agentId, range);
    }

    @Override
    public boolean isActive(String agentId, int agentServiceType, Range range) {
        return this.activeAgentValidator.isActiveAgent(agentId, agentServiceType, range);
    }
}
