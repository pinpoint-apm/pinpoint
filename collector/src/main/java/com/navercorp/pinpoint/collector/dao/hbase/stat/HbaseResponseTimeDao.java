/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase.stat;

import com.navercorp.pinpoint.collector.dao.AgentStatDaoV2;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ResponseTimeSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Repository
public class HbaseResponseTimeDao implements AgentStatDaoV2<ResponseTimeBo> {

    private final HbaseOperations2 hbaseTemplate;

    private final TableNameProvider tableNameProvider;

    private final AgentStatHbaseOperationFactory agentStatHbaseOperationFactory;

    private final ResponseTimeSerializer responseTimeSerializer;

    public HbaseResponseTimeDao(@Qualifier("asyncPutHbaseTemplate") HbaseOperations2 hbaseTemplate,
                                TableNameProvider tableNameProvider,
                                AgentStatHbaseOperationFactory agentStatHbaseOperationFactory,
                                ResponseTimeSerializer responseTimeSerializer) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.agentStatHbaseOperationFactory = Objects.requireNonNull(agentStatHbaseOperationFactory, "agentStatHbaseOperationFactory");
        this.responseTimeSerializer = Objects.requireNonNull(responseTimeSerializer, "responseTimeSerializer");
    }

    @Override
    public void insert(String agentId, List<ResponseTimeBo> responseTimeBos) {
        Objects.requireNonNull(agentId, "agentId");
        // Assert agentId
        CollectorUtils.checkAgentId(agentId);

        if (CollectionUtils.isEmpty(responseTimeBos)) {
            return;
        }
        List<Put> responseTimePuts = this.agentStatHbaseOperationFactory.createPuts(agentId, AgentStatType.RESPONSE_TIME, responseTimeBos, this.responseTimeSerializer);
        if (!responseTimePuts.isEmpty()) {
            TableName agentStatTableName = tableNameProvider.getTableName(HbaseTable.AGENT_STAT_VER2);
            this.hbaseTemplate.asyncPut(agentStatTableName, responseTimePuts);
        }
    }

    @Override
    public void dispatch(AgentStatBo agentStatBo) {
        insert(agentStatBo.getAgentId(), agentStatBo.getResponseTimeBos());
    }

}
