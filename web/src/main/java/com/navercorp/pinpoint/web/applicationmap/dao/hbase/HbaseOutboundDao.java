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
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.applicationmap.util.ApplicationMapUtils;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.dao.OutboundDao;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.MapStatisticsTimeWindowReducer;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMapUtils;
import com.navercorp.pinpoint.web.mapper.RowMapReduceResultExtractor;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowDownSampler;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.RangeFactory;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author intr3p1d
 */
@Repository
public class HbaseOutboundDao implements OutboundDao {
    private static final int MAP_STATISTICS_OUTBOUND_SERVICE_GROUP_NUM_PARTITIONS = 32;
    private static final int SCAN_CACHE_SIZE = 40;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseColumnFamily.OutboundServiceMap DESCRIPTOR = HbaseColumnFamily.MAP_STATISTICS_OUTBOUND_SERVICE_GROUP_COUNTER;

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<LinkDataMap> applicationMapOutboundMapper;
    private final RowMapper<LinkDataMap> applicationMapOutboundTimeAggregatedMapper;

    private final RangeFactory rangeFactory;

    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;


    public HbaseOutboundDao(
            @Qualifier("mapHbaseTemplate") HbaseOperations hbaseTemplate,
            TableNameProvider tableNameProvider,
            @Qualifier("applicationMapOutboundMapper") RowMapper<LinkDataMap> applicationMapOutboundMapper,
            @Qualifier("applicationMapOutboundTimeAggregatedMapper") RowMapper<LinkDataMap> applicationMapOutboundTimeAggregatedMapper,
            RangeFactory rangeFactory,
            @Qualifier("applicationMapOutboundRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix
    ) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.applicationMapOutboundMapper = Objects.requireNonNull(applicationMapOutboundMapper, "applicationMapOutboundMapper");
        this.applicationMapOutboundTimeAggregatedMapper = Objects.requireNonNull(applicationMapOutboundTimeAggregatedMapper, "mapStatisticsTimeAggregatedCallerMapper");
        this.rangeFactory = Objects.requireNonNull(rangeFactory, "rangeFactory");
        this.rowKeyDistributorByHashPrefix = Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
    }


    @Override
    public LinkDataMap selectOutboud(Application callerApplication, Range range, boolean timeAggregated) {        Objects.requireNonNull(callerApplication, "callerApplication");
        Objects.requireNonNull(callerApplication, "callerApplication");
        Objects.requireNonNull(range, "range");

        final TimeWindow timeWindow = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);
        // find distributed key.
        final Scan scan = createScan(callerApplication, range, DESCRIPTOR.getName());

        ResultsExtractor<LinkDataMap> resultsExtractor;
        if (timeAggregated) {
            resultsExtractor = new RowMapReduceResultExtractor<>(applicationMapOutboundTimeAggregatedMapper, new MapStatisticsTimeWindowReducer(timeWindow));
        } else {
            resultsExtractor = new RowMapReduceResultExtractor<>(applicationMapOutboundMapper, new MapStatisticsTimeWindowReducer(timeWindow));
        }

        TableName applicationMapOutboundTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        LinkDataMap linkDataMap = this.hbaseTemplate.findParallel(applicationMapOutboundTableName, scan, rowKeyDistributorByHashPrefix, resultsExtractor, MAP_STATISTICS_OUTBOUND_SERVICE_GROUP_NUM_PARTITIONS);
        logger.debug("tableInfo({}). {} data. {}, {} : ", applicationMapOutboundTableName.getNameAsString(), LinkDirection.OUT_LINK, linkDataMap, range );

        if (LinkDataMapUtils.hasLength(linkDataMap)) {
            return linkDataMap;
        }

        return new LinkDataMap();
    }

    private Scan createScan(Application application, Range range, byte[]... familyArgs) {

        range = rangeFactory.createStatisticsRange(range);

        if (logger.isDebugEnabled()) {
            logger.debug("scan Time:{}", range.prettyToString());
        }

        // start key is replaced by end key because timestamp has been reversed
        byte[] startKey = ApplicationMapUtils.makeRowKey("default", application.getName(), application.getServiceTypeCode(), range.getTo());
        byte[] endKey = ApplicationMapUtils.makeRowKey("default", application.getName(), application.getServiceTypeCode(), range.getFrom());

        Scan scan = new Scan();
        scan.setCaching(SCAN_CACHE_SIZE);
        scan.withStartRow(startKey);
        scan.withStopRow(endKey);
        for (byte[] family : familyArgs) {
            scan.addFamily(family);
        }
        scan.setId("ServiceGroupMapScan");

        return scan;


    }
}
