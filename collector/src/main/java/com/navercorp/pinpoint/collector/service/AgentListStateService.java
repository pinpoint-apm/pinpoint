/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.collector.dao.AgentIdDao;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AgentListStateService {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final ThrottledLogger tLogger = ThrottledLogger.getLogger(logger, 100000);

    private final AgentIdDao agentIdDao;
    private final boolean v2enabled;

    public AgentListStateService(AgentIdDao agentIdDao,
                                 @Value("${pinpoint.collector.application.index.v2.enabled:false}") boolean v2enabled) {
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.v2enabled = v2enabled;
    }

    public void update(int serviceUid, String applicationName, int serviceTypeCode, String agentId, long agentStartTime,
                       AgentLifeCycleState agentLifeCycleState, long eventTimestamp) {
        if (!v2enabled) {
            return;
        }
        if (serviceTypeCode == ServiceType.UNDEFINED.getCode()) {
            tLogger.info("Skip updateState. undefined serviceType. serviceUid={}, applicationName={}, agentId={}, agentStartTime={}, eventTimestamp={}", serviceUid, applicationName, agentId, agentStartTime, eventTimestamp);
            return;
        }
        agentIdDao.updateState(serviceUid, applicationName, serviceTypeCode, agentId, agentStartTime, eventTimestamp, agentLifeCycleState);
    }

}
