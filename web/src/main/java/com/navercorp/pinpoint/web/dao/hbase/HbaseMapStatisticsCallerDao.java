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

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableDescriptor;
import com.navercorp.pinpoint.common.profiler.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.dao.MapStatisticsCallerDao;
import com.navercorp.pinpoint.web.mapper.MapStatisticsTimeWindowReducer;
import com.navercorp.pinpoint.web.mapper.RowMapReduceResultExtractor;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowDownSampler;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.RangeFactory;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private static final int SCAN_CACHE_SIZE = 40;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseOperations2 hbaseTemplate;

    private final RowMapper<LinkDataMap> mapStatisticsCallerMapper;

    private final RangeFactory rangeFactory;

    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    private TableDescriptor<HbaseColumnFamily.CalleeStatMap> descriptor;

    public HbaseMapStatisticsCallerDao(
            HbaseOperations2 hbaseTemplate,
            TableDescriptor<HbaseColumnFamily.CalleeStatMap> descriptor, @Qualifier("mapStatisticsCallerMapper") RowMapper<LinkDataMap> mapStatisticsCallerMapper,
            RangeFactory rangeFactory,
            @Qualifier("statisticsCallerRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.mapStatisticsCallerMapper = Objects.requireNonNull(mapStatisticsCallerMapper, "mapStatisticsCallerMapper");
        this.rangeFactory = Objects.requireNonNull(rangeFactory, "rangeFactory");
        this.rowKeyDistributorByHashPrefix = Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
    }

    @Override
    public LinkDataMap selectCaller(Application callerApplication, Range range) {
        Objects.requireNonNull(callerApplication, "callerApplication");
        Objects.requireNonNull(range, "range");

        final TimeWindow timeWindow = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);
        // find distributed key.
        final Scan scan = createScan(callerApplication, range, descriptor.getColumnFamilyName());
        ResultsExtractor<LinkDataMap> resultExtractor = new RowMapReduceResultExtractor<>(mapStatisticsCallerMapper, new MapStatisticsTimeWindowReducer(timeWindow));

        TableName mapStatisticsCalleeTableName = descriptor.getTableName();
        LinkDataMap linkDataMap = this.hbaseTemplate.findParallel(mapStatisticsCalleeTableName, scan, rowKeyDistributorByHashPrefix, resultExtractor, MAP_STATISTICS_CALLEE_VER2_NUM_PARTITIONS);
        logger.debug("Caller data. {}, {}", linkDataMap, range);
        if (linkDataMap != null && linkDataMap.size() > 0) {
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
        byte[] startKey = ApplicationMapStatisticsUtils.makeRowKey(application.getName(), application.getServiceTypeCode(), range.getTo());
        byte[] endKey = ApplicationMapStatisticsUtils.makeRowKey(application.getName(), application.getServiceTypeCode(), range.getFrom());

        Scan scan = new Scan();
        scan.setCaching(SCAN_CACHE_SIZE);
        scan.setStartRow(startKey);
        scan.setStopRow(endKey);
        for(byte[] family : familyArgs) {
            scan.addFamily(family);
        }
        scan.setId("ApplicationStatisticsScan");

        return scan;
    }

}