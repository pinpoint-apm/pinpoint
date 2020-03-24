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

import com.navercorp.pinpoint.collector.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstatns;
import com.navercorp.pinpoint.common.hbase.TableDescriptor;
import com.navercorp.pinpoint.common.hbase.ValueMapper;
import com.navercorp.pinpoint.common.server.bo.AgentLifeCycleBo;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Repository
public class HbaseAgentLifeCycleDao implements AgentLifeCycleDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseOperations2 hbaseTemplate;

    private final TableDescriptor<HbaseColumnFamily.AgentLifeCycleStatus> descriptor;

    private final ValueMapper<AgentLifeCycleBo> valueMapper;

    public HbaseAgentLifeCycleDao(HbaseOperations2 hbaseTemplate, TableDescriptor<HbaseColumnFamily.AgentLifeCycleStatus> descriptor, ValueMapper<AgentLifeCycleBo> valueMapper) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.valueMapper = Objects.requireNonNull(valueMapper, "valueMapper");
    }

    @Override
    public void insert(AgentLifeCycleBo agentLifeCycleBo) {
        Objects.requireNonNull(agentLifeCycleBo, "agentLifeCycleBo");
        if (logger.isDebugEnabled()) {
            logger.debug("insert agent life cycle. {}", agentLifeCycleBo.toString());
        }

        // Assert agentId
        CollectorUtils.checkAgentId(agentLifeCycleBo.getAgentId());

        final String agentId = agentLifeCycleBo.getAgentId();
        final long startTimestamp = agentLifeCycleBo.getStartTimestamp();
        final long eventIdentifier = agentLifeCycleBo.getEventIdentifier();

        byte[] rowKey = createRowKey(agentId, startTimestamp, eventIdentifier);

        TableName agentLifeCycleTableName = descriptor.getTableName();
        this.hbaseTemplate.put(agentLifeCycleTableName, rowKey, descriptor.getColumnFamilyName(), descriptor.getColumnFamily().QUALIFIER_STATES,
                agentLifeCycleBo, this.valueMapper);
    }

    byte[] createRowKey(String agentId, long startTimestamp, long eventIdentifier) {
        byte[] agentIdKey = Bytes.toBytes(agentId);
        long reverseStartTimestamp = TimeUtils.reverseTimeMillis(startTimestamp);
        long reverseEventCounter = TimeUtils.reverseTimeMillis(eventIdentifier);

        byte[] rowKey = new byte[HbaseTableConstatns.AGENT_NAME_MAX_LEN + BytesUtils.LONG_BYTE_LENGTH + BytesUtils.LONG_BYTE_LENGTH];
        BytesUtils.writeBytes(rowKey, 0, agentIdKey);
        int offset = HbaseTableConstatns.AGENT_NAME_MAX_LEN;
        BytesUtils.writeLong(reverseStartTimestamp, rowKey, offset);
        offset += BytesUtils.LONG_BYTE_LENGTH;
        BytesUtils.writeLong(reverseEventCounter, rowKey, offset);

        return rowKey;
    }
}