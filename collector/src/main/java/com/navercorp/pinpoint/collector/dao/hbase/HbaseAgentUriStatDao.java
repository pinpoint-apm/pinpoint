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

package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.AgentUriStatDao;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentUriStatSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Repository
public class HbaseAgentUriStatDao implements AgentUriStatDao {

    private final Logger logger = LogManager.getLogger(this.getClass());


    private final HbaseOperations2 hbaseTemplate;

    private final TableNameProvider tableNameProvider;

    private final AgentStatHbaseOperationFactory agentStatHbaseOperationFactory;

    private final AgentUriStatSerializer agentUriStatSerializer;

    public HbaseAgentUriStatDao(@Qualifier("asyncPutHbaseTemplate") HbaseOperations2 hbaseTemplate,
                                TableNameProvider tableNameProvider,
                                AgentStatHbaseOperationFactory agentStatHbaseOperationFactory,
                                AgentUriStatSerializer agentUriStatSerializer) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.agentStatHbaseOperationFactory = Objects.requireNonNull(agentStatHbaseOperationFactory, "agentStatHbaseOperationFactory");
        this.agentUriStatSerializer = Objects.requireNonNull(agentUriStatSerializer, "agentUriStatSerializer");
    }

    @Override
    public void insert(AgentUriStatBo agentUriStatBo) {
        String agentId = agentUriStatBo.getAgentId();

        Objects.requireNonNull(agentId, "agentId");
        // Assert agentId
        CollectorUtils.checkAgentId(agentId);
        if (CollectionUtils.isEmpty(agentUriStatBo.getEachUriStatBoList())) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("insert() agentUriStatBo:{}", agentUriStatBo);
        }

        List<Put> agentUriStatPuts = this.agentStatHbaseOperationFactory.createPuts(agentId, AgentStatType.URI, Collections.singletonList(agentUriStatBo), this.agentUriStatSerializer);
        if (!agentUriStatPuts.isEmpty()) {
            TableName agentStatTableName = tableNameProvider.getTableName(HbaseTable.AGENT_URI_STAT);
            this.hbaseTemplate.asyncPut(agentStatTableName, agentUriStatPuts);
        }
    }

}
