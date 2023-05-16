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

package com.navercorp.pinpoint.flink.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinIntFieldBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFDataSource;
import com.navercorp.pinpoint.thrift.dto.flink.TFDataSourceList;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author minwoo.jung
 */
public class JoinDataSourceListBoMapperTest {

    @Test
    public void mapTest() {
        JoinDataSourceListBoMapper mapper = new JoinDataSourceListBoMapper();
        TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId("testAgent");
        tFAgentStat.setTimestamp(1491274138454L);
        TFDataSourceList tFDataSourceList = new TFDataSourceList();


        TFDataSource tFDataSource1 = new TFDataSource();
        tFDataSource1.setUrl("jdbc:mysql");
        tFDataSource1.setMaxConnectionSize(30);
        tFDataSource1.setActiveConnectionSize(13);
        tFDataSource1.setDatabaseName("pinpoint");
        tFDataSource1.setServiceTypeCode((short) 1000);
        TFDataSource tFDataSource2 = new TFDataSource();
        tFDataSource2.setUrl("jdbc:mssql");
        tFDataSource2.setMaxConnectionSize(31);
        tFDataSource2.setActiveConnectionSize(23);
        tFDataSource2.setDatabaseName("pinpoint");
        tFDataSource2.setServiceTypeCode((short) 2000);

        List<TFDataSource> dataSourceList = List.of(tFDataSource1, tFDataSource2);

        tFDataSourceList.setDataSourceList(dataSourceList);
        tFAgentStat.setDataSourceList(tFDataSourceList);
        JoinDataSourceListBo joinDataSourceListBo = mapper.map(tFAgentStat);

        assertEquals(joinDataSourceListBo.getId(), "testAgent");
        assertEquals(joinDataSourceListBo.getTimestamp(), 1491274138454L);
        assertThat(joinDataSourceListBo.getJoinDataSourceBoList()).hasSize(2);

        List<JoinDataSourceBo> joinDataSourceBoList = joinDataSourceListBo.getJoinDataSourceBoList();
        JoinDataSourceBo joinDataSourceBo1 = joinDataSourceBoList.get(0);
        assertEquals(joinDataSourceBo1.getServiceTypeCode(), 1000);
        assertEquals(joinDataSourceBo1.getUrl(), "jdbc:mysql");
        assertEquals(joinDataSourceBo1.getActiveConnectionSizeJoinValue(), new JoinIntFieldBo(13, 13, "testAgent", 13, "testAgent"));
        JoinDataSourceBo joinDataSourceBo2 = joinDataSourceBoList.get(1);
        assertEquals(joinDataSourceBo2.getServiceTypeCode(), 2000);
        assertEquals(joinDataSourceBo2.getUrl(), "jdbc:mssql");
        assertEquals(joinDataSourceBo2.getActiveConnectionSizeJoinValue(), new JoinIntFieldBo(23, 23, "testAgent", 23, "testAgent"));
    }


    @Test
    public void map2Test() {
        JoinDataSourceListBoMapper mapper = new JoinDataSourceListBoMapper();
        TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId("testAgent");
        tFAgentStat.setTimestamp(1491274138454L);
        JoinDataSourceListBo joinDataSourceListBo = mapper.map(tFAgentStat);
        assertEquals(joinDataSourceListBo, JoinDataSourceListBo.EMPTY_JOIN_DATA_SOURCE_LIST_BO);

        TFDataSourceList tFDataSourceList = new TFDataSourceList();
        tFAgentStat.setDataSourceList(tFDataSourceList);
        joinDataSourceListBo = mapper.map(tFAgentStat);
        assertEquals(joinDataSourceListBo, JoinDataSourceListBo.EMPTY_JOIN_DATA_SOURCE_LIST_BO);

        tFDataSourceList.setDataSourceList(List.of());
        joinDataSourceListBo = mapper.map(tFAgentStat);
        assertEquals(joinDataSourceListBo, JoinDataSourceListBo.EMPTY_JOIN_DATA_SOURCE_LIST_BO);
    }
}