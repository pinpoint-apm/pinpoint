/*
 * Copyright 2015 NAVER Corp.
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

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.AgentLifeCycleBo;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

import static com.navercorp.pinpoint.common.hbase.HbaseColumnFamily.AGENT_LIFECYCLE_STATUS;

/**
 * @author HyunGil Jeong
 */
@Component
public class AgentLifeCycleMapper implements RowMapper<AgentLifeCycleBo> {

    @Override
    public AgentLifeCycleBo mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }

        Cell valueCell = result.getColumnLatestCell(AGENT_LIFECYCLE_STATUS.getName(), HbaseColumnFamily.AGENT_LIFECYCLE_STATUS.QUALIFIER_STATES);
        
        return createAgentLifeCycleBo(valueCell);
    }
    
    private AgentLifeCycleBo createAgentLifeCycleBo(Cell valueCell) {
        if (valueCell == null) {
            return null;
        }
        byte[] value = CellUtil.cloneValue(valueCell);
        final Buffer buffer = new FixedBuffer(value);
        
        final int version = buffer.readInt();
        if (version == 0) {
            final String agentId = buffer.readPrefixedString();
            final long startTimestamp = buffer.readLong();
            final long eventTimestamp = buffer.readLong();
            final long eventIdentifier = buffer.readLong();
            final AgentLifeCycleState agentLifeCycleState = AgentLifeCycleState.getStateByCode(buffer.readShort());
            final AgentLifeCycleBo agentLifeCycleBo = new AgentLifeCycleBo(agentId, startTimestamp, eventTimestamp, eventIdentifier, agentLifeCycleState);
            return agentLifeCycleBo;
        }
        return null;
    }
    
}
