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
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowFunction;
import com.navercorp.pinpoint.web.applicationmap.dao.MapInLinkDao;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.LinkTimeWindowReducer;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.RowMapperFactory;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMapUtils;
import com.navercorp.pinpoint.web.mapper.RowMapReduceResultExtractor;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseMapInLinkDao implements MapInLinkDao {

    private static final int MAP_STATISTICS_CALLER_VER2_NUM_PARTITIONS = 32;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.MAP_STATISTICS_CALLER_VER2_COUNTER;

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;

    private final RowMapperFactory<LinkDataMap> inLinkMapperFactory;

    private final MapScanFactory scanFactory;

    private final RowKeyDistributorByHashPrefix rowKeyDistributor;


    public HbaseMapInLinkDao(
            HbaseOperations hbaseTemplate,
            TableNameProvider tableNameProvider,
            RowMapperFactory<LinkDataMap> inLinkMapperFactory,
            MapScanFactory scanFactory,
            RowKeyDistributorByHashPrefix rowKeyDistributor)  {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.inLinkMapperFactory = Objects.requireNonNull(inLinkMapperFactory, "inLinkMapperFactory");
        this.scanFactory = Objects.requireNonNull(scanFactory, "scanFactory");
        this.rowKeyDistributor = Objects.requireNonNull(rowKeyDistributor, "rowKeyDistributor");
    }

    @Override
    public LinkDataMap selectInLink(Application inApplication, TimeWindow timeWindow, boolean timeAggregated) {
        Objects.requireNonNull(inApplication, "inApplication");
        Objects.requireNonNull(timeWindow, "timeWindow");

        TimeWindowFunction mapperWindow = TimeWindowFunction.newTimeWindow(timeAggregated);
        RowMapper<LinkDataMap> rowMapper = this.inLinkMapperFactory.newMapper(mapperWindow);
        ResultsExtractor<LinkDataMap> resultExtractor = new RowMapReduceResultExtractor<>(rowMapper, new LinkTimeWindowReducer(timeWindow));

        final Scan scan = scanFactory.createScan("MapInLinkScan", inApplication, timeWindow.getWindowRange(), DESCRIPTOR.getName());

        final LinkDataMap linkDataMap = selectInLink(scan, DESCRIPTOR.getTable(), resultExtractor, MAP_STATISTICS_CALLER_VER2_NUM_PARTITIONS);
        if (logger.isDebugEnabled()) {
            logger.debug("selectInLink {} {}", inApplication, linkDataMap.getLinkDataSize());
        }
        return linkDataMap;
    }

    private LinkDataMap selectInLink(Scan scan, HbaseTable table, ResultsExtractor<LinkDataMap> resultExtractor, int parallel) {
        TableName inLinkTableName = tableNameProvider.getTableName(table);
        LinkDataMap linkDataMap = hbaseTemplate.findParallel(inLinkTableName, scan, rowKeyDistributor, resultExtractor, parallel);
        if (logger.isDebugEnabled()) {
            logger.debug("IN_LINK {} data: {}", inLinkTableName.getNameAsString(), linkDataMap);
        }
        if (LinkDataMapUtils.hasLength(linkDataMap)) {
            return linkDataMap;
        }
        return new LinkDataMap();
    }

}