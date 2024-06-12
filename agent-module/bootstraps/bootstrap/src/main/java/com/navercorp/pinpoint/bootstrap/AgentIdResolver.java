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
import com.navercorp.pinpoint.common.id.ServiceId;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentIdResolver {
    public static final String APPLICATION_NAME = "applicationName";
    public static final String SERVICE_NAME = "serviceName";
    public static final String AGENT_NAME = "agentName";

    public static final String SYSTEM_PROPERTY_PREFIX = "pinpoint.";
    public static final String APPLICATION_NAME_SYSTEM_PROPERTY = SYSTEM_PROPERTY_PREFIX + "applicationName";
    public static final String SERVICE_NAME_SYSTEM_PROPERTY = SYSTEM_PROPERTY_PREFIX + "serviceName";
    public static final String AGENT_NAME_SYSTEM_PROPERTY = SYSTEM_PROPERTY_PREFIX + "agentName";

    public static final String ENV_PROPERTY_PREFIX = "PINPOINT_";
    public static final String APPLICATION_NAME_ENV_PROPERTY = ENV_PROPERTY_PREFIX + "APPLICATION_NAME";
    public static final String SERVICE_NAME_ENV_PROPERTY = ENV_PROPERTY_PREFIX + "SERVICE_NAME";
    public static final String AGENT_NAME_ENV_PROPERTY = ENV_PROPERTY_PREFIX + "AGENT_NAME";

    private final BootLogger logger = BootLogger.getLogger(this.getClass());

    private final List<AgentProperties> agentPropertyList;

    private final IdValidator idValidator = new IdValidator();
    private final IdValidator applicationNameValidator = new IdValidator(PinpointConstants.APPLICATION_NAME_MAX_LEN);
    private final IdValidator serviceNameValidator = new IdValidator(PinpointConstants.SERVICE_NAME_MAX_LEN);

    public AgentIdResolver(List<AgentProperties> agentPropertyList) {
        this.agentPropertyList = Objects.requireNonNull(agentPropertyList, "agentPropertyList");
    }

    public AgentIds resolve() {
        final String applicationName = getApplicationName();
        if (StringUtils.isEmpty(applicationName)) {
            logger.warn("Failed to resolve ApplicationName(-Dpinpoint.applicationName)");
            return null;
        }

        String serviceName = getServiceName();
        if (StringUtils.isEmpty(serviceName)) {
            logger.info("Failed to resolve ServiceName(-Dpinpoint.serviceName)");
            serviceName = ServiceId.DEFAULT_SERVICE_NAME;
            logger.info("Using default serviceName='" + serviceName + "'");
        }

        final String agentName = getAgentName();
        if (StringUtils.isEmpty(agentName)) {
            logger.info("No AgentName(-Dpinpoint.agentName) provided, it's optional!");
        }

        return new AgentIds(agentName, applicationName, serviceName);
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

    private String getServiceName() {
        String source = null;
        for (AgentProperties agentProperty : agentPropertyList) {
            final String serviceName = agentProperty.getServiceName();
            if (StringUtils.isEmpty(serviceName)) {
                continue;
            }
            if (serviceNameValidator.validateServiceName(agentProperty.getType(), serviceName)) {
                logger.info(agentProperty.getType() + " " + agentProperty.getServiceNameKey() + "=" + serviceName);
                source = serviceName;
            }
        }
        return source;
    }

}
