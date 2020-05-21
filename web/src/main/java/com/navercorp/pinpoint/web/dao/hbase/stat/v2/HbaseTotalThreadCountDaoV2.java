/*
 * Copyright 2020 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.codec.stat.TotalThreadCountDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.TotalThreadCountBo;
import com.navercorp.pinpoint.web.dao.stat.TotalThreadCountDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository("totalThreadCountDaoV2")
public class HbaseTotalThreadCountDaoV2 implements TotalThreadCountDao {
    private final HbaseAgentStatDaoOperationsV2 operations;
    private final TotalThreadCountDecoder decoder;

    public HbaseTotalThreadCountDaoV2(HbaseAgentStatDaoOperationsV2 operations, TotalThreadCountDecoder decoder) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.decoder = Objects.requireNonNull(decoder, "decoder");
    }

    @Override
    public List<TotalThreadCountBo> getAgentStatList(String agentId, Range range) {
        AgentStatMapperV2<TotalThreadCountBo> mapper = operations.createRowMapper(decoder, range);
        return operations.getAgentStatList(AgentStatType.TOTAL_THREAD, mapper, agentId, range);
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        AgentStatMapperV2<TotalThreadCountBo> mapper = operations.createRowMapper(decoder, range);
        return operations.agentStatExists(AgentStatType.TOTAL_THREAD, mapper, agentId, range);
    }
}
