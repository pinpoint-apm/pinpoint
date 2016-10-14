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

package com.navercorp.pinpoint.web.service.stat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author HyunGil Jeong
 */
@Deprecated
@Service("legacyAgentStatChartServiceFactory")
public class LegacyAgentStatChartServiceFactory implements FactoryBean<LegacyAgentStatChartService> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("#{pinpointWebProps['web.experimental.stat.format.compatibility.version'] ?: 'v1'}")
    private String mode = "v1";

    @Autowired
    @Qualifier("legacyAgentStatChartV1Service")
    private LegacyAgentStatChartService v1;

    @Autowired
    @Qualifier("legacyAgentStatChartV2Service")
    private LegacyAgentStatChartService v2;

    @Autowired
    @Qualifier("legacyAgentStatChartCompatibilityService")
    private LegacyAgentStatChartService compatibility;

    @Override
    public LegacyAgentStatChartService getObject() throws Exception {
        logger.info("LegacyAgentStatService Compatibility {}", mode);
        if (mode.equalsIgnoreCase("v1")) {
            return v1;
        } else if (mode.equalsIgnoreCase("v2")) {
            return v2;
        } else if (mode.equalsIgnoreCase("compatibilityMode")) {
            return compatibility;
        }
        return v1;
    }

    @Override
    public Class<?> getObjectType() {
        return LegacyAgentStatChartService.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
