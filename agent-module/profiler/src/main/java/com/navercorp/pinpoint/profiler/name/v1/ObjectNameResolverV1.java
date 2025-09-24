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

/**
 * @author Woonduk Kang(emeroad)
 */
public class ObjectNameResolverV1 implements ObjectNameResolver {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final List<AgentProperties> agentPropertyList;

    private final IdValidator idValidator;

    public ObjectNameResolverV1(List<AgentProperties> agentPropertyList) {
        this(new IdValidatorV1(), agentPropertyList);
    }

    public ObjectNameResolverV1(IdValidator idValidator, List<AgentProperties> agentPropertyList) {
        this.idValidator = Objects.requireNonNull(idValidator, "idValidator");
        this.agentPropertyList = Objects.requireNonNull(agentPropertyList, "agentPropertyList");
    }

    @Override
    public ObjectName resolve() {

        ObjectNameFilter filter = new ObjectNameFilter(agentPropertyList);

        // required
        ObjectNameProperty agentIdField = filter.resolve(AgentProperties::getAgentId, idValidator::validateAgentId);
        String agentId;
        if (!agentIdField.hasLengthValue()) {
            logger.info("Failed to resolve AgentId(-Dpinpoint.agentId)");
            agentId = base64Uid();
            logger.info("Auto generate AgentId='{}'", agentId);
        } else {
            agentId = agentIdField.getValue();
            resolveLog(agentIdField);
        }

        // required
        final ObjectNameProperty applicationName = filter.resolve(AgentProperties::getApplicationName, idValidator::validateApplicationName);
        if (!applicationName.hasLengthValue()) {
            logger.warn("Failed to resolve ApplicationName(-Dpinpoint.applicationName)");
            throw new ObjectNameValidationFailedException(AgentIdType.ApplicationName, "ApplicationName not provided");
        } else {
            resolveLog(applicationName);
        }

        // optional
        final ObjectNameProperty agentName = filter.resolve(AgentProperties::getAgentName, idValidator::validateAgentName);
        if (!agentName.hasLengthValue()) {
            logger.info("No AgentName(-Dpinpoint.agentName) provided, it's optional!");
        } else {
            resolveLog(agentName);
        }
        String agentNameStr = agentName.getValue();
        if (StringUtils.isEmpty(agentNameStr)) {
            agentNameStr = agentId;
        }
        return new ObjectNameV1(agentId, agentNameStr, applicationName.getValue());
    }

    private void resolveLog(ObjectNameProperty keyValue) {
        logger.info("{} {}={}", keyValue.getSourceType().getDesc(), keyValue.getKey(), keyValue.getValue());
    }

    private String base64Uid() {
        TimeBasedEpochGenerator gen = Generators.timeBasedEpochGenerator();

        UUID uuid = gen.generate();
        return Base64Utils.encode(uuid);
    }

}
