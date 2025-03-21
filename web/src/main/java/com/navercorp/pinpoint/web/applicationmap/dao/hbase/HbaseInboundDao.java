/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.web.applicationmap.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowFunction;
import com.navercorp.pinpoint.web.applicationmap.dao.InboundDao;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.LinkTimeWindowReducer;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.RowMapperFactory;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMapUtils;
import com.navercorp.pinpoint.web.mapper.RowMapReduceResultExtractor;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowDownSampler;
import com.navercorp.pinpoint.web.vo.Application;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author intr3p1d
 */
@Repository
public class HbaseInboundDao implements InboundDao {

    private static final int MAP_STATISTICS_INBOUND_SERVICE_GROUP_NUM_PARTITIONS = 32;
    private static final int SCAN_CACHE_SIZE = 40;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseColumnFamily.InboundServiceMap DESCRIPTOR = HbaseColumnFamily.MAP_STATISTICS_INBOUND_SERVICE_GROUP_COUNTER;

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;

    private final RowMapperFactory<LinkDataMap> mapperFactory;

    private final ApplicationMapScanFactory scanFactory;

    private final RowKeyDistributorByHashPrefix rowKeyDistributor;


    public HbaseInboundDao(
            HbaseOperations hbaseTemplate,
            TableNameProvider tableNameProvider,
            RowMapperFactory<LinkDataMap> mapperFactory,
            ApplicationMapScanFactory scanFactory,
            RowKeyDistributorByHashPrefix rowKeyDistributor
    ) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.mapperFactory = Objects.requireNonNull(mapperFactory, "serviceGroupInboundMapper");
        this.scanFactory = Objects.requireNonNull(scanFactory, "scanFactory");
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributorByHashPrefix");
    }

    @Override
    public LinkDataMap selectInbound(Application destApplication, Range range, boolean timeAggregated) {
        Objects.requireNonNull(destApplication, "destApplication");
        Objects.requireNonNull(range, "range");

        final TimeWindow timeWindow = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);

        TimeWindowFunction mapperWindow = newTimeWindow(timeAggregated);
        RowMapper<LinkDataMap> rowMapper = this.mapperFactory.newMapper(mapperWindow);
        ResultsExtractor<LinkDataMap> resultsExtractor = new RowMapReduceResultExtractor<>(
                rowMapper, new LinkTimeWindowReducer(timeWindow)
        );


        final Scan scan = scanFactory.createScan("MapInboundScan", destApplication, range, DESCRIPTOR.getName());

        return selectInLink(scan, DESCRIPTOR.getTable(), resultsExtractor, MAP_STATISTICS_INBOUND_SERVICE_GROUP_NUM_PARTITIONS);
    }

    private TimeWindowFunction newTimeWindow(boolean timeAggregated) {
        if (timeAggregated) {
            return TimeWindowFunction.ALL_IN_ONE;
        }
        return TimeWindowFunction.identity();
    }

    private LinkDataMap selectInLink(Scan scan, HbaseTable table, ResultsExtractor<LinkDataMap> resultExtractor, int parallel) {
        TableName callerTableName = tableNameProvider.getTableName(table);
        LinkDataMap linkDataMap = hbaseTemplate.findParallel(callerTableName, scan, rowKeyDistributor, resultExtractor, parallel);
        logger.debug("{} {} data: {}", LinkDirection.IN_LINK, callerTableName.getNameAsString(), linkDataMap);
        if (LinkDataMapUtils.hasLength(linkDataMap)) {
            return linkDataMap;
        }
        return new LinkDataMap();
    }
}