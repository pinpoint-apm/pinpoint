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

package com.navercorp.pinpoint.collector.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TDataSource;
import com.navercorp.pinpoint.thrift.dto.TDataSourceList;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Taejin Koo
 */
@Component
public class ThriftDataSourceBoMapper implements ThriftStatMapper<DataSourceBo, TDataSource> {

    @Override
    public DataSourceBo map(TDataSource dataSource) {
        DataSourceBo dataSourceBo = new DataSourceBo();
        dataSourceBo.setId(dataSource.getId());
        dataSourceBo.setServiceTypeCode(dataSource.getServiceTypeCode());
        dataSourceBo.setDatabaseName(dataSource.getDatabaseName());
        dataSourceBo.setJdbcUrl(dataSource.getUrl());
        dataSourceBo.setActiveConnectionSize(dataSource.getActiveConnectionSize());
        dataSourceBo.setMaxConnectionSize(dataSource.getMaxConnectionSize());
        return dataSourceBo;
    }

    @Override
    public void map(AgentStatBo.Builder.StatBuilder agentStatBuilder, TAgentStat tAgentStat) {
        // datasource
        if (tAgentStat.isSetDataSourceList()) {
            DataSourceListBo dataSourceListBo = new DataSourceListBo();

            TDataSourceList dataSourceList = tAgentStat.getDataSourceList();
            List<TDataSource> innerDataSourceList = dataSourceList.getDataSourceList();
            if (innerDataSourceList != null) {
                for (TDataSource dataSource : innerDataSourceList) {
                    DataSourceBo dataSourceBo = map(dataSource);

                    dataSourceListBo.add(dataSourceBo);
                }
            }
            agentStatBuilder.addDataSourceList(dataSourceListBo);
        }
    }
}