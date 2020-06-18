/*
 * Copyright 2020 NAVER Corp.
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
import com.navercorp.pinpoint.common.server.bo.serializer.stat.LoadedClassSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class HBaseLoadedClassDao implements AgentStatDaoV2<LoadedClassBo> {
    private final HbaseOperations2 hbaseTemplate;

    private final TableNameProvider tableNameProvider;

    private final AgentStatHbaseOperationFactory agentStatHbaseOperationFactory;

    private final LoadedClassSerializer loadedClassSerializer;

    public HBaseLoadedClassDao(@Qualifier("asyncPutHbaseTemplate") HbaseOperations2 hbaseTemplate,
                               TableNameProvider tableNameProvider,
                               AgentStatHbaseOperationFactory agentStatHbaseOperationFactory,
                               LoadedClassSerializer loadedClassSerializer) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.agentStatHbaseOperationFactory = Objects.requireNonNull(agentStatHbaseOperationFactory, "agentStatHbaseOperationFactory");
        this.loadedClassSerializer = Objects.requireNonNull(loadedClassSerializer, "loadedClassSerializer");
    }

    @Override
    public void insert(String agentId, List<LoadedClassBo> agentStatDataPoints) {
        Objects.requireNonNull(agentId, "agentId");
        // Assert agentId
        CollectorUtils.checkAgentId(agentId);

        if(CollectionUtils.isEmpty(agentStatDataPoints)) { return; }
        List<Put> loadedClassPuts = this.agentStatHbaseOperationFactory.createPuts(agentId, AgentStatType.LOADED_CLASS, agentStatDataPoints, this.loadedClassSerializer);
        if (!loadedClassPuts.isEmpty()) {
            TableName agentStatTableName = tableNameProvider.getTableName(HbaseTable.AGENT_STAT_VER2);
            this.hbaseTemplate.asyncPut(agentStatTableName, loadedClassPuts);
        }
    }
}
