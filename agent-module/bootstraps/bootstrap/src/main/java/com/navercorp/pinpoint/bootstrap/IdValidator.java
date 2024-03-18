/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.bootstrap;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class IdValidator {

    private final BootLogger logger = BootLogger.getLogger(this.getClass());

    private static final int MAX_ID_LENGTH = PinpointConstants.AGENT_ID_MAX_LEN;

    private static final int MAX_NAME_LENGTH = PinpointConstants.AGENT_NAME_MAX_LEN;

    private final int maxSize;

    public IdValidator(int maxSize) {
        this.maxSize = maxSize;
    }

    public IdValidator() {
        this(MAX_ID_LENGTH);
    }

    public boolean validateAgentId(AgentIdSourceType type, String agentId) {
        Objects.requireNonNull(agentId, "agentId");

        return validate0(type + " agentId", agentId);
    }

    public boolean validateApplicationName(AgentIdSourceType type, String applicationName) {
        Objects.requireNonNull(applicationName, "applicationName");
        return validate0(type + " applicationName", applicationName);
    }

    public boolean validateServiceName(AgentIdSourceType type, String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName");
        return validate0(type + " serviceName", serviceName);
    }

    public boolean validateAgentName(AgentIdSourceType type, String agentName) {
        if (StringUtils.isEmpty(agentName)) {
            return false;
        }
        return validate0(type + " agentName", agentName, MAX_NAME_LENGTH);
    }

    private boolean validate0(String keyName, String keyValue) {
        return validate0(keyName, keyValue, maxSize);
    }

    private boolean validate0(String keyName, String keyValue, int maxSize) {
        logger.info("check " + keyName + ":" + keyValue);
        if (!IdValidateUtils.validateId(keyValue, maxSize)) {
            logger.info("invalid Id. " + keyName + " can only contain [a-zA-Z0-9], '.', '-', '_'. maxLength:" + maxSize + " value:" + keyValue);
            return false;
        }
        return true;
    }

}
