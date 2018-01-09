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
import com.navercorp.pinpoint.thrift.dto.flink.TFDataSource;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class TFDataSourceBoMapperTest {

    public static final long timestamp = 1496370596375L;

    @Test
    public void mapTest() {
        DataSourceBo dataSourceBo = new DataSourceBo();
        dataSourceBo.setId(1);
        dataSourceBo.setJdbcUrl("jdbc:mysql");
        dataSourceBo.setDatabaseName("pinpoint");
        dataSourceBo.setServiceTypeCode((short) 1000);
        dataSourceBo.setTimestamp(timestamp);
        dataSourceBo.setActiveConnectionSize(15);
        dataSourceBo.setMaxConnectionSize(30);

        TFDataSourceBoMapper mapper = new TFDataSourceBoMapper();
        TFDataSource tFdataSource = mapper.map(dataSourceBo);
        assertEquals(tFdataSource.getId(), 1);
        assertEquals(tFdataSource.getServiceTypeCode(), 1000);
        assertEquals(tFdataSource.getDatabaseName(), "pinpoint");
        assertEquals(tFdataSource.getUrl(), "jdbc:mysql");
        assertEquals(tFdataSource.getActiveConnectionSize(), 15);
        assertEquals(tFdataSource.getMaxConnectionSize(), 30);
    }

}