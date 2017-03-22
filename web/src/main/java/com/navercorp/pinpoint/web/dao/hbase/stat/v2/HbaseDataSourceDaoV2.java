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

package com.navercorp.pinpoint.web.dao.hbase.stat.v2;

import com.navercorp.pinpoint.common.server.bo.codec.stat.DataSourceDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.web.dao.stat.DataSourceDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Taejin Koo
 */
@Repository("dataSourceDaoV2")
public class HbaseDataSourceDaoV2 implements DataSourceDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DataSourceDecoder dataSourceDecoder;

    @Autowired
    private HbaseAgentStatDaoOperationsV2 operations;

    @Override
    public List<DataSourceListBo> getAgentStatList(String agentId, Range range) {
        AgentStatMapperV2<DataSourceListBo> mapper = operations.createRowMapper(dataSourceDecoder, range);
        List<DataSourceListBo> agentStatList = operations.getAgentStatList(AgentStatType.DATASOURCE, mapper, agentId, range);
        return agentStatList;
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        AgentStatMapperV2<DataSourceListBo> mapper = operations.createRowMapper(dataSourceDecoder, range);
        return operations.agentStatExists(AgentStatType.DATASOURCE, mapper, agentId, range);
    }

}
