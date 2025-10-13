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

import com.navercorp.pinpoint.collector.dao.AgentInfoDao;
import com.navercorp.pinpoint.collector.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author emeroad
 * @author koo.taejin
 * @author jaehong.kim
 */
@Service
public class AgentInfoService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentInfoDao agentInfoDao;
    private final ApplicationIndexDao applicationIndexDao;
    private final boolean applicationIndexV1Enabled;

    public AgentInfoService(AgentInfoDao agentInfoDao, ApplicationIndexDao applicationIndexDao,
                            @Value("${pinpoint.collector.application.index.v1.enabled:true}") boolean applicationIndexV1Enabled) {
        this.agentInfoDao = Objects.requireNonNull(agentInfoDao, "agentInfoDao");
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.applicationIndexV1Enabled = applicationIndexV1Enabled;
    }

    public void insert(final AgentInfoBo agentInfoBo) {
        agentInfoDao.insert(agentInfoBo);
        if (applicationIndexV1Enabled) {
            applicationIndexDao.insert(agentInfoBo);
        }
    }

}