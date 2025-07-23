/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * @author intr3p1d
 */
@ConditionalOnMissingBean(value = AgentInfoStatisticsService.class, ignored = EmptyAgentInfoStatisticsService.class)
@Service
public class EmptyAgentInfoStatisticsService implements AgentInfoStatisticsService {
    @Override
    public void insert(Supplier<ServiceUid> serviceUidSupplier, Supplier<ApplicationUid> applicationUidSupplier, AgentInfoBo agentInfoBo) {
        // do nothing
    }
}
