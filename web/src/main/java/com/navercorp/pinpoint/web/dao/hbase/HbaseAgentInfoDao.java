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
import com.navercorp.pinpoint.common.hbase.HbaseTableConstatns;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.TableDescriptor;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.vo.AgentInfo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
@Repository
public class HbaseAgentInfoDao implements AgentInfoDao {

    private static final int SCANNER_CACHING = 1;

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    private ResultsExtractor<AgentInfo> agentInfoResultsExtractor;

    @Autowired
    private TableDescriptor<HbaseColumnFamily.AgentInfo> descriptor;

    /**
     * Returns the very first information of the agent
     *
     * @param agentId
     */
    @Override
    public AgentInfo getInitialAgentInfo(final String agentId) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        Scan scan = createScanForInitialAgentInfo(agentId);

        TableName agentInfoTableName = descriptor.getTableName();
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

        TableName agentInfoTableName = descriptor.getTableName();
        return this.hbaseOperations2.find(agentInfoTableName, scans, agentInfoResultsExtractor);
    }

    private Scan createScanForInitialAgentInfo(String agentId) {
        Scan scan = new Scan();
        byte[] agentIdBytes = Bytes.toBytes(agentId);
        byte[] reverseStartKey = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HbaseTableConstatns.AGENT_NAME_MAX_LEN, Long.MAX_VALUE);
        scan.setStartRow(reverseStartKey);
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
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }

        Scan scan = createScan(agentId, timestamp);

        TableName agentInfoTableName = descriptor.getTableName();
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

        TableName agentInfoTableName = descriptor.getTableName();
        return this.hbaseOperations2.findParallel(agentInfoTableName, scans, agentInfoResultsExtractor);
    }

    private Scan createScan(String agentId, long currentTime) {
        Scan scan = new Scan();

        byte[] agentIdBytes = Bytes.toBytes(agentId);
        long startTime = TimeUtils.reverseTimeMillis(currentTime);
        byte[] startKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HbaseTableConstatns.AGENT_NAME_MAX_LEN, startTime);
        byte[] endKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HbaseTableConstatns.AGENT_NAME_MAX_LEN, Long.MAX_VALUE);

        scan.setStartRow(startKeyBytes);
        scan.setStopRow(endKeyBytes);
        scan.addFamily(descriptor.getColumnFamilyName());

        scan.setMaxVersions(1);
        scan.setCaching(SCANNER_CACHING);

        return scan;
    }


}
