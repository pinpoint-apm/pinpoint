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
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Component
public class AgentEventMapper implements RowMapper<List<AgentEventBo>> {

    @Override
    public List<AgentEventBo> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<AgentEventBo> agentEvents = new ArrayList<>();
        for (Cell cell : result.rawCells()) {
            byte[] qualifier = CellUtil.cloneQualifier(cell);
            final AgentEventType eventType = AgentEventType.getTypeByCode(BytesUtils.bytesToInt(qualifier, 0));
            if (eventType == null) {
                continue;
            }
            
            byte[] value = CellUtil.cloneValue(cell);
            final Buffer buffer = new FixedBuffer(value);
            
            final int version = buffer.readInt();
            switch (version) {
                case 0 :
                    final String agentId = buffer.readPrefixedString();
                    final long startTimestamp = buffer.readLong();
                    final long eventTimestamp = buffer.readLong();
                    final byte[] eventMessage = buffer.readPrefixedBytes();
                    final AgentEventBo agentEvent = new AgentEventBo(version, agentId, startTimestamp, eventTimestamp, eventType);
                    agentEvent.setEventBody(eventMessage);
                    agentEvents.add(agentEvent);
                    break;
                default :
                    break;
            }
        }
        
        return agentEvents;
    }

}
