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

import com.navercorp.pinpoint.common.hbase.HBaseAdminTemplate;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import org.apache.hadoop.hbase.TableName;
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

    @Autowired
    private HBaseAdminTemplate adminTemplate;

    @Value("#{pinpoint_collector_properties['collector.stat.format.compatibility.version'] ?: 'v2'}")
    private String mode = "v2";

    @Override
    public Handler getObject() throws Exception {
        logger.info("AgentStatHandler Mode {}", mode);

        final TableName v1TableName = HBaseTables.AGENT_STAT;
        final TableName v2TableName = HBaseTables.AGENT_STAT_VER2;

        if (mode.equalsIgnoreCase("v1")) {
            if (this.adminTemplate.tableExists(v1TableName)) {
                return v1;
            } else {
                logger.error("AgentStatHandler configured for v1, but {} table does not exist", v1TableName);
                throw new IllegalStateException(v1TableName + " table does not exist");
            }
        } else if (mode.equalsIgnoreCase("v2")) {
            if (this.adminTemplate.tableExists(v2TableName)) {
                return v2;
            } else {
                logger.error("AgentStatHandler configured for v2, but {} table does not exist", v2TableName);
                throw new IllegalStateException(v2TableName + " table does not exist");
            }
        } else if (mode.equalsIgnoreCase("dualWrite")) {
            boolean v1TableExists = this.adminTemplate.tableExists(v1TableName);
            boolean v2TableExists = this.adminTemplate.tableExists(v2TableName);
            if (v1TableExists && v2TableExists) {
                return new DualAgentStatHandler(v1, v2);
            } else {
                logger.error("AgentStatHandler configured for dualWrite, but {} and {} tables do not exist", v1TableName, v2TableName);
                throw new IllegalStateException(v1TableName + ", " + v2TableName + " tables do not exist");
            }
        } else {
            throw new IllegalStateException("Unknown AgentStatHandler configuration : " + mode);
        }
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
