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

package com.navercorp.pinpoint.web.dao.hbase.stat.v2;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.TableDescriptor;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.web.mapper.RangeTimestampFilter;
import com.navercorp.pinpoint.web.mapper.TimestampFilter;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.util.ListListUtils;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Component
public class HbaseAgentStatDaoOperationsV2 {

    private static final int AGENT_STAT_VER2_NUM_PARTITIONS = 32;
    private static final int MAX_SCAN_CACHE_SIZE = 256;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseOperations2 hbaseOperations2;

    private final AgentStatHbaseOperationFactory operationFactory;

    private final TableDescriptor<HbaseColumnFamily.AgentStatStatistics> descriptor;

    public HbaseAgentStatDaoOperationsV2(HbaseOperations2 hbaseOperations2,
                                         TableDescriptor<HbaseColumnFamily.AgentStatStatistics> descriptor, AgentStatHbaseOperationFactory operationFactory) {
        this.hbaseOperations2 = Objects.requireNonNull(hbaseOperations2, "hbaseOperations2");
        this.operationFactory = Objects.requireNonNull(operationFactory, "operationFactory");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
    }

    <T extends AgentStatDataPoint> List<T> getAgentStatList(AgentStatType agentStatType, AgentStatMapperV2<T> mapper, String agentId, Range range) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(range, "range");

        Scan scan = this.createScan(agentStatType, agentId, range);

        TableName agentStatTableName = descriptor.getTableName();
        List<List<T>> intermediate = hbaseOperations2.findParallel(agentStatTableName, scan, this.operationFactory.getRowKeyDistributor(), mapper, AGENT_STAT_VER2_NUM_PARTITIONS);
        int expectedSize = (int) (range.getRange() / descriptor.getColumnFamily().TIMESPAN_MS);

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

        TableName agentStatTableName = descriptor.getTableName();
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

        TableName agentStatTableName = descriptor.getTableName();
        return hbaseOperations2.findParallel(agentStatTableName, scan, this.operationFactory.getRowKeyDistributor(), resultExtractor, AGENT_STAT_VER2_NUM_PARTITIONS);
    }

    <T extends AgentStatDataPoint> AgentStatMapperV2<T> createRowMapper(AgentStatDecoder<T> decoder, Range range) {
        TimestampFilter filter = new RangeTimestampFilter(range);
        return new AgentStatMapperV2<>(this.operationFactory, decoder, filter);
    }

    private Scan createScan(AgentStatType agentStatType, String agentId, Range range) {
        long scanRange = range.getTo() - range.getFrom();
        long expectedNumRows = ((scanRange - 1) / descriptor.getColumnFamily().TIMESPAN_MS) + 1;
        if (range.getFrom() != AgentStatUtils.getBaseTimestamp(range.getFrom())) {
            expectedNumRows++;
        }
        if (expectedNumRows > MAX_SCAN_CACHE_SIZE) {
            return this.createScan(agentStatType, agentId, range, MAX_SCAN_CACHE_SIZE);
        } else {
            // expectedNumRows guaranteed to be within integer range at this point
            return this.createScan(agentStatType, agentId, range, (int) expectedNumRows);
        }
    }

    private Scan createScan(AgentStatType agentStatType, String agentId, Range range, int scanCacheSize) {
        Scan scan = this.operationFactory.createScan(agentId, agentStatType, range.getFrom(), range.getTo());
        scan.setCaching(scanCacheSize);
        scan.setId("AgentStat_" + agentStatType);
        scan.addFamily(descriptor.getColumnFamilyName());
        return scan;
    }


}
