/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.uid.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.wd.ByteSaltKey;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.uid.UidRowKeyUtils;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ResultExtractorFactory;
import com.navercorp.pinpoint.web.applicationmap.uid.ApplicationUidResponse;
import com.navercorp.pinpoint.web.vo.RangeFactory;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class HbaseMapSelfUidDao implements MapSelfUidDao {

    private static final int NUM_PARTITIONS = 4;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.MAP_SELF_V3_COUNTER;
    private static final int SALT_KEY = ByteSaltKey.NONE.size();

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RangeFactory rangeFactory;

    private final RowKeyDistributorByHashPrefix rowKeyDistributor;
    private final ResultExtractorFactory<ApplicationUidResponse> resultExtractorFactory;

    public HbaseMapSelfUidDao(HbaseOperations hbaseOperations,
                              TableNameProvider tableNameProvider,
                              RangeFactory rangeFactory,
                              RowKeyDistributorByHashPrefix rowKeyDistributor,
                              ResultExtractorFactory<ApplicationUidResponse> resultExtractorFactory) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.rangeFactory = Objects.requireNonNull(rangeFactory, "rangeFactory");
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
        this.resultExtractorFactory = Objects.requireNonNull(resultExtractorFactory, "resultExtractorFactory");
    }

    @Override
    public ApplicationUidResponse selectApplicationResponse(ServiceUid serviceUid,
                                                            ApplicationUid applicationUid,
                                                            ServiceType serviceType,
                                                            TimeWindow timeWindow) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        Objects.requireNonNull(applicationUid, "application");
        Objects.requireNonNull(timeWindow, "timeWindow");

        int serviceUidCode = serviceUid.getUid();
        long applicationUidCode = applicationUid.getUid();
        int serviceTypeCode = serviceType.getCode();
        Range windowRange = timeWindow.getWindowRange();
        Range statisticsRange = rangeFactory.createStatisticsRange(windowRange);
        Scan scan = createScan("MapUidNode", serviceUidCode, applicationUidCode, serviceTypeCode, statisticsRange, DESCRIPTOR.getName());

        TableName tableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());

        ResultsExtractor<ApplicationUidResponse> extractor = resultExtractorFactory.newMapper(timeWindow);
        ApplicationUidResponse response = hbaseOperations.findParallel(tableName, scan, rowKeyDistributor, extractor, NUM_PARTITIONS);
        if (response == null) {
            return new ApplicationUidResponse(serviceUidCode, applicationUidCode, serviceType, List.of());
        }
        logger.info("ApplicationUidResponse {}", response);
        return response;
    }

    public Scan createScan(String id, int serviceUid, long applicationUid, int serviceType, Range range, byte[] family) {
        range = rangeFactory.createStatisticsRange(range);
        if (logger.isDebugEnabled()) {
            logger.debug("scan time:{} ", range.prettyToString());
        }
        // start key is replaced by end key because timestamp has been reversed
        byte[] startKey = UidRowKeyUtils.writeRowKey(SALT_KEY, serviceUid, applicationUid, serviceType, range.getTo());
        byte[] endKey = UidRowKeyUtils.writeRowKey(SALT_KEY, serviceUid, applicationUid, serviceType, range.getFrom());

        final Scan scan = new Scan();
        scan.setCaching(100);
        scan.withStartRow(startKey);
        scan.withStopRow(endKey);
        scan.addFamily(family);
        scan.setId(id);

        return scan;
    }

}
