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
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.DataSourceSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Taejin Koo
 */
@Repository
public class HbaseDataSourceListDao implements AgentStatDaoV2<DataSourceListBo> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    private AgentStatHbaseOperationFactory agentStatHbaseOperationFactory;

    @Autowired
    private DataSourceSerializer dataSourceSerializer;

    @Override
    public void insert(String agentId, List<DataSourceListBo> dataSourceListBos) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (CollectionUtils.isEmpty(dataSourceListBos)) {
            return;
        }

        List<DataSourceListBo> reorderedDataSourceListBos = reorderDataSourceListBos(dataSourceListBos);
        List<Put> activeTracePuts = this.agentStatHbaseOperationFactory.createPuts(agentId, AgentStatType.DATASOURCE, reorderedDataSourceListBos, dataSourceSerializer);
        if (!activeTracePuts.isEmpty()) {
            List<Put> rejectedPuts = this.hbaseTemplate.asyncPut(HBaseTables.AGENT_STAT_VER2, activeTracePuts);
            if (CollectionUtils.hasLength(rejectedPuts)) {
                this.hbaseTemplate.put(HBaseTables.AGENT_STAT_VER2, rejectedPuts);
            }
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
