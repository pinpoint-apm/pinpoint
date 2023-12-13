/*
 * Copyright 2022 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.dao.hbase.appmetric;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.hbase.HBaseUtils;
import com.navercorp.pinpoint.web.mapper.RangeTimestampFilter;
import com.navercorp.pinpoint.web.mapper.TimestampFilter;
import com.navercorp.pinpoint.web.mapper.stat.ApplicationStatMapper;
import com.navercorp.pinpoint.web.vo.stat.AggregationStatData;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author Minwoo Jung
 */
@Component
public class HbaseApplicationStatDaoOperations {

    private static final int APPLICATION_STAT_NUM_PARTITIONS = 32;
    private static final int MAX_SCAN_CACHE_SIZE = 256;

    private final Logger logger = LogManager.getLogger(this.getClass());
    private static final HbaseColumnFamily.ApplicationStatStatistics DESCRIPTOR = HbaseColumnFamily.APPLICATION_STAT_STATISTICS;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final ApplicationStatHbaseOperationFactory operationFactory;

    public HbaseApplicationStatDaoOperations(HbaseOperations hbaseOperations,
                                             TableNameProvider tableNameProvider,
                                             ApplicationStatHbaseOperationFactory operationFactory) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.operationFactory = Objects.requireNonNull(operationFactory, "operationFactory");
    }

    <OUT extends AggregationStatData> List<OUT> getSampledStatList(StatType statType,
                                 ResultsExtractor<List<OUT>> resultExtractor,
                                 String applicationId, Range range) {
        Objects.requireNonNull(applicationId, "applicationId");
        Objects.requireNonNull(resultExtractor, "resultExtractor");
        Objects.requireNonNull(range, "range");

        Scan scan = this.createScan(statType, applicationId, range);

        TableName applicationStatAggreTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseOperations.findParallel(applicationStatAggreTableName, scan, this.operationFactory.getRowKeyDistributor(), resultExtractor, APPLICATION_STAT_NUM_PARTITIONS);
    }

    <IN extends JoinStatBo> RowMapper<List<IN>> createRowMapper(ApplicationStatDecoder<IN> decoder, Range range) {
        TimestampFilter filter = new RangeTimestampFilter(range);
        return new ApplicationStatMapper<>(this.operationFactory, decoder, filter);
    }

    private Scan createScan(StatType statType, String applicationId, Range range) {
        int scanCacheSize = HBaseUtils.getScanCacheSize(range, DESCRIPTOR.TIMESPAN_MS, MAX_SCAN_CACHE_SIZE);
        return this.createScan(statType, applicationId, range, scanCacheSize);
    }

    private Scan createScan(StatType statType, String applicationId, Range range, int scanCacheSize) {
        Scan scan = this.operationFactory.createScan(applicationId, statType, range.getFrom(), range.getTo());
        scan.setCaching(scanCacheSize);
        scan.setId("ApplicationStat_" + statType);
        scan.addFamily(DESCRIPTOR.getName());
        return scan;
    }

}
