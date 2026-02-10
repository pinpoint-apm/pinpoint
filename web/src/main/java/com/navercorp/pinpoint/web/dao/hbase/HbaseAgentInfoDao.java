/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.AgentIdRowKeyEncoder;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.dao.AgentInfoQuery;
import com.navercorp.pinpoint.web.mapper.Timestamped;
import com.navercorp.pinpoint.web.mapper.TimestampedMapper;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentInfo;
import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.util.unit.DataSize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
@Repository
public class HbaseAgentInfoDao implements AgentInfoDao {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final int SCANNER_CACHING = 1;
    private static final long MAX_RESULT_BYTES = DataSize.ofBytes(1).toBytes();

    private static final HbaseTables.AgentInfo DESCRIPTOR = HbaseTables.AGENTINFO_INFO;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final ResultsExtractor<AgentInfo> agentInfoResultsExtractor;
    private final ResultsExtractor<DetailedAgentInfo> detailedAgentInfoResultsExtractor;
    private final RowMapper<Timestamped<AgentInfoBo>> timestampedRowMapper;

    private final AgentIdRowKeyEncoder rowKeyEncoder;


    public HbaseAgentInfoDao(HbaseOperations hbaseOperations,
                             AgentIdRowKeyEncoder rowKeyEncoder,
                             TableNameProvider tableNameProvider,
                             ResultsExtractor<AgentInfo> agentInfoResultsExtractor,
                             ResultsExtractor<DetailedAgentInfo> detailedAgentInfoResultsExtractor,
                             RowMapper<AgentInfoBo> agentInfoMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.rowKeyEncoder = Objects.requireNonNull(rowKeyEncoder, "rowKeyEncoder");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.agentInfoResultsExtractor = Objects.requireNonNull(agentInfoResultsExtractor, "agentInfoResultsExtractor");
        this.detailedAgentInfoResultsExtractor = Objects.requireNonNull(detailedAgentInfoResultsExtractor, "detailedAgentInfoResultsExtractor");
        this.timestampedRowMapper = new TimestampedMapper<>(Objects.requireNonNull(agentInfoMapper, "agentInfoMapper"));
    }

    @Override
    public DetailedAgentInfo getDetailedAgentInfo(final String agentId, final long timestamp) {
        return getAgentInfo0(agentId, timestamp, detailedAgentInfoResultsExtractor, AgentInfoQuery.all());
    }

    /**
     * Returns the information of the agent with its start time closest to the given timestamp
     *
     * @param agentId
     * @param timestamp
     * @return
     */
    @Override
    public AgentInfo getAgentInfo(final String agentId, final long timestamp) {
        return getAgentInfo0(agentId, timestamp, agentInfoResultsExtractor, AgentInfoQuery.simple());
    }

    private <T> T getAgentInfo0(String agentId, long timestamp, ResultsExtractor<T> action, AgentInfoQuery query) {
        Objects.requireNonNull(agentId, "agentId");

        Scan scan = createScan(agentId, timestamp, query);

        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseOperations.find(agentInfoTableName, scan, action);
    }


    /**
     * @param agentId                 agent id
     * @param agentStartTime          agent start time in milliseconds
     * @param deltaTimeInMilliSeconds limit the scan range in case of scanning for a non-exist agent
     * @return
     */
    @Override
    public AgentInfo getAgentInfo(String agentId, long agentStartTime, int deltaTimeInMilliSeconds) {
        Objects.requireNonNull(agentId, "agentId");

        if (agentStartTime <= 0) {
            throw new IllegalArgumentException("agentStartTime must be greater than 0");
        }
        if (deltaTimeInMilliSeconds < 0) {
            throw new IllegalArgumentException("deltaTimeInMilliSeconds must be greater than or equal to 0");
        }

        final long startTime = agentStartTime - deltaTimeInMilliSeconds;
        final long endTime = agentStartTime + deltaTimeInMilliSeconds;

        Scan scan = createScan(agentId, startTime, endTime, AgentInfoQuery.simple());

        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseOperations.find(agentInfoTableName, scan, agentInfoResultsExtractor);
    }


