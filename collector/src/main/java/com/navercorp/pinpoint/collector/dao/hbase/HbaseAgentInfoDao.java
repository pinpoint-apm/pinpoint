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

package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.AgentInfoDao;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.AgentIdRowKeyEncoder;
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
    private static final HbaseColumnFamily.AgentInfo DESCRIPTOR = HbaseColumnFamily.AGENTINFO_INFO;

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;

    private final ResultsExtractor<AgentInfoBo> agentInfoResultsExtractor;
    private final AgentIdRowKeyEncoder rowKeyEncoder = new AgentIdRowKeyEncoder();

    public HbaseAgentInfoDao(HbaseOperations hbaseTemplate,
                             TableNameProvider tableNameProvider,
                             ResultsExtractor<AgentInfoBo> agentInfoResultsExtractor) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.agentInfoResultsExtractor = Objects.requireNonNull(agentInfoResultsExtractor, "agentInfoResultsExtractor");
    }

    @Override
    public void insert(AgentInfoBo agentInfo) {
        Objects.requireNonNull(agentInfo, "agentInfo");
        if (logger.isDebugEnabled()) {
            logger.debug("insert agent info. {}", agentInfo);
        }

        // Assert agentId
        CollectorUtils.checkAgentId(agentInfo.getAgentId());
        // Assert applicationName
        CollectorUtils.checkApplicationName(agentInfo.getApplicationName());
        //check agentName if set
        CollectorUtils.checkAgentName(agentInfo.getAgentName());

        final byte[] rowKey = rowKeyEncoder.encodeRowKey(AgentId.unwrap(agentInfo.getAgentId()), agentInfo.getStartTime());
        final Put put = new Put(rowKey);

        // should add additional agent information. for now added only start-time for sqlMetaData
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

    @Override
    public AgentInfoBo getAgentInfo(final AgentId agentId, final long timestamp) {
        Objects.requireNonNull(agentId, "agentId");

        final Scan scan = createScan(agentId, timestamp);
        final TableName agentInfoTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return this.hbaseTemplate.find(agentInfoTableName, scan, agentInfoResultsExtractor);
    }

    private Scan createScan(AgentId agentId, long currentTime) {
        final Scan scan = new Scan();

        final byte[] startKeyBytes = rowKeyEncoder.encodeRowKey(AgentId.unwrap(agentId), currentTime);
        final byte[] endKeyBytes = RowKeyUtils.agentIdAndTimestamp(AgentId.unwrap(agentId), Long.MAX_VALUE);

        scan.withStartRow(startKeyBytes);
        scan.withStopRow(endKeyBytes);
        scan.addFamily(DESCRIPTOR.getName());
        scan.readVersions(1);
        scan.setCaching(SCANNER_CACHING);

        return scan;
    }
}