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

import com.navercorp.pinpoint.collector.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.collector.dao.hbase.mapper.AgentLifeCycleValueMapper;
import com.navercorp.pinpoint.common.server.bo.AgentLifeCycleBo;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

/**
 * @author HyunGil Jeong
 */
@Repository
public class HbaseAgentLifeCycleDao implements AgentLifeCycleDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    private AgentLifeCycleValueMapper valueMapper;

    @Override
    public void insert(AgentLifeCycleBo agentLifeCycleBo) {
        if (agentLifeCycleBo == null) {
            throw new NullPointerException("agentLifeCycleBo must not be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("insert agent life cycle. {}", agentLifeCycleBo.toString());
        }

        final String agentId = agentLifeCycleBo.getAgentId();
        final long startTimestamp = agentLifeCycleBo.getStartTimestamp();
        final long eventIdentifier = agentLifeCycleBo.getEventIdentifier();

        byte[] rowKey = createRowKey(agentId, startTimestamp, eventIdentifier);

        this.hbaseTemplate.put(HBaseTables.AGENT_LIFECYCLE, rowKey, HBaseTables.AGENT_LIFECYCLE_CF_STATUS, HBaseTables.AGENT_LIFECYCLE_CF_STATUS_QUALI_STATES,
                agentLifeCycleBo, this.valueMapper);
    }

    byte[] createRowKey(String agentId, long startTimestamp, long eventIdentifier) {
        byte[] agentIdKey = Bytes.toBytes(agentId);
        long reverseStartTimestamp = TimeUtils.reverseTimeMillis(startTimestamp);
        long reverseEventCounter = TimeUtils.reverseTimeMillis(eventIdentifier);

        byte[] rowKey = new byte[HBaseTables.AGENT_NAME_MAX_LEN + BytesUtils.LONG_BYTE_LENGTH + BytesUtils.LONG_BYTE_LENGTH];
        BytesUtils.writeBytes(rowKey, 0, agentIdKey);
        int offset = HBaseTables.AGENT_NAME_MAX_LEN;
        BytesUtils.writeLong(reverseStartTimestamp, rowKey, offset);
        offset += BytesUtils.LONG_BYTE_LENGTH;
        BytesUtils.writeLong(reverseEventCounter, rowKey, offset);

        return rowKey;
    }

}
