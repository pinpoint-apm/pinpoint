/*
 * Copyright 2014 NAVER Corp.
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

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.AgentIdRowKeyEncoder;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.springframework.stereotype.Repository;

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

    private static final int SCANNER_CACHING = 1;

    private static final HbaseColumnFamily.AgentInfo DESCRIPTOR = HbaseColumnFamily.AGENTINFO_INFO;

    private final HbaseOperations2 hbaseOperations2;

    private final TableNameProvider tableNameProvider;

    private final ResultsExtractor<AgentInfo> agentInfoResultsExtractor;

    private final ResultsExtractor<DetailedAgentInfo> detailedAgentInfoResultsExtractor;

    private final AgentIdRowKeyEncoder encoder = new AgentIdRowKeyEncoder();


    public HbaseAgentInfoDao(HbaseOperations2 hbaseOperations2,
                             TableNameProvider tableNameProvider,
                             ResultsExtractor<AgentInfo> agentInfoResultsExtractor,
                             ResultsExtractor<DetailedAgentInfo> detailedAgentInfoResultsExtractor) {
        this.hbaseOperations2 = Objects.requireNonNull(hbaseOperations2, "hbaseOperations2");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.agentInfoResultsExtractor = Objects.requireNonNull(agentInfoResultsExtractor, "agentInfoResultsExtractor");
        this.detailedAgentInfoResultsExtractor = Objects.requireNonNull(detailedAgentInfoResultsExtractor, "detailedAgentInfoResultsExtractor");
    }

    @Override
    public DetailedAgentInfo getDetailedAgentInfo(final String agentId, final long timestamp) {
        return getAgentInfo0(agentId, timestamp, detailedAgentInfoResultsExtractor, AgentInfoColumn.all());
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
        return getAgentInfo0(agentId, timestamp, agentInfoResultsExtractor, AgentInfoColumn.simple());
    }

    private <T> T getAgentInfo0(String agentId, long timestamp, ResultsExtractor<T> action, AgentInfoColumn column) {
        Objects.requireNonNull(agentId, "agentId");

        Scan scan = createScan(agentId, timestamp, column);

        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseOperations2.find(agentInfoTableName, scan, action);
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

        Scan scan = createScan(agentId, startTime, endTime, AgentInfoColumn.simple());

        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseOperations2.find(agentInfoTableName, scan, agentInfoResultsExtractor);
    }

    @Override
    public List<AgentInfo> getAgentInfos(List<String> agentIds, long timestamp) {
        return getAgentInfos0(agentIds, timestamp, agentInfoResultsExtractor, AgentInfoColumn.simple());
    }

    @Override
    public List<DetailedAgentInfo> getDetailedAgentInfos(List<String> agentIds, long timestamp, boolean withServerMetadata, boolean withJVM) {
        return getAgentInfos0(agentIds, timestamp, detailedAgentInfoResultsExtractor, new AgentInfoColumn(true, withServerMetadata, withJVM));
    }

    @Override
    public List<AgentInfo> getSimpleAgentInfos(List<String> agentIds, long timestamp) {
        return getAgentInfos0(agentIds, timestamp, agentInfoResultsExtractor, AgentInfoColumn.simple());
    }

    public <T> List<T> getAgentInfos0(List<String> agentIds, long timestamp, ResultsExtractor<T> action, AgentInfoColumn column) {
        if (CollectionUtils.isEmpty(agentIds)) {
            return Collections.emptyList();
        }

        List<Scan> scans = new ArrayList<>(agentIds.size());
        for (String agentId : agentIds) {
            scans.add(createScan(agentId, timestamp, column));
        }

        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseOperations2.findParallel(agentInfoTableName, scans, action);
    }

    private Scan createScan(String agentId, long currentTime, AgentInfoColumn column) {
        return createScan(agentId, currentTime, Long.MAX_VALUE, column);
    }

    private Scan createScan(String agentId, long startTime, long endTime, AgentInfoColumn column) {
        Scan scan = new Scan();

        byte[] startKeyBytes;
        byte[] endKeyBytes;
        if (endTime == Long.MAX_VALUE) {
            startKeyBytes = encoder.encodeRowKey(agentId, startTime);
            endKeyBytes = RowKeyUtils.agentIdAndTimestamp(agentId, Long.MAX_VALUE);
        } else {
            startKeyBytes = encoder.encodeRowKey(agentId, endTime);
            endKeyBytes = encoder.encodeRowKey(agentId, startTime);
        }

        scan.withStartRow(startKeyBytes);
        scan.withStopRow(endKeyBytes);

        final byte[] family = DESCRIPTOR.getName();
        if (column.identifier) {
            scan.addColumn(family, DESCRIPTOR.QUALIFIER_IDENTIFIER);
        }
        if (column.serverMetaData) {
            scan.addColumn(family, DESCRIPTOR.QUALIFIER_SERVER_META_DATA);
        }
        if (column.jvm) {
            scan.addColumn(family, DESCRIPTOR.QUALIFIER_JVM);
        }

        scan.setMaxVersions(1);
        scan.setCaching(SCANNER_CACHING);

        return scan;
    }

    private static class AgentInfoColumn {
        private final boolean identifier;
        private final boolean serverMetaData;
        private final boolean jvm;

        public AgentInfoColumn(boolean identifier, boolean serverMetaData, boolean jvm) {
            this.identifier = identifier;
            this.serverMetaData = serverMetaData;
            this.jvm = jvm;
        }

        public static AgentInfoColumn all() {
            return new AgentInfoColumn(true, true, true);
        }

        public static AgentInfoColumn simple() {
            return new AgentInfoColumn(true, false, false);
        }

        public static AgentInfoColumn jvm() {
            return new AgentInfoColumn(true, false, true);
        }
    }
}
