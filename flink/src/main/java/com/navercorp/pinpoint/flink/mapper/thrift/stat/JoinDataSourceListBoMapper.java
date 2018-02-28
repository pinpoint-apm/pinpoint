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
import com.navercorp.pinpoint.flink.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFDataSource;
import com.navercorp.pinpoint.thrift.dto.flink.TFDataSourceList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class JoinDataSourceListBoMapper implements ThriftBoMapper<JoinDataSourceListBo, TFAgentStat> {
    @Override
    public JoinDataSourceListBo map(TFAgentStat tFAgentStat) {
        if (!tFAgentStat.isSetDataSourceList()) {
            return JoinDataSourceListBo.EMPTY_JOIN_DATA_SOURCE_LIST_BO;
        }

        TFDataSourceList dataSourceList = tFAgentStat.getDataSourceList();
        if (!dataSourceList.isSetDataSourceList()) {
            return JoinDataSourceListBo.EMPTY_JOIN_DATA_SOURCE_LIST_BO;
        }

        List<TFDataSource> tFDataSourceList = dataSourceList.getDataSourceList();
        if (tFDataSourceList.isEmpty()) {
            return JoinDataSourceListBo.EMPTY_JOIN_DATA_SOURCE_LIST_BO;
        }


        final String agentId = tFAgentStat.getAgentId();
        JoinDataSourceListBo joinDataSourceListBo = new JoinDataSourceListBo();
        joinDataSourceListBo.setId(agentId);
        joinDataSourceListBo.setTimestamp(tFAgentStat.getTimestamp());

        List<JoinDataSourceBo> joinDataSourceBoList = new ArrayList<>();
        for (TFDataSource tFDataSource : tFDataSourceList) {
            JoinDataSourceBo joinDataSourceBo = new JoinDataSourceBo();
            joinDataSourceBo.setServiceTypeCode(tFDataSource.getServiceTypeCode());
            joinDataSourceBo.setUrl(tFDataSource.getUrl());
            joinDataSourceBo.setAvgActiveConnectionSize(tFDataSource.getActiveConnectionSize());
            joinDataSourceBo.setMinActiveConnectionSize(tFDataSource.getActiveConnectionSize());
            joinDataSourceBo.setMinActiveConnectionAgentId(tFAgentStat.getAgentId());
            joinDataSourceBo.setMaxActiveConnectionSize(tFDataSource.getActiveConnectionSize());
            joinDataSourceBo.setMaxActiveConnectionAgentId(tFAgentStat.getAgentId());
            joinDataSourceBoList.add(joinDataSourceBo);
        }
        joinDataSourceListBo.setJoinDataSourceBoList(joinDataSourceBoList);

        return joinDataSourceListBo;

    }
}
