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

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.AgentUuidUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.common.util.UuidUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentIdResolver {
    public static final String APPLICATION_NAME = "applicationName";
    public static final String AGENT_ID = "agentId";
    public static final String AGENT_NAME = "agentName";

    public static final String SYSTEM_PROPERTY_PREFIX = "pinpoint.";
    public static final String APPLICATION_NAME_SYSTEM_PROPERTY = SYSTEM_PROPERTY_PREFIX + "applicationName";
    public static final String AGENT_ID_SYSTEM_PROPERTY = SYSTEM_PROPERTY_PREFIX + "agentId";
    public static final String AGENT_NAME_SYSTEM_PROPERTY = SYSTEM_PROPERTY_PREFIX + "agentName";

    public static final String ENV_PROPERTY_PREFIX = "PINPOINT_";
    public static final String APPLICATION_NAME_ENV_PROPERTY = ENV_PROPERTY_PREFIX + "APPLICATION_NAME";
    public static final String AGENT_ID_ENV_PROPERTY = ENV_PROPERTY_PREFIX + "AGENT_ID";
    public static final String AGENT_NAME_ENV_PROPERTY = ENV_PROPERTY_PREFIX + "AGENT_NAME";

    private final BootLogger logger = BootLogger.getLogger(this.getClass());

    private final List<AgentProperties> agentPropertyList;

    private final IdValidator idValidator = new IdValidator();
    private final IdValidator applicationNameValidator = new IdValidator(PinpointConstants.APPLICATION_NAME_MAX_LEN);

    public AgentIdResolver(List<AgentProperties> agentPropertyList) {
        this.agentPropertyList = Objects.requireNonNull(agentPropertyList, "agentPropertyList");
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

        final String agentName = getAgentName();
        if (StringUtils.isEmpty(agentName)) {
            logger.info("No AgentName(-Dpinpoint.agentName) provided, it's optional!");
        }

        return new AgentIds(agentId, agentName, applicationName);
    }

    private String newRandomAgentId() {
        UUID agentUUID = UuidUtils.createV4();
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
                logger.info(agentProperty.getType() + " " + agentProperty.getAgentIdKey() + "=" + agentId);
                source = agentId;
            }
        }
        return source;
    }

    private String getAgentName() {
        String source = "";
        for (AgentProperties agentProperty : agentPropertyList) {
            final String agentName = agentProperty.getAgentName();
            if (idValidator.validateAgentName(agentProperty.getType(), agentName)) {
                logger.info(agentProperty.getType() + " " + agentProperty.getAgentNameKey() + "=" + agentName);
                source = agentName;
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
            if (applicationNameValidator.validateApplicationName(agentProperty.getType(), applicationName)) {
                logger.info(agentProperty.getType() + " " + agentProperty.getApplicationNameKey() + "=" + applicationName);
                source = applicationName;
            }
        }
        return source;
    }

}
