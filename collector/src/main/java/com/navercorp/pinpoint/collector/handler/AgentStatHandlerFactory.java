/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.collector.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * usage for development env
 * @author HyunGil Jeong
 */
@Repository
public class AgentStatHandlerFactory implements FactoryBean<Handler> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("agentStatHandler")
    private AgentStatHandler v1;

    @Autowired
    @Qualifier("agentStatHandlerV2")
    private AgentStatHandlerV2 v2;

    @Value("#{pinpoint_collector_properties['collector.experimental.stat.format.compatibility.version'] ?: 'v0'}")
    private String mode = "v1";

    @Override
    public Handler getObject() throws Exception {
        logger.info("AgentStatHandler Mode {}", mode);
        if (mode.equalsIgnoreCase("v1")) {
            return v1;
        } else if (mode.equalsIgnoreCase("v2")) {
            return v2;
        } else if (mode.equalsIgnoreCase("dualWrite")) {
            return new DualAgentStatHandler(v1, v2);
        }
        return v1;
    }

    @Override
    public Class<?> getObjectType() {
        return Handler.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
