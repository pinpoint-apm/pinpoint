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

package com.navercorp.pinpoint.profiler.name.v4;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import com.navercorp.pinpoint.common.profiler.name.Base64Utils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.name.AgentIdType;
import com.navercorp.pinpoint.profiler.name.AgentProperties;
import com.navercorp.pinpoint.profiler.name.IdValidator;
import com.navercorp.pinpoint.profiler.name.ObjectName;
import com.navercorp.pinpoint.profiler.name.ObjectNameFilter;
import com.navercorp.pinpoint.profiler.name.ObjectNameProperty;
import com.navercorp.pinpoint.profiler.name.ObjectNameResolver;
import com.navercorp.pinpoint.profiler.name.ObjectNameValidationFailedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ObjectNameResolverV4 implements ObjectNameResolver {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final List<AgentProperties> agentPropertyList;

    private final IdValidator idValidator = new IdValidatorV4();

    public ObjectNameResolverV4(List<AgentProperties> agentPropertyList) {
        this.agentPropertyList = Objects.requireNonNull(agentPropertyList, "agentPropertyList");
    }

    @Override
    public ObjectName resolve() {
        TimeBasedEpochGenerator timeBasedEpochGenerator = Generators.timeBasedEpochGenerator();
        final UUID agentId = timeBasedEpochGenerator.generate();

        ObjectNameFilter filter = new ObjectNameFilter(agentPropertyList);

        final ObjectNameProperty agentName = filter.resolve(AgentProperties::getAgentName, idValidator::validateAgentName);
        if (!agentName.hasLengthValue()) {
            logger.info("No AgentName(-Dpinpoint.agentName) provided, it's optional!");
        } else {
            resolveLog(agentName);
        }

        final ObjectNameProperty applicationName = filter.resolve(AgentProperties::getApplicationName, idValidator::validateApplicationName);
        if (!applicationName.hasLengthValue()) {
            logger.warn("Failed to resolve ApplicationName(-Dpinpoint.applicationName)");
            throw new ObjectNameValidationFailedException(AgentIdType.ApplicationName, "ApplicationName not provided");
        }  else {
            resolveLog(applicationName);
        }

        final ObjectNameProperty serviceName = filter.resolve(AgentProperties::getServiceName, idValidator::validateServiceName);
        if (!serviceName.hasLengthValue()) {
            logger.warn("Failed to resolve ServiceName(-Dpinpoint.serviceName)");
            throw new ObjectNameValidationFailedException(AgentIdType.ServiceName, "ServiceName not provided");
        } else {
            resolveLog(serviceName);
        }

        final ObjectNameProperty apiKey = getApiKey(filter);
        if (!apiKey.hasLengthValue()) {
            logger.warn("Failed to resolve ApiKey(-Dpinpoint.apikey)");
            throw new ObjectNameValidationFailedException(AgentIdType.APIKEY, "ApiKey not provided");
        } else {
            resolveLog(apiKey);
        }

        String agentNameStr = agentName.getValue();
        if (agentNameStr == null) {
            agentNameStr = Base64Utils.encode(agentId);
        }
        return new ObjectNameV4(agentId, agentNameStr, applicationName.getValue(), serviceName.getValue(), apiKey.getValue());
    }

    private ObjectNameProperty getApiKey(ObjectNameFilter filter) {
        return filter.resolve(new Function<AgentProperties, ObjectNameProperty>() {
            @Override
            public ObjectNameProperty apply(AgentProperties properties) {
                return properties.getProperty("pinpoint.apikey", properties.getType(), AgentIdType.APIKEY);
            }
        }, this::validateApiKey);
    }

    private boolean validateApiKey(ObjectNameProperty apiKey) {
        Objects.requireNonNull(apiKey, "apiKey");

        final String key = apiKey.getKey();
        String value = apiKey.getValue();
        String desc = apiKey.getSourceType().getDesc();
        logger.info("check {} = {}", desc, value);
        if (StringUtils.isEmpty(value)) {
            logger.warn("apikey is empty");
            return false;
        }
        return true;
    }


    private void resolveLog(ObjectNameProperty keyValue) {
        logger.info("{} {}={}", keyValue.getSourceType().getDesc(), keyValue.getKey(), keyValue.getValue());
    }

}
