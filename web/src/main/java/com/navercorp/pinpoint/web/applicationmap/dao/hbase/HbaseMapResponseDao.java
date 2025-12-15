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

package com.navercorp.pinpoint.web.applicationmap.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.dao.ApplicationResponse;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ResultExtractorFactory;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 *
 * @author emeroad
 *
 */
@Repository
public class HbaseMapResponseDao implements MapResponseDao {

    private static final int NUM_PARTITIONS = 8;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HbaseColumnFamily table;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final MapScanFactory scanFactory;

    private final RowKeyDistributorByHashPrefix rowKeyDistributor;
    private final ResultExtractorFactory<ApplicationResponse> resultExtractor;

    public HbaseMapResponseDao(HbaseColumnFamily table,
                               HbaseOperations hbaseOperations,
                               TableNameProvider tableNameProvider,
                               ResultExtractorFactory<ApplicationResponse> resultExtractor,
                               MapScanFactory scanFactory,
                               RowKeyDistributorByHashPrefix rowKeyDistributor) {
        this.table = Objects.requireNonNull(table, "table");
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.resultExtractor = Objects.requireNonNull(resultExtractor, "resultExtractor");

        this.scanFactory = Objects.requireNonNull(scanFactory, "scanFactory");
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
    }

    public ApplicationResponse selectApplicationResponse(Application application, TimeWindow timeWindow) {
        Objects.requireNonNull(application, "application");

        if (logger.isDebugEnabled()) {
            logger.debug("selectApplicationResponse applicationName:{}, {}", application, timeWindow);
        }

        Range windowRange = timeWindow.getWindowRange();
        Scan scan = scanFactory.createScan("MapAppSelf", ServiceUid.DEFAULT_SERVICE_UID_CODE, application, windowRange, table.getName());

        ResultsExtractor<ApplicationResponse> mapper = resultExtractor.newMapper(timeWindow, application);
        TableName mapStatisticsSelfTableName = tableNameProvider.getTableName(table.getTable());

        ApplicationResponse histogram = hbaseOperations.findParallel(mapStatisticsSelfTableName, scan, rowKeyDistributor,
                mapper, NUM_PARTITIONS);
        if (histogram == null) {
            return ApplicationResponse.newBuilder(application).build();
        }
        return histogram;
    }
}
