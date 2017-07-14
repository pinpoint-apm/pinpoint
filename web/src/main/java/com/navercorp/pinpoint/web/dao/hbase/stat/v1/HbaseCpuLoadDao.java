/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.web.dao.hbase.stat.v1;

import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.web.dao.stat.CpuLoadDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV1;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Deprecated
@Repository("cpuLoadDaoV1")
public class HbaseCpuLoadDao implements CpuLoadDao {

    @Autowired
    private AgentStatMapperV1.CpuLoadMapper mapper;

    @Autowired
    private Aggregator.CpuLoadAggregator aggregator;

    @Autowired
    private HbaseAgentStatDaoOperations operations;

    @Override
    public List<CpuLoadBo> getAgentStatList(String agentId, Range range) {
        return operations.getAgentStatList(mapper, aggregator, agentId, range);
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        return operations.agentStatExists(mapper, agentId, range);
    }
}
