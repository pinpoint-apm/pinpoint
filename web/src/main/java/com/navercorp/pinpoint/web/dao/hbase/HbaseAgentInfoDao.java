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
import com.navercorp.pinpoint.web.vo.AgentInfo;
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

    private final AgentIdRowKeyEncoder encoder = new AgentIdRowKeyEncoder();


    public HbaseAgentInfoDao(HbaseOperations2 hbaseOperations2,
                             TableNameProvider tableNameProvider,
                             ResultsExtractor<AgentInfo> agentInfoResultsExtractor) {
        this.hbaseOperations2 = Objects.requireNonNull(hbaseOperations2, "hbaseOperations2");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.agentInfoResultsExtractor = Objects.requireNonNull(agentInfoResultsExtractor, "agentInfoResultsExtractor");
    }

    /**
     * Returns the very first information of the agent
     *
     * @param agentId
     */
    @Override
    public AgentInfo getInitialAgentInfo(final String agentId) {
        Objects.requireNonNull(agentId, "agentId");

        Scan scan = createScanForInitialAgentInfo(agentId);

        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseOperations2.find(agentInfoTableName, scan, agentInfoResultsExtractor);
    }

    @Override
    public List<AgentInfo> getInitialAgentInfos(List<String> agentIds) {
        if (CollectionUtils.isEmpty(agentIds)) {
            return Collections.emptyList();
        }
        List<Scan> scans = new ArrayList<>(agentIds.size());
        for (String agentId : agentIds) {
            scans.add(createScanForInitialAgentInfo(agentId));
        }

        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseOperations2.find(agentInfoTableName, scans, agentInfoResultsExtractor);
    }

    private Scan createScanForInitialAgentInfo(String agentId) {
        Scan scan = new Scan();

        byte[] reverseStartKey = RowKeyUtils.agentIdAndTimestamp(agentId, Long.MAX_VALUE);
        scan.withStartRow(reverseStartKey);
        scan.setReversed(true);
        scan.setMaxVersions(1);
        scan.setCaching(SCANNER_CACHING);
        return scan;
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
        Objects.requireNonNull(agentId, "agentId");

        Scan scan = createScan(agentId, timestamp);

        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseOperations2.find(agentInfoTableName, scan, agentInfoResultsExtractor);
    }


    /**
     *
     * @param agentId agent id
     * @param agentStartTime agent start time in milliseconds
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

        Scan scan = createScan(agentId, startTime, endTime);

        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseOperations2.find(agentInfoTableName, scan, agentInfoResultsExtractor);
    }

    @Override
    public List<AgentInfo> getAgentInfos(List<String> agentIds, long timestamp) {
        if (CollectionUtils.isEmpty(agentIds)) {
            return Collections.emptyList();
        }

        List<Scan> scans = new ArrayList<>(agentIds.size());
        for (String agentId : agentIds) {
            scans.add(createScan(agentId, timestamp));
        }

        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseOperations2.findParallel(agentInfoTableName, scans, agentInfoResultsExtractor);
    }

    private Scan createScan(String agentId, long currentTime) {
        return createScan(agentId, currentTime, Long.MAX_VALUE);
    }

    private Scan createScan(String agentId, long startTime, long endTime) {
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
        scan.addFamily(DESCRIPTOR.getName());

        scan.setMaxVersions(1);
        scan.setCaching(SCANNER_CACHING);

        return scan;
    }

}
