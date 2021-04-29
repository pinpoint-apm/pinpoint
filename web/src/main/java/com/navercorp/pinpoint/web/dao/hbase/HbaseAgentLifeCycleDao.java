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

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableDescriptor;
import com.navercorp.pinpoint.common.server.bo.AgentLifeCycleBo;
import com.navercorp.pinpoint.common.server.bo.SimpleAgentKey;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.vo.AgentStatus;

import com.navercorp.pinpoint.web.vo.AgentStatusQuery;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author HyunGil Jeong
 */
@Repository
public class HbaseAgentLifeCycleDao implements AgentLifeCycleDao {

    private static final int SCANNER_CACHING = 20;

    private final HbaseOperations2 hbaseOperations2;

    private final RowMapper<AgentLifeCycleBo> agentLifeCycleMapper;

    private final TableDescriptor<HbaseColumnFamily.AgentLifeCycleStatus> descriptor;

    public HbaseAgentLifeCycleDao(TableDescriptor<HbaseColumnFamily.AgentLifeCycleStatus> descriptor, HbaseOperations2 hbaseOperations2,
                                  @Qualifier("agentLifeCycleMapper")RowMapper<AgentLifeCycleBo> agentLifeCycleMapper) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.hbaseOperations2 = Objects.requireNonNull(hbaseOperations2, "hbaseOperations2");
        this.agentLifeCycleMapper = Objects.requireNonNull(agentLifeCycleMapper, "agentLifeCycleMapper");

    }

    @Override
    public AgentStatus getAgentStatus(String agentId, long timestamp) {
        Objects.requireNonNull(agentId, "agentId");
        Assert.isTrue(timestamp >= 0, "timestamp must not be less than 0");

        Scan scan = createScan(agentId, 0, timestamp);

        TableName agentLifeCycleTableName = descriptor.getTableName();
        AgentLifeCycleBo agentLifeCycleBo = this.hbaseOperations2.find(agentLifeCycleTableName, scan, new MostRecentAgentLifeCycleResultsExtractor(this.agentLifeCycleMapper, timestamp));
        return createAgentStatus(agentId, agentLifeCycleBo);
    }

    @Override
    public Optional<AgentStatus> getAgentStatus(String agentId, long agentStartTimestamp, long timestamp) {
        if (agentId == null) {
            return Optional.empty();
        }
        Assert.isTrue(timestamp >= 0, "timestamp must not be less than 0");
        // startTimestamp is stored in reverse order
        final long toTimestamp = agentStartTimestamp;
        final long fromTimestamp = toTimestamp - 1;
        Scan scan = createScan(agentId, fromTimestamp, toTimestamp);

        TableName agentLifeCycleTableName = descriptor.getTableName();
        AgentLifeCycleBo agentLifeCycleBo = this.hbaseOperations2.find(agentLifeCycleTableName, scan, new MostRecentAgentLifeCycleResultsExtractor(this.agentLifeCycleMapper, timestamp));
        AgentStatus agentStatus = createAgentStatus(agentId, agentLifeCycleBo);
        return Optional.of(agentStatus);
    }

    /**
     *
     * @param agentStatusQuery agentId and agentStartTime
     */
    @Override
    public List<Optional<AgentStatus>> getAgentStatus(AgentStatusQuery agentStatusQuery) {
        Objects.requireNonNull(agentStatusQuery, "agentStatusQuery");
        if (agentStatusQuery.getAgentKeys().isEmpty()) {
            return Collections.emptyList();
        }
        List<SimpleAgentKey> agentKeyList = agentStatusQuery.getAgentKeys();
        List<Scan> scans = new ArrayList<>(agentKeyList.size());
        for (SimpleAgentKey agentInfo : agentKeyList) {
            if (agentInfo != null) {
                final String agentId = agentInfo.getAgentId();
                // startTimestamp is stored in reverse order
                final long toTimestamp = agentInfo.getAgentStartTime();
                final long fromTimestamp = toTimestamp - 1;
                scans.add(createScan(agentId, fromTimestamp, toTimestamp));
            }
        }

        ResultsExtractor<AgentLifeCycleBo> action = new MostRecentAgentLifeCycleResultsExtractor(this.agentLifeCycleMapper, agentStatusQuery.getQueryTimestamp());
        TableName agentLifeCycleTableName = descriptor.getTableName();
        List<AgentLifeCycleBo> agentLifeCycles = this.hbaseOperations2.findParallel(agentLifeCycleTableName, scans, action);

        int idx = 0;
        List<Optional<AgentStatus>> agentStatusResult = new ArrayList<>(agentKeyList.size());
        for (SimpleAgentKey agentInfo : agentKeyList) {
            if (agentInfo != null) {
                AgentStatus agentStatus = createAgentStatus(agentInfo.getAgentId(), agentLifeCycles.get(idx++));
                agentStatusResult.add(Optional.of(agentStatus));
            } else {
                agentStatusResult.add(Optional.empty());
            }
        }
        return agentStatusResult;
    }

    private Scan createScan(String agentId, long fromTimestamp, long toTimestamp) {
        byte[] agentIdBytes = Bytes.toBytes(agentId);
        long reverseFromTimestamp = TimeUtils.reverseTimeMillis(fromTimestamp);
        long reverseToTimestamp = TimeUtils.reverseTimeMillis(toTimestamp);
        byte[] startKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HbaseTableConstants.AGENT_ID_MAX_LEN, reverseToTimestamp);
        byte[] endKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HbaseTableConstants.AGENT_ID_MAX_LEN, reverseFromTimestamp);

        Scan scan = new Scan(startKeyBytes, endKeyBytes);
        scan.addColumn(descriptor.getColumnFamilyName(), descriptor.getColumnFamily().QUALIFIER_STATES);
        scan.setMaxVersions(1);
        scan.setCaching(SCANNER_CACHING);

        return scan;
    }

    private AgentStatus createAgentStatus(String agentId, AgentLifeCycleBo agentLifeCycle) {
        if (agentLifeCycle == null) {
            return new AgentStatus(agentId, AgentLifeCycleState.UNKNOWN, 0);
        } else {
            return new AgentStatus(agentLifeCycle.getAgentId(), agentLifeCycle.getAgentLifeCycleState(), agentLifeCycle.getEventTimestamp());
        }
    }

    private static class MostRecentAgentLifeCycleResultsExtractor implements ResultsExtractor<AgentLifeCycleBo> {

        private final RowMapper<AgentLifeCycleBo> agentLifeCycleMapper;
        private final long queryTimestamp;

        private MostRecentAgentLifeCycleResultsExtractor(RowMapper<AgentLifeCycleBo> agentLifeCycleMapper, long queryTimestamp) {
            this.agentLifeCycleMapper = agentLifeCycleMapper;
            this.queryTimestamp = queryTimestamp;
        }

        @Override
        public AgentLifeCycleBo extractData(ResultScanner results) throws Exception {
            int found = 0;
            for (Result result : results) {
                AgentLifeCycleBo agentLifeCycle = this.agentLifeCycleMapper.mapRow(result, found++);
                if (agentLifeCycle.getEventTimestamp() < this.queryTimestamp) {
                    return agentLifeCycle;
                }
            }
            return null;
        }
    }


}
