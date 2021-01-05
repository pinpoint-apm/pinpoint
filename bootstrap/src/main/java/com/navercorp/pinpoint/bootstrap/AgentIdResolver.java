/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap;

import com.navercorp.pinpoint.bootstrap.agentdir.Assert;
import com.navercorp.pinpoint.common.util.AgentUuidUtils;
import com.navercorp.pinpoint.common.util.StringUtils;


import java.util.List;
import java.util.UUID;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentIdResolver {
    public static final String APPLICATION_NAME = "applicationName";
    public static final String AGENT_ID = "agentId";

    public static final String SYSTEM_PROPERTY_PREFIX = "pinpoint.";
    public static final String APPLICATION_NAME_SYSTEM_PROPERTY = SYSTEM_PROPERTY_PREFIX + "applicationName";
    public static final String AGENT_ID_SYSTEM_PROPERTY = SYSTEM_PROPERTY_PREFIX + "agentId";
    
    public static final String ENV_PROPERTY_PREFIX = "PINPOINT_";
    public static final String APPLICATION_NAME_ENV_PROPERTY = ENV_PROPERTY_PREFIX + "APPLICATION_NAME";
    public static final String AGENT_ID_ENV_PROPERTY = ENV_PROPERTY_PREFIX + "AGENT_ID";

    private final BootLogger logger = BootLogger.getLogger(this.getClass());

    private final List<AgentProperties> agentPropertyList;

    private final IdValidator idValidator = new IdValidator();

    public AgentIdResolver(List<AgentProperties> agentPropertyList) {
        this.agentPropertyList = Assert.requireNonNull(agentPropertyList, "agentPropertyList");
    }

    public AgentIds resolve() {
        String agentId = getAgentId();
        if (StringUtils.isEmpty(agentId)) {
            logger.info("Failed to resolve AgentId(-Dpinpoint.agentId)");
            agentId = newRandomAgentId();
            logger.info("Auto generate AgentId='" + agentId + "'");
        }

        final String applicationName = getApplicationName();
        if (StringUtils.isEmpty(applicationName)) {
            logger.warn("Failed to resolve ApplicationName(-Dpinpoint.applicationName)");
            return null;
        }
        return new AgentIds(agentId, applicationName);
    }

    private String newRandomAgentId() {
        UUID agentUUID = UUID.randomUUID();
        return AgentUuidUtils.encode(agentUUID);
    }

    private String getAgentId() {
        String source = null;
        for (AgentProperties agentProperty : agentPropertyList) {
            final String agentId = agentProperty.getAgentId();
            if (StringUtils.isEmpty(agentId)) {
                continue;
            }
            if (idValidator.validateAgentId(agentProperty.getType(), agentId)) {
                logger.info(agentProperty.getType() + " " + agentProperty.getAgentKey() + "=" + agentId);
                source = agentId;
            }
        }
        return source;
    }

    private String getApplicationName() {
        String source = null;
        for (AgentProperties agentProperty : agentPropertyList) {
            final String applicationName = agentProperty.getApplicationName();
            if (StringUtils.isEmpty(applicationName)) {
                continue;
            }
            if (idValidator.validateApplicatonName(agentProperty.getType(), applicationName)) {
                logger.info(agentProperty.getType() + " " + agentProperty.getApplicationName() + "=" + applicationName);
                source = applicationName;
            }
        }
        return source;
    }

}
