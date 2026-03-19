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
import com.navercorp.pinpoint.common.server.bo.SimpleAgentKey;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.AgentIdRowKeyEncoder;
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.dao.AgentInfoQuery;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFactory;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
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
    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final int SCANNER_CACHING = 1;
    private static final HbaseTables.AgentInfo DESCRIPTOR = HbaseTables.AGENTINFO_INFO;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final AgentIdRowKeyEncoder rowKeyEncoder;

    private final AgentInfoFactory agentInfoFactory;
    private final ResultsExtractor<AgentInfo> agentInfoResultsExtractor;
    private final ResultsExtractor<DetailedAgentInfo> detailedAgentInfoResultsExtractor;
    private final RowMapper<AgentInfoBo> agentInfoBoMapper;


    public HbaseAgentInfoDao(HbaseOperations hbaseOperations,
                             TableNameProvider tableNameProvider,
                             AgentIdRowKeyEncoder rowKeyEncoder,
                             ServiceTypeRegistryService registryService,
                             RowMapper<AgentInfoBo> agentInfoBoMapper,
                             ResultsExtractor<AgentInfo> agentInfoResultsExtractor,
                             ResultsExtractor<DetailedAgentInfo> detailedAgentInfoResultsExtractor
    ) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.rowKeyEncoder = Objects.requireNonNull(rowKeyEncoder, "rowKeyEncoder");
        this.agentInfoFactory = new AgentInfoFactory(registryService);
        this.agentInfoBoMapper = Objects.requireNonNull(agentInfoBoMapper, "agentInfoBoMapper");
        this.agentInfoResultsExtractor = Objects.requireNonNull(agentInfoResultsExtractor, "agentInfoResultsExtractor");
        this.detailedAgentInfoResultsExtractor = Objects.requireNonNull(detailedAgentInfoResultsExtractor, "detailedAgentInfoResultsExtractor");
    }

    @Override
    public DetailedAgentInfo findDetailedAgentInfo(final String agentId, final long maxTimestamp) {
        return findAgentInfo0(agentId, maxTimestamp, detailedAgentInfoResultsExtractor, AgentInfoQuery.all());
    }

    /**
     * Returns the information of the agent with its start time closest to the given maxTimestamp
     *
     * @param agentId
     * @param maxTimestamp
     * @return
     */
    @Override
    public AgentInfo findAgentInfo(final String agentId, final long maxTimestamp) {
        return findAgentInfo0(agentId, maxTimestamp, agentInfoResultsExtractor, AgentInfoQuery.simple());
    }

    private <T> T findAgentInfo0(String agentId, long timestamp, ResultsExtractor<T> action, AgentInfoQuery query) {
        Objects.requireNonNull(agentId, "agentId");

        Scan scan = createScan(agentId, timestamp, query);
        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseOperations.find(agentInfoTableName, scan, action);
    }

    // timestamp is reversed so (fromTimestamp, toTimestamp]
    @Override
    public AgentInfo findAgentInfo(String agentId, long fromTimestamp, long toTimestamp) {
        Scan scan = createScan(agentId, fromTimestamp, toTimestamp, AgentInfoQuery.simple());
        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseOperations.find(agentInfoTableName, scan, agentInfoResultsExtractor);
    }

    @Override
    public List<DetailedAgentInfo> findDetailedAgentInfos(List<String> agentIds, final long maxTimestamp, AgentInfoQuery query) {
        return findAgentInfos0(agentIds, maxTimestamp, detailedAgentInfoResultsExtractor, query);
    }

    @Override
    public List<AgentInfo> findAgentInfos(List<String> agentIds, long timestamp) {
        return findAgentInfos0(agentIds, timestamp, agentInfoResultsExtractor, AgentInfoQuery.simple());
    }

    private <T> List<T> findAgentInfos0(List<String> agentIds, long maxTimestamp, ResultsExtractor<T> action, AgentInfoQuery query) {
        if (CollectionUtils.isEmpty(agentIds)) {
            return Collections.emptyList();
        }

        List<Scan> scans = new ArrayList<>(agentIds.size());
        for (String agentId : agentIds) {
            scans.add(createScan(agentId, maxTimestamp, query));
        }
        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseOperations.findParallel(agentInfoTableName, scans, action);
    }

    private Scan createScan(String agentId, long currentTime, AgentInfoQuery query) {
        return createScan(agentId, 0, currentTime, query);
    }

    // search from endTimestamp to fromTimestamp because timestamp is reversed
    private Scan createScan(String agentId, long fromTimestamp, long endTimestamp, AgentInfoQuery query) {
        Scan scan = new Scan();
        scan.setId("AgentId:" + agentId);
        byte[] startKeyBytes = rowKeyEncoder.encodeRowKey(agentId, endTimestamp);
        byte[] endKeyBytes = rowKeyEncoder.encodeRowKey(agentId, fromTimestamp);
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
        return scan;
    }

    @Override
    public AgentInfo getAgentInfo(String agentId, long agentStartTime) {
        Get get = createGet(agentId, agentStartTime, AgentInfoQuery.simple());
        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        AgentInfoBo agentInfoBo = this.hbaseOperations.get(agentInfoTableName, get, agentInfoBoMapper);
        return toAgentInfo(agentInfoBo);
    }

    @Override
    public List<AgentInfo> getAgentInfos(List<SimpleAgentKey> simpleAgentKeyList) {
        if (CollectionUtils.isEmpty(simpleAgentKeyList)) {
            return Collections.emptyList();
        }

        List<Get> gets = new ArrayList<>(simpleAgentKeyList.size());
        for (SimpleAgentKey simpleAgentKey : simpleAgentKeyList) {
            gets.add(createGet(simpleAgentKey.agentId(), simpleAgentKey.agentStartTime(), AgentInfoQuery.simple()));
        }
        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseOperations.get(agentInfoTableName, gets, agentInfoBoMapper).stream()
                .map(this::toAgentInfo)
                .toList();
    }

    private @Nullable AgentInfo toAgentInfo(AgentInfoBo agentInfoBo) {
        if (agentInfoBo == null) {
            return null;
        }
        return agentInfoFactory.build(agentInfoBo);
    }

    private Get createGet(String agentId, long agentStartTime, AgentInfoQuery query) {
        byte[] row = rowKeyEncoder.encodeRowKey(agentId, agentStartTime);
        Get get = new Get(row);
        get.setId("AgentId:" + agentId);
        final byte[] family = DESCRIPTOR.getName();
        if (query.hasBasic()) {
            get.addColumn(family, DESCRIPTOR.QUALIFIER_IDENTIFIER);
        }
        if (query.hasServerMetaData()) {
            get.addColumn(family, DESCRIPTOR.QUALIFIER_SERVER_META_DATA);
        }
        if (query.hasJvm()) {
            get.addColumn(family, DESCRIPTOR.QUALIFIER_JVM);
        }
        return get;
    }

    // only for agentList migration
    @Override
    public List<AgentInfoBo> fetchAgentInfoBo(int limit, long minStamp, @Nullable String lastAgentId, @Nullable Long lastAgentStartTime) {
        Scan scan = new Scan();
        setStartRow(scan, lastAgentId, lastAgentStartTime);
        if (minStamp > 0) {
            try {
                scan.setTimeRange(minStamp, Long.MAX_VALUE);
            } catch (Exception e) {
                logger.error("setTimeRange error ", e);
            }
        }
        scan.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.QUALIFIER_IDENTIFIER);
        scan.setLimit(limit);
        scan.readVersions(1);

        TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseOperations.find(agentInfoTableName, scan, agentInfoBoMapper);
    }

    private void setStartRow(Scan scan, String lastAgentId, Long startTime) {
        if (lastAgentId != null && startTime != null) {
            Buffer buffer = new FixedBuffer(HbaseTableConstants.AGENT_ID_MAX_LEN + Long.BYTES);
            buffer.putPadString(lastAgentId, HbaseTableConstants.AGENT_ID_MAX_LEN);
            buffer.putLong(LongInverter.invert(startTime));
            scan.withStartRow(buffer.getBuffer(), false);
        }
    }
}
