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
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.dao.MapStatisticsCallerDao;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.MapStatisticsTimeWindowReducer;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.RowMapperFactory;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMapUtils;
import com.navercorp.pinpoint.web.mapper.RowMapReduceResultExtractor;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowDownSampler;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowFunction;
import com.navercorp.pinpoint.web.vo.Application;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author netspider
 * @author emeroad
 * @author HyunGil Jeong
 */
@Repository
public class HbaseMapStatisticsCallerDao implements MapStatisticsCallerDao {

    private static final int MAP_STATISTICS_CALLEE_VER2_NUM_PARTITIONS = 32;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseColumnFamily.CalleeStatMap DESCRIPTOR = HbaseColumnFamily.MAP_STATISTICS_CALLEE_VER2_COUNTER;

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;

    private final RowMapperFactory<LinkDataMap> callerMapperFactory;

    private final MapScanFactory scanFactory;

    private final RowKeyDistributorByHashPrefix rowKeyDistributor;

    public HbaseMapStatisticsCallerDao(
            HbaseOperations hbaseTemplate,
            TableNameProvider tableNameProvider,
            RowMapperFactory<LinkDataMap> callerMapperFactory,
            MapScanFactory scanFactory,
            RowKeyDistributorByHashPrefix rowKeyDistributor) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.callerMapperFactory = Objects.requireNonNull(callerMapperFactory, "callerMapperFactory");
        this.scanFactory = Objects.requireNonNull(scanFactory, "scanFactory");
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
    }

    @Override
    public LinkDataMap selectCaller(Application callerApplication, Range range, boolean timeAggregated) {

        final TimeWindow timeWindow = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);

        TimeWindowFunction mapperWindow = newTimeWindow(timeAggregated);
        RowMapper<LinkDataMap> rowMapper = this.callerMapperFactory.newMapper(mapperWindow);

        ResultsExtractor<LinkDataMap> resultExtractor = new RowMapReduceResultExtractor<>(rowMapper, new MapStatisticsTimeWindowReducer(timeWindow));

        final Scan scan = scanFactory.createScan("MapCallerScan", callerApplication, range, DESCRIPTOR.getName());
        return selectOutLink(scan, DESCRIPTOR.getTable(), resultExtractor, MAP_STATISTICS_CALLEE_VER2_NUM_PARTITIONS);
    }

    private TimeWindowFunction newTimeWindow(boolean timeAggregated) {
        if (timeAggregated) {
            return TimeWindowFunction.ALL_IN_ONE;
        }
        return TimeWindowFunction.identity();
    }


    private LinkDataMap selectOutLink(Scan scan, HbaseTable table, ResultsExtractor<LinkDataMap> resultExtractor, int parallel) {
        TableName calleeTableName = tableNameProvider.getTableName(table);
        LinkDataMap linkDataMap = this.hbaseTemplate.findParallel(calleeTableName, scan, rowKeyDistributor, resultExtractor, parallel);
        logger.debug("{} {} data: {}", LinkDirection.OUT_LINK, calleeTableName.getNameAsString(), linkDataMap);
        if (LinkDataMapUtils.hasLength(linkDataMap)) {
            return linkDataMap;
        }
        return new LinkDataMap();
    }

}