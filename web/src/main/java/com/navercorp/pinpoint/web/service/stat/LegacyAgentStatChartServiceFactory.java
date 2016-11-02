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

import com.navercorp.pinpoint.common.hbase.HBaseAdminTemplate;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import org.apache.hadoop.hbase.TableName;
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

    @Value("#{pinpointWebProps['web.stat.format.compatibility.version'] ?: 'v2'}")
    private String mode = "v2";

    @Autowired
    @Qualifier("legacyAgentStatChartV1Service")
    private LegacyAgentStatChartService v1;

    @Autowired
    @Qualifier("legacyAgentStatChartV2Service")
    private LegacyAgentStatChartService v2;

    @Autowired
    @Qualifier("legacyAgentStatChartCompatibilityService")
    private LegacyAgentStatChartService compatibility;

    @Autowired
    private HBaseAdminTemplate adminTemplate;

    @Override
    public LegacyAgentStatChartService getObject() throws Exception {
        logger.info("LegacyAgentStatService Compatibility {}", mode);

        final TableName v1TableName = HBaseTables.AGENT_STAT;
        final TableName v2TableName = HBaseTables.AGENT_STAT_VER2;

        if (mode.equalsIgnoreCase("v1")) {
            if (this.adminTemplate.tableExists(v1TableName)) {
                return v1;
            } else {
                logger.error("LegacyAgentStatService configured for v1, but {} table does not exist", v1TableName);
                throw new IllegalStateException(v1TableName + " table does not exist");
            }
        } else if (mode.equalsIgnoreCase("v2")) {
            if (this.adminTemplate.tableExists(v2TableName)) {
                return v2;
            } else {
                logger.error("LegacyAgentStatService configured for v2, but {} table does not exist", v2TableName);
                throw new IllegalStateException(v2TableName + " table does not exist");
            }
        } else if (mode.equalsIgnoreCase("compatibilityMode")) {
            boolean v1TableExists = this.adminTemplate.tableExists(v1TableName);
            boolean v2TableExists = this.adminTemplate.tableExists(v2TableName);
            if (v1TableExists && v2TableExists) {
                return compatibility;
            } else {
                logger.error("LegacyAgentStatService configured for compatibilityMode, but {} and {} tables do not exist", v1TableName, v2TableName);
                throw new IllegalStateException(v1TableName + ", " + v2TableName + " tables do not exist");
            }
        } else {
            throw new IllegalStateException("Unknown LegacyAgentStatService configuration : " + mode);
        }
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
