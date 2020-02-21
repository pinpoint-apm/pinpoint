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
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentIdResolver {
    public static final String APPLICATION_NAME = "applicationName";
    public static final String AGENT_ID = "agentId";

    public static final String SYSTEM_PROPERTY_PREFIX = "pinpoint.";
    public static final String APPLICATION_NAME_SYSTEM_PROPERTY = SYSTEM_PROPERTY_PREFIX + "applicationName";
    public static final String AGENT_ID_SYSTEM_PROPERTY = SYSTEM_PROPERTY_PREFIX + "agentId";

    private final BootLogger logger = BootLogger.getLogger(this.getClass().getName());

    private final List<AgentProperties> agentPropertyList;

    public AgentIdResolver(List<AgentProperties> agentPropertyList) {
        this.agentPropertyList = Assert.requireNonNull(agentPropertyList, "agentPropertyList");
    }

    public AgentIds resolve() {
        for (AgentProperties agentProperty : agentPropertyList) {
            final String agentId = agentProperty.getAgentId();
            boolean touch = false;
            if (agentId != null) {
                logger.info(agentProperty.getType() + " " + agentProperty.getAgentKey() +"=" + agentId);
                touch = true;
            }

            final String applicationName = agentProperty.getApplicationName();
            if (applicationName != null) {
                logger.info(agentProperty.getType() + " " + agentProperty.getApplicationNameKey() + "=" + applicationName);
                touch = true;
            }

            if (touch) {
                if (StringUtils.isEmpty(agentId)) {
                    String error = agentProperty.getType() + " agentId is missing";
                    logger.warn(error);
                    return null;
                }
                if (StringUtils.isEmpty(applicationName)) {
                    String error = agentProperty.getType() + " applicationName is missing";
                    logger.warn(error);
                    return null;
                }
                return new AgentIds(agentProperty.getType(), agentId, applicationName);
            }
        }
        
        return null;
    }


}
