/*
 * Copyright 2014 NAVER Corp.
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

import java.util.*;

import com.navercorp.pinpoint.common.hbase.HBaseAdminTemplate;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.dao.MapStatisticsCallerDao;
import com.navercorp.pinpoint.web.mapper.*;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowDownSampler;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.RangeFactory;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

/**
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseMapStatisticsCallerDao implements MapStatisticsCallerDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private int scanCacheSize = 40;
    private boolean backwardCompatibility = false;
    private boolean tableExists = false;

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    HBaseAdminTemplate hBaseAdminTemplate;

    @Autowired
    @Qualifier("mapStatisticsCallerMapperBackwardCompatibility")
    private RowMapper<LinkDataMap> mapStatisticsCallerMapperBackwardCompatibility;

    @Autowired
    @Qualifier("mapStatisticsCallerMapper")
    private RowMapper<LinkDataMap> mapStatisticsCallerMapper;

    @Autowired
    private RangeFactory rangeFactory;

    @Autowired
    @Qualifier("statisticsCallerRowKeyDistributor")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    @PostConstruct
    public void init() {
        tableExists = hBaseAdminTemplate.tableExists(HBaseTables.MAP_STATISTICS_CALLEE_VER2);
        if (!tableExists) {
            logger.warn("Please create '{}' table.", HBaseTables.MAP_STATISTICS_CALLEE_VER2);
        }

        backwardCompatibility = hBaseAdminTemplate.tableExists(HBaseTables.MAP_STATISTICS_CALLEE);
        if (backwardCompatibility) {
            logger.warn("'{}' table exists. Recommend that only use '{}' table.", HBaseTables.MAP_STATISTICS_CALLEE, HBaseTables.MAP_STATISTICS_CALLEE_VER2);
        }

        if (!tableExists && !backwardCompatibility) {
            throw new RuntimeException("Please check for '" + HBaseTables.MAP_STATISTICS_CALLEE_VER2 + "' table in HBase. Need to create '" + HBaseTables.MAP_STATISTICS_CALLEE_VER2 + "' table.");
        }
    }

    @Override
    public LinkDataMap selectCaller(Application callerApplication, Range range) {
        if (callerApplication == null) {
            throw new NullPointerException("callerApplication must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }

        final TimeWindow timeWindow = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);
        if(tableExists) {
            // find distributed key.
            final Scan scan = createScan(callerApplication, range, HBaseTables.MAP_STATISTICS_CALLEE_VER2_CF_COUNTER);
            ResultsExtractor<LinkDataMap> resultExtractor = new RowMapReduceResultExtractor<LinkDataMap>(mapStatisticsCallerMapper, new MapStatisticsTimeWindowReducer(timeWindow));
            LinkDataMap linkDataMap = hbaseOperations2.find(HBaseTables.MAP_STATISTICS_CALLEE_VER2, scan, rowKeyDistributorByHashPrefix, resultExtractor);
            logger.debug("Caller data. {}, {}", linkDataMap, range);
            if(linkDataMap != null && linkDataMap.size() > 0) {
                return linkDataMap;
            }
        }

        if (backwardCompatibility) {
            // backward compatibility - non distributed.
            final Scan scan = createScan(callerApplication, range, HBaseTables.MAP_STATISTICS_CALLEE_CF_COUNTER, HBaseTables.MAP_STATISTICS_CALLEE_CF_VER2_COUNTER);
            ResultsExtractor<LinkDataMap> resultExtractor = new RowMapReduceResultExtractor<LinkDataMap>(mapStatisticsCallerMapperBackwardCompatibility, new MapStatisticsTimeWindowReducer(timeWindow));
            LinkDataMap linkDataMap = hbaseOperations2.find(HBaseTables.MAP_STATISTICS_CALLEE, scan, resultExtractor);
            logger.debug("Caller data. {}, {}", linkDataMap, range);
            return linkDataMap != null ? linkDataMap : new LinkDataMap();
        } else {
            return new LinkDataMap();
        }
    }

    /**
     * statistics information used when a link between nodes is clicked at the server map
     *
     * @return <pre>
     * list [
     *     map {
     *         key = timestamp
     *         value = map {
     *             key = histogram slot
     *             value = count
     *         }
     *     }
     * ]
     * </pre>
     */
    @Override
    @Deprecated
    public List<LinkDataMap> selectCallerStatistics(Application callerApplication, Application calleeApplication, Range range) {
        if (logger.isDebugEnabled()) {
            logger.debug("selectCallerStatistics. {}, {}, {}", callerApplication, calleeApplication, range);
        }
        Scan scan = createScan(callerApplication, range);

        final LinkFilter filter = new DefaultLinkFilter(callerApplication, calleeApplication);
        RowMapper<LinkDataMap> mapper = new MapStatisticsCallerMapperBackwardCompatibility(filter);
        return hbaseOperations2.find(HBaseTables.MAP_STATISTICS_CALLEE, scan, mapper);
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
        scan.setCaching(this.scanCacheSize);
        scan.setStartRow(startKey);
        scan.setStopRow(endKey);
        for(byte[] family : familyArgs) {
            scan.addFamily(family);
        }
        scan.setId("ApplicationStatisticsScan");

        return scan;
    }
}