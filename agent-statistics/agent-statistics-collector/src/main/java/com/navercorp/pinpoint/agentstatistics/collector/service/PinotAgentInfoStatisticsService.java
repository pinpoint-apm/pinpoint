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
package com.navercorp.pinpoint.agentstatistics.collector.service;

import com.navercorp.pinpoint.agentstatistics.collector.dao.AgentInfoStatisticsDao;
import com.navercorp.pinpoint.collector.service.AgentInfoStatisticsService;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author intr3p1d
 */
@Service
@ConditionalOnProperty(
        name = "pinpoint.modules.collector.agent-statistics.enabled",
        havingValue = "true"
)
@Validated
public class PinotAgentInfoStatisticsService implements AgentInfoStatisticsService {


    private final AgentInfoStatisticsDao agentInfoStatisticsDao;
    private final TenantProvider tenantProvider;

    public PinotAgentInfoStatisticsService(
            AgentInfoStatisticsDao agentInfoStatisticsDao,
            TenantProvider tenantProvider
    ) {
        this.agentInfoStatisticsDao = Objects.requireNonNull(agentInfoStatisticsDao, "agentInfoStatisticsDao");
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
    }

    @Override
    public void insert(
            Supplier<ServiceUid> serviceUidSupplier,
            Supplier<ApplicationUid> applicationUidSupplier,
            AgentInfoBo agentInfoBo
    ) {
        Objects.requireNonNull(serviceUidSupplier, "serviceUidSupplier");
        Objects.requireNonNull(applicationUidSupplier, "applicationUidSupplier");
        Objects.requireNonNull(agentInfoBo, "agentInfoBo");

        final String tenantId = tenantProvider.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant ID is not set");
        }

        agentInfoStatisticsDao.insert(
                null, // TODO: will be replaced with serviceUidSupplier.get()
                null, // TODO: will be replaced with applicationUidSupplier.get()
                agentInfoBo
        );
    }
}
