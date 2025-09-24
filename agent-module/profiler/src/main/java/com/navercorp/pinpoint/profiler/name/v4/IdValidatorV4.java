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

package com.navercorp.pinpoint.profiler.name.v4;

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
public class IdValidatorV4 implements IdValidator {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final int MAX_NAME_LENGTH = PinpointConstants.AGENT_NAME_MAX_LEN_V4;

    private final int maxSize;

    public IdValidatorV4(int maxSize) {
        this.maxSize = maxSize;
    }

    public IdValidatorV4() {
        this(MAX_NAME_LENGTH);
    }

    @Override
    public boolean validateAgentId(ObjectNameProperty agentId) {
        return false;
    }

    @Override
    public boolean validateAgentName(ObjectNameProperty agentName) {
        if (!ObjectNameProperty.hasLengthValue(agentName)) {
            return false;
        }
        return validateKey(agentName, maxSize);
    }

    @Override
    public boolean validateApplicationName(ObjectNameProperty applicationName) {
        if (!ObjectNameProperty.hasLengthValue(applicationName)) {
            return false;
        }
        return validateKey(applicationName, maxSize);
    }

    @Override
    public boolean validateServiceName(ObjectNameProperty serviceName) {
        if (!ObjectNameProperty.hasLengthValue(serviceName)) {
            return false;
        }
        return validateKey(serviceName, maxSize);
    }

    private boolean validateKey(ObjectNameProperty keyValue, int maxSize) {
        Objects.requireNonNull(keyValue, "keyValue");

        final String key = keyValue.getKey();
        String value = keyValue.getValue();
        String desc = keyValue.getSourceType().getDesc();
        logger.info("check {} = {}", desc, value);
        if (!IdValidateUtils.validateId(value, maxSize)) {
            logger.info("invalid Id. {} {} can only contain [a-zA-Z0-9], '.', '-', '_'. maxLength:{} value:{}", desc, key, maxSize, value);
            return false;
        }
        return true;
    }

}
