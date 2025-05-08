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
import com.navercorp.pinpoint.collector.dao.AgentListDao;
import com.navercorp.pinpoint.collector.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.io.request.UidException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author emeroad
 * @author koo.taejin
 * @author jaehong.kim
 */
@Service
@Validated
public class AgentInfoService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentInfoDao agentInfoDao;
    private final ApplicationIndexDao applicationIndexDao;

    private final AgentListDao agentListDao;
    private final boolean agentListEnable;

    public AgentInfoService(AgentInfoDao agentInfoDao, ApplicationIndexDao applicationIndexDao, Optional<AgentListDao> agentListDao,
                            @Value("${pinpoint.collector.application.uid.agent.list.enable:false}") boolean agentListEnable) {
        this.agentInfoDao = Objects.requireNonNull(agentInfoDao, "agentInfoDao");
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.agentListDao = agentListDao.orElse(null);
        this.agentListEnable = agentListEnable;
    }

    public void insert(Supplier<ServiceUid> serviceUidSupplier, Supplier<ApplicationUid> applicationUidSupplier, @Valid final AgentInfoBo agentInfoBo) {
        agentInfoDao.insert(agentInfoBo);
        applicationIndexDao.insert(agentInfoBo);
        uidAgentListInsert(serviceUidSupplier, applicationUidSupplier, agentInfoBo);
    }

    private void uidAgentListInsert(Supplier<ServiceUid> serviceUidSupplier, Supplier<ApplicationUid> applicationUidSupplier, AgentInfoBo agentInfoBo) {
        if (!agentListEnable || agentListDao == null) {
            return;
        }
        try {
            ServiceUid serviceUid = serviceUidSupplier.get();
            ApplicationUid applicationUid = applicationUidSupplier.get();
            agentListDao.insert(serviceUid, applicationUid, agentInfoBo.getAgentId(), agentInfoBo.getStartTime(), agentInfoBo.getAgentName());
        } catch (UidException ignore) {
            // ignore UidException on unary
        } catch (Exception e) {
            logger.warn("Failed to insert uid agent list. agentId: {}, startTime: {}, agentName: {}", agentInfoBo.getAgentId(), agentInfoBo.getStartTime(), agentInfoBo.getAgentName(), e);
        }
    }

    public AgentInfoBo getSimpleAgentInfo(@NotBlank final String agentId, @PositiveOrZero final long timestamp) {
        return agentInfoDao.getSimpleAgentInfo(agentId, timestamp);
    }
}