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

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFactory;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentInfo;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Component
public class DetailedAgentInfoResultsExtractor implements ResultsExtractor<DetailedAgentInfo> {
    private final AgentInfoFactory factory;
    private final RowMapper<AgentInfoBo> agentInfoMapper;

    public DetailedAgentInfoResultsExtractor(ServiceTypeRegistryService registryService,
                                             RowMapper<AgentInfoBo> agentInfoMapper) {
        Objects.requireNonNull(registryService, "registryService");

        this.factory = new AgentInfoFactory(registryService);
        this.agentInfoMapper = Objects.requireNonNull(agentInfoMapper, "agentInfoMapper");
    }

    @Override
    public DetailedAgentInfo extractData(ResultScanner results) throws Exception {
        for (Result result : results) {
            AgentInfoBo agentInfoBo = agentInfoMapper.mapRow(result, 0);
            AgentInfo agentInfo = factory.build(agentInfoBo);
            return new DetailedAgentInfo(agentInfo, agentInfoBo.getServerMetaData(), agentInfoBo.getJvmInfo());
        }
        return null;
    }
}
