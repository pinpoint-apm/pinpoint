/*
 * Copyright 2017 NAVER Corp.
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
import com.navercorp.pinpoint.common.hbase.TableDescriptor;
import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.web.mapper.RangeTimestampFilter;
import com.navercorp.pinpoint.web.mapper.TimestampFilter;
import com.navercorp.pinpoint.web.mapper.stat.ApplicationStatMapper;
import com.navercorp.pinpoint.web.mapper.stat.SampledApplicationStatResultExtractor;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.AggregationStatData;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Minwoo Jung
 */
@Repository
public class HbaseApplicationStatDaoOperations {

    private static final int APPLICATION_STAT_NUM_PARTITIONS = 32;
    private static final int MAX_SCAN_CACHE_SIZE = 256;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseOperations2 hbaseOperations2;

    private final ApplicationStatHbaseOperationFactory operationFactory;

    private final TableDescriptor<HbaseColumnFamily.ApplicationStatStatistics> descriptor;

    public HbaseApplicationStatDaoOperations(HbaseOperations2 hbaseOperations2,
                                             TableDescriptor<HbaseColumnFamily.ApplicationStatStatistics> descriptor,
                                             ApplicationStatHbaseOperationFactory operationFactory) {
        this.hbaseOperations2 = Objects.requireNonNull(hbaseOperations2, "hbaseOperations2");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.operationFactory = Objects.requireNonNull(operationFactory, "operationFactory");
    }

    List<AggregationStatData> getSampledStatList(StatType statType, SampledApplicationStatResultExtractor resultExtractor, String applicationId, Range range) {
        Objects.requireNonNull(applicationId, "applicationId");
        Objects.requireNonNull(range, "range");
        Objects.requireNonNull(resultExtractor, "resultExtractor");

        Scan scan = this.createScan(statType, applicationId, range);

        TableName applicationStatAggreTableName = descriptor.getTableName();
        return hbaseOperations2.findParallel(applicationStatAggreTableName, scan, this.operationFactory.getRowKeyDistributor(), resultExtractor, APPLICATION_STAT_NUM_PARTITIONS);
    }

    ApplicationStatMapper createRowMapper(ApplicationStatDecoder decoder, Range range) {
        TimestampFilter filter = new RangeTimestampFilter(range);
        return new ApplicationStatMapper(this.operationFactory, decoder, filter);
    }

    private Scan createScan(StatType statType, String applicationId, Range range) {
        long scanRange = range.getTo() - range.getFrom();
        long expectedNumRows = ((scanRange - 1) / descriptor.getColumnFamily().TIMESPAN_MS) + 1;
        if (range.getFrom() != AgentStatUtils.getBaseTimestamp(range.getFrom())) {
            expectedNumRows++;
        }
        if (expectedNumRows > MAX_SCAN_CACHE_SIZE) {
            return this.createScan(statType, applicationId, range, MAX_SCAN_CACHE_SIZE);
        } else {
            // expectedNumRows guaranteed to be within integer range at this point
            return this.createScan(statType, applicationId, range, (int) expectedNumRows);
        }
    }

    private Scan createScan(StatType statType, String applicationId, Range range, int scanCacheSize) {
        Scan scan = this.operationFactory.createScan(applicationId, statType, range.getFrom(), range.getTo());
        scan.setCaching(scanCacheSize);
        scan.setId("ApplicationStat_" + statType);
        scan.addFamily(descriptor.getColumnFamilyName());
        return scan;
    }

}
