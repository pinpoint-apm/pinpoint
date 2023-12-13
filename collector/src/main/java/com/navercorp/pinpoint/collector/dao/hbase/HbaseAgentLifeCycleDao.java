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
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.ValueMapper;
import com.navercorp.pinpoint.common.hbase.util.Puts;
import com.navercorp.pinpoint.common.server.bo.AgentLifeCycleBo;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Repository
public class HbaseAgentLifeCycleDao implements AgentLifeCycleDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseColumnFamily.AgentLifeCycleStatus DESCRIPTOR = HbaseColumnFamily.AGENT_LIFECYCLE_STATUS;

    private final HbaseOperations hbaseTemplate;

    private final TableNameProvider tableNameProvider;
    private final ValueMapper<AgentLifeCycleBo> valueMapper;

    public HbaseAgentLifeCycleDao(HbaseOperations hbaseTemplate,
                                  TableNameProvider tableNameProvider,
                                  ValueMapper<AgentLifeCycleBo> valueMapper) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
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

        TableName agentLifeCycleTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());

        byte[] value = this.valueMapper.mapValue(agentLifeCycleBo);
        Put put = Puts.put(rowKey, DESCRIPTOR.getName(), DESCRIPTOR.QUALIFIER_STATES, value);
        this.hbaseTemplate.put(agentLifeCycleTableName, put);
    }

    byte[] createRowKey(String agentId, long startTimestamp, long eventIdentifier) {
        byte[] agentIdKey = Bytes.toBytes(agentId);
        long reverseStartTimestamp = TimeUtils.reverseTimeMillis(startTimestamp);
        long reverseEventCounter = TimeUtils.reverseTimeMillis(eventIdentifier);

        byte[] rowKey = new byte[HbaseTableConstants.AGENT_ID_MAX_LEN + BytesUtils.LONG_BYTE_LENGTH + BytesUtils.LONG_BYTE_LENGTH];
        BytesUtils.writeBytes(rowKey, 0, agentIdKey);
        int offset = HbaseTableConstants.AGENT_ID_MAX_LEN;
        BytesUtils.writeLong(reverseStartTimestamp, rowKey, offset);
        offset += BytesUtils.LONG_BYTE_LENGTH;
        BytesUtils.writeLong(reverseEventCounter, rowKey, offset);

        return rowKey;
    }
}