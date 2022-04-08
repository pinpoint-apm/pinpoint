/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.web.dao.hbase.stat;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.hbase.HBaseUtils;
import com.navercorp.pinpoint.web.mapper.RangeTimestampFilter;
import com.navercorp.pinpoint.web.mapper.TimestampFilter;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.util.ListListUtils;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class HbaseAgentStatDaoOperations {

    private static final int AGENT_STAT_VER2_NUM_PARTITIONS = 32;
    private static final int MAX_SCAN_CACHE_SIZE = 256;

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final HbaseColumnFamily columnFamily;
    private final long timespan;

    private final HbaseOperations2 hbaseOperations2;
    private final TableNameProvider tableNameProvider;

    private final AgentStatHbaseOperationFactory operationFactory;


    public HbaseAgentStatDaoOperations(HbaseColumnFamily columnFamily, long timespan,
                                       HbaseOperations2 hbaseOperations2,
                                       TableNameProvider tableNameProvider,
                                       AgentStatHbaseOperationFactory operationFactory) {
        this.hbaseOperations2 = Objects.requireNonNull(hbaseOperations2, "hbaseOperations2");
        this.columnFamily = Objects.requireNonNull(columnFamily, "columnFamily");
        this.timespan = timespan;
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.operationFactory = Objects.requireNonNull(operationFactory, "operationFactory");
    }

    <T extends AgentStatDataPoint> List<T> getAgentStatList(AgentStatType agentStatType, AgentStatMapperV2<T> mapper, String agentId, Range range) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(range, "range");

        Scan scan = this.createScan(agentStatType, agentId, range);

        TableName agentStatTableName = tableNameProvider.getTableName(columnFamily.getTable());
        List<List<T>> intermediate = hbaseOperations2.findParallel(agentStatTableName, scan, this.operationFactory.getRowKeyDistributor(), mapper, AGENT_STAT_VER2_NUM_PARTITIONS);
        int expectedSize = (int) (range.durationMillis() / timespan);

        return ListListUtils.toList(intermediate, expectedSize);
    }

    <T extends AgentStatDataPoint> boolean agentStatExists(AgentStatType agentStatType, AgentStatMapperV2<T> mapper, String agentId, Range range) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(range, "range");

        if (logger.isDebugEnabled()) {
            logger.debug("checking for stat data existence : agentId={}, {}", agentId, range);
        }

        int resultLimit = 20;
        Scan scan = this.createScan(agentStatType, agentId, range, resultLimit);

        TableName agentStatTableName = tableNameProvider.getTableName(columnFamily.getTable());
        List<List<T>> result = hbaseOperations2.findParallel(agentStatTableName, scan, this.operationFactory.getRowKeyDistributor(), resultLimit, mapper, AGENT_STAT_VER2_NUM_PARTITIONS);
        if (result.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    <S extends SampledAgentStatDataPoint> List<S> getSampledAgentStatList(AgentStatType agentStatType, ResultsExtractor<List<S>> resultExtractor, String agentId, Range range) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(range, "range");
        Objects.requireNonNull(resultExtractor, "resultExtractor");

        Scan scan = this.createScan(agentStatType, agentId, range);

        TableName agentStatTableName = tableNameProvider.getTableName(columnFamily.getTable());
        return hbaseOperations2.findParallel(agentStatTableName, scan, this.operationFactory.getRowKeyDistributor(), resultExtractor, AGENT_STAT_VER2_NUM_PARTITIONS);
    }

    <T extends AgentStatDataPoint> AgentStatMapperV2<T> createRowMapper(AgentStatDecoder<T> decoder, Range range) {
        TimestampFilter filter = new RangeTimestampFilter(range);
        return new AgentStatMapperV2<>(this.operationFactory, decoder, filter, columnFamily);
    }

    private Scan createScan(AgentStatType agentStatType, String agentId, Range range) {
        int scanCacheSize = HBaseUtils.getScanCacheSize(range, timespan, MAX_SCAN_CACHE_SIZE);
        return this.createScan(agentStatType, agentId, range, scanCacheSize);
    }

    private Scan createScan(AgentStatType agentStatType, String agentId, Range range, int scanCacheSize) {
        Scan scan = this.operationFactory.createScan(agentId, agentStatType, range.getFrom(), range.getTo());
        scan.setCaching(scanCacheSize);
        scan.setId(agentStatType.getChartType());
        scan.addFamily(columnFamily.getName());
        return scan;
    }


}
