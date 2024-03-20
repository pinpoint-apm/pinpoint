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

import com.navercorp.pinpoint.collector.dao.AgentEventDao;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.ValueMapper;
import com.navercorp.pinpoint.common.hbase.util.Puts;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.AgentIdRowKeyEncoder;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
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
public class HbaseAgentEventDao implements AgentEventDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseColumnFamily.AgentEvent DESCRIPTOR = HbaseColumnFamily.AGENT_EVENT_EVENTS;

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;

    private final ValueMapper<AgentEventBo> valueMapper;

    private final AgentIdRowKeyEncoder rowKeyEncoder = new AgentIdRowKeyEncoder();

    public HbaseAgentEventDao(HbaseOperations hbaseTemplate,
                              TableNameProvider tableNameProvider,
                              ValueMapper<AgentEventBo> valueMapper) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.valueMapper = Objects.requireNonNull(valueMapper, "valueMapper");
    }


    @Override
    public void insert(AgentEventBo agentEventBo) {
        Objects.requireNonNull(agentEventBo, "agentEventBo");
        if (logger.isDebugEnabled()) {
            logger.debug("insert agent event: {}", agentEventBo.toString());
        }
        // Assert agentId
        CollectorUtils.checkAgentId(agentEventBo.getAgentId());

        final String agentId = agentEventBo.getAgentId();
        final long eventTimestamp = agentEventBo.getEventTimestamp();

        byte[] rowKey = createRowKey(agentId, eventTimestamp);

        final AgentEventType eventType = agentEventBo.getEventType();
        byte[] qualifier = Bytes.toBytes(eventType.getCode());

        TableName agentEventTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());

        byte[] value = valueMapper.mapValue(agentEventBo);
        Put put = Puts.put(rowKey, DESCRIPTOR.getName(), qualifier, value);
        this.hbaseTemplate.put(agentEventTableName, put);
    }

    byte[] createRowKey(String agentId, long eventTimestamp) {
            return rowKeyEncoder.encodeRowKey(agentId, eventTimestamp);
    }
}