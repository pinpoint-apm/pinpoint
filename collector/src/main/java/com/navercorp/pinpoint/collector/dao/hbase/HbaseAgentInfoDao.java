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

package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.AgentInfoDao;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.AgentIdRowKeyEncoder;
import com.navercorp.pinpoint.common.server.dao.hbase.mapper.SingleResultsExtractor;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author emeroad
 * @author jaehong.kim
 */
@Repository
public class HbaseAgentInfoDao implements AgentInfoDao {
    private static final int SCANNER_CACHING = 1;

    private final Logger logger = LogManager.getLogger(this.getClass());
    private static final HbaseTables.AgentInfo DESCRIPTOR = HbaseTables.AGENTINFO_INFO;

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;

    private final ResultsExtractor<AgentInfoBo> agentInfoResultsExtractor;
    private final AgentIdRowKeyEncoder rowKeyEncoder;

    public HbaseAgentInfoDao(HbaseOperations hbaseTemplate,
                             AgentIdRowKeyEncoder rowKeyEncoder,
                             TableNameProvider tableNameProvider,
                             RowMapper<AgentInfoBo> agentInfoMapper) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.rowKeyEncoder = Objects.requireNonNull(rowKeyEncoder, "rowKeyEncoder");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");

        this.agentInfoResultsExtractor = new SingleResultsExtractor<>(agentInfoMapper);
    }

    @Override
    public void insert(AgentInfoBo agentInfo) {
        Objects.requireNonNull(agentInfo, "agentInfo");
        if (logger.isDebugEnabled()) {
            logger.debug("insert agent info. {}", agentInfo);
        }

        final byte[] rowKey = rowKeyEncoder.encodeRowKey(agentInfo.getAgentId(), agentInfo.getStartTime());
        final Put put = new Put(rowKey, true);

        // should add additional agent informations. for now added only starttime for sqlMetaData
        final byte[] agentInfoBoValue = agentInfo.writeValue();
        put.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.QUALIFIER_IDENTIFIER, agentInfoBoValue);

        if (agentInfo.getServerMetaData() != null) {
            final byte[] serverMetaDataBoValue = agentInfo.getServerMetaData().writeValue();
            put.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.QUALIFIER_SERVER_META_DATA, serverMetaDataBoValue);
        }

        if (agentInfo.getJvmInfo() != null) {
            final byte[] jvmInfoBoValue = agentInfo.getJvmInfo().writeValue();
            put.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.QUALIFIER_JVM, jvmInfoBoValue);
        }

        final TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.put(agentInfoTableName, put);
    }

    public AgentInfoBo getSimpleAgentInfo(final String agentId, final long timestamp) {
        Objects.requireNonNull(agentId, "agentId");

        final TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return getSimpleAgentInfoBoByScanner(agentId, timestamp, agentInfoTableName);
//        return getAgentInfoBoByGet(agentId, timestamp, agentInfoTableName);
    }

//    private AgentInfoBo getSimpleAgentInfoBoByGet(String agentId, long timestamp, TableName agentInfoTableName) {
//        byte[] rowKey = rowKeyEncoder.encodeRowKey(agentId, timestamp);
//        Get get = new Get(rowKey);
//        get.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.QUALIFIER_IDENTIFIER);
//        return hbaseTemplate.get(agentInfoTableName, get, agentInfoMapper);
//    }

    private AgentInfoBo getSimpleAgentInfoBoByScanner(String agentId, long timestamp, TableName agentInfoTableName) {
        final Scan scan = createScan(agentId, timestamp);
        return this.hbaseTemplate.find(agentInfoTableName, scan, agentInfoResultsExtractor);
    }

    private Scan createScan(String agentId, long currentTime) {
        final Scan scan = new Scan();

        final byte[] startKeyBytes = rowKeyEncoder.encodeRowKey(agentId, currentTime);
        final byte[] endKeyBytes = RowKeyUtils.agentIdAndTimestamp(agentId, Long.MAX_VALUE);

        scan.withStartRow(startKeyBytes);
        scan.withStopRow(endKeyBytes);

        scan.readVersions(1);
        scan.setOneRowLimit();
        scan.setCaching(SCANNER_CACHING);

        scan.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.QUALIFIER_IDENTIFIER);

        return scan;
    }
}