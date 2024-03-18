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

import com.navercorp.pinpoint.collector.dao.AgentInfoDao;
import com.navercorp.pinpoint.collector.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * @author emeroad
 * @author koo.taejin
 * @author jaehong.kim
 */
@Service
@Validated
public class AgentInfoService {

    private final AgentInfoDao agentInfoDao;
    private final ApplicationIndexDao applicationIndexDao;
    private final ServiceInfoService serviceInfoService;

    public AgentInfoService(
            AgentInfoDao agentInfoDao,
            ApplicationIndexDao applicationIndexDao,
            ServiceInfoService serviceInfoService
    ) {
        this.agentInfoDao = Objects.requireNonNull(agentInfoDao, "agentInfoDao");
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.serviceInfoService = Objects.requireNonNull(serviceInfoService, "serviceInfoService");
    }

    public void insert(@Valid final AgentInfoBo agentInfoBo) {
        agentInfoDao.insert(agentInfoBo);
        applicationIndexDao.insert(agentInfoBo);
        serviceInfoService.insertAgentInfo(agentInfoBo);
    }

    public AgentInfoBo getAgentInfo(AgentId agentId, @PositiveOrZero final long timestamp) {
        return agentInfoDao.getAgentInfo(agentId, timestamp);
    }
}
