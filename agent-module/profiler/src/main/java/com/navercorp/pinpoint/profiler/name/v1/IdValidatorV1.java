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

package com.navercorp.pinpoint.profiler.name.v1;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.profiler.name.IdValidator;
import com.navercorp.pinpoint.profiler.name.ObjectNameProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class IdValidatorV1 implements IdValidator {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final int agentIdMaxLen;
    private final int agentNameMaxLen;
    private final int applicationNameMaxLen;

    public static IdValidator v3() {
        return new IdValidatorV1(PinpointConstants.AGENT_ID_MAX_LEN, PinpointConstants.AGENT_NAME_MAX_LEN, PinpointConstants.APPLICATION_NAME_MAX_LEN_V3);
    }

    public IdValidatorV1(int agentIdMaxLen, int agentNameMaxLen, int applicationNameMaxLen) {
        this.agentIdMaxLen = agentIdMaxLen;
        this.agentNameMaxLen = agentNameMaxLen;
        this.applicationNameMaxLen = applicationNameMaxLen;
    }

    public IdValidatorV1() {
        this(PinpointConstants.AGENT_ID_MAX_LEN, PinpointConstants.AGENT_NAME_MAX_LEN, PinpointConstants.AGENT_ID_MAX_LEN);
    }

    @Override
    public boolean validateAgentId(ObjectNameProperty agentId) {
        if (!ObjectNameProperty.hasLengthValue(agentId)) {
            return false;
        }
        return validateKey(agentId, agentIdMaxLen);
    }

    @Override
    public boolean validateAgentName(ObjectNameProperty agentName) {
        if (!ObjectNameProperty.hasLengthValue(agentName)) {
            return false;
        }
        return validateKey(agentName, agentNameMaxLen);
    }

    @Override
    public boolean validateApplicationName(ObjectNameProperty applicationName) {
        if (!ObjectNameProperty.hasLengthValue(applicationName)) {
            return false;
        }
        return validateKey(applicationName, applicationNameMaxLen);
    }

    @Override
    public boolean validateServiceName(ObjectNameProperty serviceName) {
        return false;
    }

    private boolean validateKey(ObjectNameProperty property, int maxSize) {
        Objects.requireNonNull(property, "property");

        final String key = property.getKey();
        String value = property.getValue();
        String desc = property.getSourceType().getDesc();
        logger.info("check {} = {}", desc, value);
        if (!IdValidateUtils.validateId(value, maxSize)) {
            logger.info("invalid Id. {} {} can only contain [a-zA-Z0-9], '.', '-', '_'. maxLength:{} value:{}", desc, key, maxSize, value);
            return false;
        }
        return true;
    }

}
