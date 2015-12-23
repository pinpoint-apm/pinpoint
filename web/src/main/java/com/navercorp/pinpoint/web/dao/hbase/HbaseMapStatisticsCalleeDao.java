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
import com.navercorp.pinpoint.web.dao.MapStatisticsCalleeDao;
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
public class HbaseMapStatisticsCalleeDao implements MapStatisticsCalleeDao {

    private static final int MAP_STATISTICS_CALLER_VER2_NUM_PARTITIONS = 32;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private int scanCacheSize = 40;
    private boolean backwardCompatibility = false;
    private boolean tableExists = false;

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    HBaseAdminTemplate hBaseAdminTemplate;

    @Autowired
    @Qualifier("mapStatisticsCalleeMapperBackwardCompatibility")
    private RowMapper<LinkDataMap> mapStatisticsCalleeMapperBackwardCompatibility;

    @Autowired
    @Qualifier("mapStatisticsCalleeMapper")
    private RowMapper<LinkDataMap> mapStatisticsCalleeMapper;

    @Autowired
    private RangeFactory rangeFactory;

    @Autowired
    @Qualifier("statisticsCalleeRowKeyDistributor")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    @PostConstruct
    public void init() {
        tableExists = hBaseAdminTemplate.tableExists(HBaseTables.MAP_STATISTICS_CALLER_VER2);
        if (!tableExists) {
            logger.warn("Please create '{}' table.", HBaseTables.MAP_STATISTICS_CALLER_VER2);
        }

        backwardCompatibility = hBaseAdminTemplate.tableExists(HBaseTables.MAP_STATISTICS_CALLER);
        if (backwardCompatibility) {
            logger.warn("'{}' table exists. Recommend that only use '{}' table.", HBaseTables.MAP_STATISTICS_CALLER, HBaseTables.MAP_STATISTICS_CALLER_VER2);
        }

        if(!tableExists && !backwardCompatibility) {
            throw new RuntimeException("Please check for '" + HBaseTables.MAP_STATISTICS_CALLER_VER2 + "' table in HBase. Need to create '" + HBaseTables.MAP_STATISTICS_CALLER_VER2 + "' table.");
        }
    }

    @Override
    public LinkDataMap selectCallee(Application calleeApplication, Range range) {
        if (calleeApplication == null) {
            throw new NullPointerException("calleeApplication must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        final TimeWindow timeWindow = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);
        if(tableExists) {
            // find distributed key - ver2.
            final Scan scan = createScan(calleeApplication, range, HBaseTables.MAP_STATISTICS_CALLER_VER2_CF_COUNTER);
            ResultsExtractor<LinkDataMap> resultExtractor = new RowMapReduceResultExtractor<>(mapStatisticsCalleeMapper, new MapStatisticsTimeWindowReducer(timeWindow));
            LinkDataMap linkDataMap = hbaseOperations2.findParallel(HBaseTables.MAP_STATISTICS_CALLER_VER2, scan, rowKeyDistributorByHashPrefix, resultExtractor, MAP_STATISTICS_CALLER_VER2_NUM_PARTITIONS);
            logger.debug("Callee data. {}, {}", linkDataMap, range);
            if (linkDataMap != null && linkDataMap.size() > 0) {
                return linkDataMap;
            }
        }

        if(backwardCompatibility) {
            // backward compatibility - non distributed - ver1.
            final Scan scan = createScan(calleeApplication, range, HBaseTables.MAP_STATISTICS_CALLER_CF_COUNTER);
            ResultsExtractor<LinkDataMap> resultExtractor = new RowMapReduceResultExtractor<>(mapStatisticsCalleeMapperBackwardCompatibility, new MapStatisticsTimeWindowReducer(timeWindow));
            LinkDataMap linkDataMap = hbaseOperations2.find(HBaseTables.MAP_STATISTICS_CALLER, scan, resultExtractor);
            logger.debug("Callee data. {}, {}", linkDataMap, range);
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
    public List<LinkDataMap> selectCalleeStatistics(Application callerApplication, Application calleeApplication, Range range) {
        if (logger.isDebugEnabled()) {
            logger.debug("selectCalleeStatistics. {}, {}, {}", callerApplication, calleeApplication, range);
        }
        Scan scan = createScan(calleeApplication, range, HBaseTables.MAP_STATISTICS_CALLER_CF_COUNTER);
        final LinkFilter filter = new DefaultLinkFilter(callerApplication, calleeApplication);
        RowMapper<LinkDataMap> mapper = new MapStatisticsCalleeMapperBackwardCompatibility(filter);
        return hbaseOperations2.find(HBaseTables.MAP_STATISTICS_CALLER, scan, mapper);
    }

    private Scan createScan(Application application, Range range, byte[] family) {
        range = rangeFactory.createStatisticsRange(range);

        if (logger.isDebugEnabled()) {
            logger.debug("scan time:{} ", range.prettyToString());
        }

        // start key is replaced by end key because timestamp has been reversed
        byte[] startKey = ApplicationMapStatisticsUtils.makeRowKey(application.getName(), application.getServiceTypeCode(), range.getTo());
        byte[] endKey = ApplicationMapStatisticsUtils.makeRowKey(application.getName(), application.getServiceTypeCode(), range.getFrom());

        Scan scan = new Scan();
        scan.setCaching(this.scanCacheSize);
        scan.setStartRow(startKey);
        scan.setStopRow(endKey);
        scan.addFamily(family);
        scan.setId("ApplicationStatisticsScan");

        return scan;
    }
}