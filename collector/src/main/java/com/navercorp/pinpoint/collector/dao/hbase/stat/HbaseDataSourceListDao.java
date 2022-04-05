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

import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Taejin Koo
 */
@Repository
public class HbaseDataSourceListDao extends AbstractHBaseDao<DataSourceListBo> {

    public HbaseDataSourceListDao(HbaseOperations2 hbaseTemplate,
                               TableNameProvider tableNameProvider,
                               AgentStatHbaseOperationFactory operationFactory,
                               AgentStatSerializer<DataSourceListBo> serializer) {
        super(AgentStatType.DATASOURCE, HbaseTable.AGENT_STAT_VER2, AgentStatBo::getDataSourceListBos,
                hbaseTemplate, tableNameProvider, operationFactory, serializer);
        this.preprocessor = this::reorderDataSourceListBos;
    }

    private List<DataSourceListBo> reorderDataSourceListBos(List<DataSourceListBo> dataSourceListBos) {
        // reorder dataSourceBo using id and timeSlot
        MultiKeyMap<Long, DataSourceListBo> dataSourceListBoMap = new MultiKeyMap<>();

        for (DataSourceListBo dataSourceListBo : dataSourceListBos) {
            for (DataSourceBo dataSourceBo : dataSourceListBo.getList()) {
                int id = dataSourceBo.getId();
                long timestamp = dataSourceBo.getTimestamp();
                long timeSlot = AgentStatUtils.getBaseTimestamp(timestamp);

                DataSourceListBo mappedDataSourceListBo = dataSourceListBoMap.get(id, timeSlot);
                if (mappedDataSourceListBo == null) {
                    mappedDataSourceListBo = new DataSourceListBo();
                    mappedDataSourceListBo.setAgentId(dataSourceBo.getAgentId());
                    mappedDataSourceListBo.setStartTimestamp(dataSourceBo.getStartTimestamp());
                    mappedDataSourceListBo.setTimestamp(dataSourceBo.getTimestamp());

                    dataSourceListBoMap.put((long) id, timeSlot, mappedDataSourceListBo);
                }

                // set fastest timestamp
                if (mappedDataSourceListBo.getTimestamp() > dataSourceBo.getTimestamp()) {
                    mappedDataSourceListBo.setTimestamp(dataSourceBo.getTimestamp());
                }

                mappedDataSourceListBo.add(dataSourceBo);
            }
        }

        Collection<DataSourceListBo> values = dataSourceListBoMap.values();
        return new ArrayList<>(values);
    }

}