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

import com.navercorp.pinpoint.collector.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.collector.dao.ApplicationIndexPerTimeDao;
import com.navercorp.pinpoint.collector.service.async.AgentProperty;
import com.navercorp.pinpoint.common.server.bo.AgentLifeCycleBo;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Objects;

@Service
@Validated
public class AgentLifeCycleService {

    private final AgentLifeCycleDao agentLifeCycleDao;

    private final ApplicationIndexPerTimeDao applicationIndexPerTimeDao;

    public AgentLifeCycleService(AgentLifeCycleDao agentLifeCycleDao, ApplicationIndexPerTimeDao applicationIndexPerTimeDao) {
        this.agentLifeCycleDao = Objects.requireNonNull(agentLifeCycleDao, "agentLifeCycleDao");
        this.applicationIndexPerTimeDao = Objects.requireNonNull(applicationIndexPerTimeDao, "applicationIndexPerTimeDao");
    }

    public void insert(@Valid final AgentLifeCycleBo agentLifeCycleBo, AgentProperty agentProperty) {
        this.agentLifeCycleDao.insert(agentLifeCycleBo);
        this.applicationIndexPerTimeDao.insert(agentLifeCycleBo, agentProperty);
    }

}
