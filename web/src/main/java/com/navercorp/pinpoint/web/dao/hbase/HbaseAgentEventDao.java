/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.AgentIdRowKeyEncoder;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.dao.AgentEventDao;
import com.navercorp.pinpoint.common.server.util.time.Range;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
@Repository
public class HbaseAgentEventDao implements AgentEventDao {

    private static final int SCANNER_CACHE_SIZE = 20;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseColumnFamily.AgentEvent DESCRIPTOR = HbaseColumnFamily.AGENT_EVENT_EVENTS;

    private final HbaseOperations2 hbaseOperations2;

    private final TableNameProvider tableNameProvider;

    private final RowMapper<List<AgentEventBo>> agentEventMapper;

    private final ResultsExtractor<List<AgentEventBo>> agentEventResultsExtractor;

    private final AgentIdRowKeyEncoder rowKeyEncoder = new AgentIdRowKeyEncoder();

    public HbaseAgentEventDao(HbaseOperations2 hbaseOperations2,
                              TableNameProvider tableNameProvider,
                              @Qualifier("agentEventMapper") RowMapper<List<AgentEventBo>> agentEventMapper,
                              ResultsExtractor<List<AgentEventBo>> agentEventResultsExtractor) {
        this.hbaseOperations2 = Objects.requireNonNull(hbaseOperations2, "hbaseOperations2");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.agentEventMapper = Objects.requireNonNull(agentEventMapper, "agentEventMapper");
        this.agentEventResultsExtractor = Objects.requireNonNull(agentEventResultsExtractor, "agentEventResultsExtractor");
    }

    @Override
    public List<AgentEventBo> getAgentEvents(String agentId, Range range, Set<AgentEventType> excludeEventTypes) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(range, "range");

        Scan scan = new Scan();
        scan.setMaxVersions(1);
        scan.setCaching(SCANNER_CACHE_SIZE);

        scan.withStartRow(createRowKey(agentId, range.getTo()));
        scan.withStopRow(createRowKey(agentId, range.getFrom()));
        scan.addFamily(DESCRIPTOR.getName());

        if (CollectionUtils.hasLength(excludeEventTypes)) {
            FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
            for (AgentEventType excludeEventType : excludeEventTypes) {
                byte[] excludeQualifier = Bytes.toBytes(excludeEventType.getCode());
                filterList.addFilter(new QualifierFilter(CompareFilter.CompareOp.NOT_EQUAL, new BinaryComparator(excludeQualifier)));
            }
            scan.setFilter(filterList);
        }

        TableName agentEventTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        List<AgentEventBo> agentEvents = this.hbaseOperations2.find(agentEventTableName, scan, agentEventResultsExtractor);
        logger.debug("agentEvents found. {}", agentEvents);
        return agentEvents;
    }

    @Override
    public AgentEventBo getAgentEvent(String agentId, long eventTimestamp, AgentEventType eventType) {
        Objects.requireNonNull(agentId, "agentId");
        if (eventTimestamp < 0) {
            throw new IllegalArgumentException("eventTimestamp must not be less than 0");
        }
        Objects.requireNonNull(eventType, "eventType");

        final byte[] rowKey = createRowKey(agentId, eventTimestamp);
        byte[] qualifier = Bytes.toBytes(eventType.getCode());

        TableName agentEventTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        List<AgentEventBo> events = this.hbaseOperations2.get(agentEventTableName, rowKey,
                DESCRIPTOR.getName(), qualifier, this.agentEventMapper);
        if (CollectionUtils.isEmpty(events)) {
            return null;
        }
        return events.get(0);
    }

    private byte[] createRowKey(String agentId, long timestamp) {
        return rowKeyEncoder.encodeRowKey(agentId, timestamp);
    }


}
