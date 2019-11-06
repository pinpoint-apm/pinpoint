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

import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFDataSource;
import com.navercorp.pinpoint.thrift.dto.flink.TFDataSourceList;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author minwoo.jung
 */
public class TFDataSourceListBoMapperTest {

    public static final long startTimestamp = 1496370596375L;
    public static final long collectTime1st = startTimestamp + 5000;

    @Test
    public void mapTest() {
        DataSourceListBo dataSourceListBo = new DataSourceListBo();
        dataSourceListBo.setAgentId("test_agent1");
        dataSourceListBo.setStartTimestamp(startTimestamp);
        dataSourceListBo.setTimestamp(collectTime1st);

        DataSourceBo dataSourceBo1 = new DataSourceBo();
        dataSourceBo1.setAgentId("test_agent1");
        dataSourceBo1.setTimestamp(collectTime1st);
        dataSourceBo1.setServiceTypeCode((short) 1000);
        dataSourceBo1.setJdbcUrl("jdbc:mysql");
        dataSourceBo1.setActiveConnectionSize(15);
        dataSourceBo1.setMaxConnectionSize(30);
        dataSourceBo1.setId(1);
        dataSourceBo1.setDatabaseName("pinpoint1");
        DataSourceBo dataSourceBo2 = new DataSourceBo();
        dataSourceBo2.setAgentId("test_agent1");
        dataSourceBo2.setTimestamp(collectTime1st);
        dataSourceBo2.setServiceTypeCode((short) 2000);
        dataSourceBo2.setJdbcUrl("jdbc:mssql");
        dataSourceBo2.setActiveConnectionSize(25);
        dataSourceBo2.setMaxConnectionSize(40);
        dataSourceBo2.setId(2);
        dataSourceBo2.setDatabaseName("pinpoint2");
        dataSourceListBo.add(dataSourceBo1);
        dataSourceListBo.add(dataSourceBo2);

        TFDataSourceListBoMapper tFDataSourceListBoMapper = new TFDataSourceListBoMapper();
        TFDataSourceList tfDataSourceList = tFDataSourceListBoMapper.map(dataSourceListBo);

        List<TFDataSource> dataSourceList = tfDataSourceList.getDataSourceList();
        assertEquals(dataSourceList.size(), 2);

        TFDataSource tfDataSource1 = dataSourceList.get(0);
        assertEquals(tfDataSource1.getId(), 1);
        assertEquals(tfDataSource1.getUrl(), "jdbc:mysql");
        assertEquals(tfDataSource1.getDatabaseName(), "pinpoint1");
        assertEquals(tfDataSource1.getActiveConnectionSize(), 15);
        assertEquals(tfDataSource1.getMaxConnectionSize(), 30);
        assertEquals(tfDataSource1.getServiceTypeCode(), 1000);

        TFDataSource tfDataSource2 = dataSourceList.get(1);
        assertEquals(tfDataSource2.getId(), 2);
        assertEquals(tfDataSource2.getUrl(), "jdbc:mssql");
        assertEquals(tfDataSource2.getDatabaseName(), "pinpoint2");
        assertEquals(tfDataSource2.getActiveConnectionSize(), 25);
        assertEquals(tfDataSource2.getMaxConnectionSize(), 40);
        assertEquals(tfDataSource2.getServiceTypeCode(), 2000);
    }
}