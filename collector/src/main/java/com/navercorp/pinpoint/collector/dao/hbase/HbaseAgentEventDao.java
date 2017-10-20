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

package com.navercorp.pinpoint.collector.dao.hbase;

import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.navercorp.pinpoint.collector.dao.AgentEventDao;
import com.navercorp.pinpoint.collector.dao.hbase.mapper.AgentEventValueMapper;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

/**
 * @author HyunGil Jeong
 */
@Repository
public class HbaseAgentEventDao implements AgentEventDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    private AgentEventValueMapper valueMapper;

    @Override
    public void insert(AgentEventBo agentEventBo) {
        if (agentEventBo == null) {
            throw new NullPointerException("agentEventBo must not be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("insert event. {}", agentEventBo.toString());
        }

        final String agentId = agentEventBo.getAgentId();
        final long eventTimestamp = agentEventBo.getEventTimestamp();

        byte[] rowKey = createRowKey(agentId, eventTimestamp);

        final AgentEventType eventType = agentEventBo.getEventType();
        byte[] qualifier = Bytes.toBytes(eventType.getCode());

        this.hbaseTemplate.put(HBaseTables.AGENT_EVENT, rowKey, HBaseTables.AGENT_EVENT_CF_EVENTS, qualifier, agentEventBo, this.valueMapper);
    }

    byte[] createRowKey(String agentId, long eventTimestamp) {
        byte[] agentIdKey = BytesUtils.toBytes(agentId);
        long reverseStartTimestamp = TimeUtils.reverseTimeMillis(eventTimestamp);
        return RowKeyUtils.concatFixedByteAndLong(agentIdKey, HBaseTables.AGENT_NAME_MAX_LEN, reverseStartTimestamp);
    }

}
