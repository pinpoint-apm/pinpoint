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
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.DataSourceSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Repository
public class HbaseDataSourceListDao implements AgentStatDaoV2<DataSourceListBo> {

    private final HbaseOperations2 hbaseTemplate;

    private final TableNameProvider tableNameProvider;

    private final AgentStatHbaseOperationFactory agentStatHbaseOperationFactory;

    private final DataSourceSerializer dataSourceSerializer;

    public HbaseDataSourceListDao(@Qualifier("asyncPutHbaseTemplate") HbaseOperations2 hbaseTemplate, TableNameProvider tableNameProvider,
                                  AgentStatHbaseOperationFactory agentStatHbaseOperationFactory, DataSourceSerializer dataSourceSerializer) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.agentStatHbaseOperationFactory = Objects.requireNonNull(agentStatHbaseOperationFactory, "agentStatHbaseOperationFactory");
        this.dataSourceSerializer = Objects.requireNonNull(dataSourceSerializer, "dataSourceSerializer");
    }

    @Override
    public void insert(String agentId, List<DataSourceListBo> dataSourceListBos) {
        Objects.requireNonNull(agentId, "agentId");
        // Assert agentId
        CollectorUtils.checkAgentId(agentId);

        if (CollectionUtils.isEmpty(dataSourceListBos)) {
            return;
        }

        List<DataSourceListBo> reorderedDataSourceListBos = reorderDataSourceListBos(dataSourceListBos);
        List<Put> activeTracePuts = this.agentStatHbaseOperationFactory.createPuts(agentId, AgentStatType.DATASOURCE, reorderedDataSourceListBos, dataSourceSerializer);
        if (!activeTracePuts.isEmpty()) {
            TableName agentStatTableName = tableNameProvider.getTableName(HbaseTable.AGENT_STAT_VER2);
            this.hbaseTemplate.asyncPut(agentStatTableName, activeTracePuts);
        }
    }

    private List<DataSourceListBo> reorderDataSourceListBos(List<DataSourceListBo> dataSourceListBos) {
        // reorder dataSourceBo using id and timeSlot
        MultiKeyMap dataSourceListBoMap = new MultiKeyMap();

        for (DataSourceListBo dataSourceListBo : dataSourceListBos) {
            for (DataSourceBo dataSourceBo : dataSourceListBo.getList()) {
                int id = dataSourceBo.getId();
                long timestamp = dataSourceBo.getTimestamp();
                long timeSlot = AgentStatUtils.getBaseTimestamp(timestamp);

                DataSourceListBo mappedDataSourceListBo = (DataSourceListBo) dataSourceListBoMap.get(id, timeSlot);
                if (mappedDataSourceListBo == null) {
                    mappedDataSourceListBo = new DataSourceListBo();
                    mappedDataSourceListBo.setAgentId(dataSourceBo.getAgentId());
                    mappedDataSourceListBo.setStartTimestamp(dataSourceBo.getStartTimestamp());
                    mappedDataSourceListBo.setTimestamp(dataSourceBo.getTimestamp());

                    dataSourceListBoMap.put(id, timeSlot, mappedDataSourceListBo);
                }

                // set fastest timestamp
                if (mappedDataSourceListBo.getTimestamp() > dataSourceBo.getTimestamp()) {
                    mappedDataSourceListBo.setTimestamp(dataSourceBo.getTimestamp());
                }

                mappedDataSourceListBo.add(dataSourceBo);
            }
        }

        Collection values = dataSourceListBoMap.values();
        return new ArrayList<DataSourceListBo>(values);
    }
}