    @Override
    public List<DetailedAgentInfo> getDetailedAgentInfos(List<String> agentIds, long timestamp, AgentInfoQuery query) {
        return getAgentInfos0(agentIds, timestamp, detailedAgentInfoResultsExtractor, query);
    }

    @Override
    public List<AgentInfo> getSimpleAgentInfos(List<String> agentIds, long timestamp) {
        return getAgentInfos0(agentIds, timestamp, agentInfoResultsExtractor, AgentInfoQuery.simple());
    }

    public <T> List<T> getAgentInfos0(List<String> agentIds, long timestamp, ResultsExtractor<T> action, AgentInfoQuery query) {
        if (CollectionUtils.isEmpty(agentIds)) {
            return Collections.emptyList();
        }

        List<Scan> scans = new ArrayList<>(agentIds.size());
        for (String agentId : agentIds) {
            scans.add(createScan(agentId, timestamp, query));
        }

        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseOperations.findParallel(agentInfoTableName, scans, action);
    }

    private Scan createScan(String agentId, long currentTime, AgentInfoQuery query) {
        return createScan(agentId, currentTime, Long.MAX_VALUE, query);
    }

    private Scan createScan(String agentId, long startTime, long endTime, AgentInfoQuery query) {
        Scan scan = new Scan();
        scan.setId("AgentId:" + agentId);

        byte[] startKeyBytes;
        byte[] endKeyBytes;
        if (endTime == Long.MAX_VALUE) {
            startKeyBytes = rowKeyEncoder.encodeRowKey(agentId, startTime);
            endKeyBytes = RowKeyUtils.agentIdAndTimestamp(agentId, Long.MAX_VALUE);
        } else {
            startKeyBytes = rowKeyEncoder.encodeRowKey(agentId, endTime);
            endKeyBytes = rowKeyEncoder.encodeRowKey(agentId, startTime);
        }

        scan.withStartRow(startKeyBytes);
        scan.withStopRow(endKeyBytes);

        final byte[] family = DESCRIPTOR.getName();
        if (query.hasBasic()) {
            scan.addColumn(family, DESCRIPTOR.QUALIFIER_IDENTIFIER);
        }
        if (query.hasServerMetaData()) {
            scan.addColumn(family, DESCRIPTOR.QUALIFIER_SERVER_META_DATA);
        }
        if (query.hasJvm()) {
            scan.addColumn(family, DESCRIPTOR.QUALIFIER_JVM);
        }

        scan.readVersions(1);
        scan.setCaching(SCANNER_CACHING);
        scan.setOneRowLimit();
        scan.setMaxResultSize(MAX_RESULT_BYTES);
//        scan.setAsyncPrefetch(false);

        return scan;
    }

    // only for agentList migration
    @Override
    public List<Timestamped<AgentInfoBo>> getAgentInfo(int limit, long fromTimestamp, @Nullable String lastAgentId, long startTime) {
        Scan scan = new Scan();
        setStartRow(scan, lastAgentId, startTime);
        if (fromTimestamp > 0) {
            try {
                scan.setTimeRange(fromTimestamp, Long.MAX_VALUE);
            } catch (Exception e) {
                logger.error("setTimeRange error ", e);
            }
        }
        scan.setLimit(limit);
        scan.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.QUALIFIER_IDENTIFIER);
        scan.readVersions(1);

        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseOperations.find(agentInfoTableName, scan, timestampedRowMapper);

    }

    private void setStartRow(Scan scan, String lastAgentId, long startTime) {
        if (lastAgentId != null && startTime > 0) {
            Buffer buffer = new FixedBuffer(HbaseTableConstants.AGENT_ID_MAX_LEN + Long.BYTES);
            buffer.putPadString(lastAgentId, HbaseTableConstants.AGENT_ID_MAX_LEN);
            buffer.putLong(LongInverter.invert(startTime));
            scan.withStartRow(buffer.getBuffer(), false);
        }
    }
}
