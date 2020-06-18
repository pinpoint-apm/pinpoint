/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.codec.stat.LoadedClassCountDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.web.dao.stat.LoadedClassCountDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository("loadedClassDaoV2")
public class HbaseLoadedClassDaoV2 implements LoadedClassCountDao {

    private final HbaseAgentStatDaoOperationsV2 operations;
    private final LoadedClassCountDecoder loadedClassDecoder;

    public HbaseLoadedClassDaoV2(HbaseAgentStatDaoOperationsV2 operations, LoadedClassCountDecoder loadedClassDecoder) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.loadedClassDecoder = Objects.requireNonNull(loadedClassDecoder, "directBufferDecoder");
    }

    @Override
    public List<LoadedClassBo> getAgentStatList(String agentId, Range range) {
        AgentStatMapperV2<LoadedClassBo> mapper = operations.createRowMapper(loadedClassDecoder, range);
        return operations.getAgentStatList(AgentStatType.LOADED_CLASS, mapper, agentId, range);
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        AgentStatMapperV2<LoadedClassBo> mapper = operations.createRowMapper(loadedClassDecoder, range);
        return operations.agentStatExists(AgentStatType.LOADED_CLASS, mapper, agentId, range);
    }

}
