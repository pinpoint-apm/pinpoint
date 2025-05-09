/*
 * Copyright 2019 NAVER Corp.
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
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.dao.ApplicationResponse;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ResultExtractorFactory;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseMapResponseTimeDao implements MapResponseDao {

    private static final int MAP_STATISTICS_SELF_VER2_NUM_PARTITIONS = 8;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.MAP_STATISTICS_SELF_VER2_COUNTER;

    private final ResultExtractorFactory<List<ResponseTime>> resultExtractFactory;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final MapScanFactory scanFactory;

    private final RowKeyDistributorByHashPrefix rowKeyDistributor;
    private final ResultExtractorFactory<ApplicationResponse> applicationHistogramResultExtractor;

    public HbaseMapResponseTimeDao(HbaseOperations hbaseOperations,
                                   TableNameProvider tableNameProvider,
                                   ResultExtractorFactory<List<ResponseTime>> resultExtractMapperFactory,
                                   ResultExtractorFactory<ApplicationResponse> applicationHistogramResultExtractor,
                                   MapScanFactory scanFactory,
                                   RowKeyDistributorByHashPrefix rowKeyDistributor) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.resultExtractFactory = Objects.requireNonNull(resultExtractMapperFactory, "resultExtractMapperFactory");
        this.applicationHistogramResultExtractor = Objects.requireNonNull(applicationHistogramResultExtractor, "applicationHistogramResultExtractor");

        this.scanFactory = Objects.requireNonNull(scanFactory, "scanFactory");
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
    }


    @Override
    public List<ResponseTime> selectResponseTime(Application application, TimeWindow timeWindow) {
        Objects.requireNonNull(application, "application");

        if (logger.isDebugEnabled()) {
            logger.debug("selectResponseTime applicationName:{}, {}", application, timeWindow);
        }

        Range windowRange = timeWindow.getWindowRange();
        Scan scan = scanFactory.createScan("MapSelfScan", application, windowRange, DESCRIPTOR.getName());

        ResultsExtractor<List<ResponseTime>> resultsExtractor = resultExtractFactory.newMapper(timeWindow);

        TableName mapStatisticsSelfTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        List<ResponseTime> responseTimeList = hbaseOperations.findParallel(mapStatisticsSelfTableName, scan, rowKeyDistributor,
                resultsExtractor, MAP_STATISTICS_SELF_VER2_NUM_PARTITIONS);

        if (responseTimeList.isEmpty()) {
            return List.of();
        }
        return responseTimeList;
    }

    @Override
    public ApplicationResponse selectApplicationResponse(Application application, TimeWindow timeWindow) {
        Objects.requireNonNull(application, "application");

        if (logger.isDebugEnabled()) {
            logger.debug("selectResponseTime applicationName:{}, {}", application, timeWindow);
        }

        Range windowRange = timeWindow.getWindowRange();
        Scan scan = scanFactory.createScan("MapSelfScan", application, windowRange, DESCRIPTOR.getName());

        ResultsExtractor<ApplicationResponse> mapper = applicationHistogramResultExtractor.newMapper(timeWindow);
        TableName mapStatisticsSelfTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());

        ApplicationResponse histogram = hbaseOperations.findParallel(mapStatisticsSelfTableName, scan, rowKeyDistributor,
                mapper, MAP_STATISTICS_SELF_VER2_NUM_PARTITIONS);
        if (histogram == null) {
            return ApplicationResponse.newBuilder(application).build();
        }
        return histogram;
    }
}