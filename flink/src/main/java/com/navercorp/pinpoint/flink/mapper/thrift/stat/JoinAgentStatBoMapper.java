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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinAgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.flink.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStatBatch;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class JoinAgentStatBoMapper implements ThriftBoMapper<JoinAgentStatBo, TFAgentStatBatch> {

    private static JoinCpuLoadBoMapper joinCpuLoadBoMapper = new JoinCpuLoadBoMapper();

    @Override
    public JoinAgentStatBo map(TFAgentStatBatch tFAgentStatBatch) {
        if (!tFAgentStatBatch.isSetAgentStats()) {
            return null;
        }

        if (StringUtils.isEmpty(tFAgentStatBatch.getAgentId())) {
            return null;
        }

        JoinAgentStatBo joinAgentStatBo = new JoinAgentStatBo();
        int agentStatSize = tFAgentStatBatch.getAgentStats().size();
        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<>(agentStatSize);
        for (TFAgentStat tFAgentStat : tFAgentStatBatch.getAgentStats()) {
            createAndAddJoinCpuLoadBo(tFAgentStat, joinCpuLoadBoList);
        }

        joinAgentStatBo.setJoinCpuLoadBoList(joinCpuLoadBoList);
        joinAgentStatBo.setId(tFAgentStatBatch.getAgentId());
        joinAgentStatBo.setAgentStartTimestamp(tFAgentStatBatch.getStartTimestamp());
        joinAgentStatBo.setTimestamp(getTimeStamp(joinAgentStatBo));
        return joinAgentStatBo;
    }

    private long getTimeStamp(JoinAgentStatBo joinAgentStatBo) {
        List<JoinCpuLoadBo> joinCpuLoadBoList = joinAgentStatBo.getJoinCpuLoadBoList();

        if(joinCpuLoadBoList.size() != 0) {
            return joinCpuLoadBoList.get(0).getTimestamp();
        }

        return Long.MIN_VALUE;
    }

    public void createAndAddJoinCpuLoadBo(TFAgentStat tFAgentStat, List<JoinCpuLoadBo> joinCpuLoadBoList) {
        JoinCpuLoadBo joinCpuLoadBo = joinCpuLoadBoMapper.map(tFAgentStat);

        if (joinCpuLoadBo == null) {
            return;
        }

        joinCpuLoadBoList.add(joinCpuLoadBo);
    }

}
