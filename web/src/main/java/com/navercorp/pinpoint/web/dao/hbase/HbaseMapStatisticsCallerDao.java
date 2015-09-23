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

import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author netspider
 * @author emeroad
 * 
 */
@Repository
public class HbaseMapStatisticsCallerDao implements MapStatisticsCallerDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private int scanCacheSize = 40;

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    @Qualifier("mapStatisticsCallerMapper")
    private RowMapper<LinkDataMap> mapStatisticsCallerMapper;

    @Autowired
    private RangeFactory rangeFactory;

    @Override
    public LinkDataMap selectCaller(Application callerApplication, Range range) {
        Scan scan = createScan(callerApplication, range);
        final TimeWindow timeWindow = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);
        final ResultsExtractor<LinkDataMap> resultExtractor = new RowMapReduceResultExtractor<LinkDataMap>(mapStatisticsCallerMapper, new MapStatisticsTimeWindowReducer(timeWindow));
        final LinkDataMap foundList = hbaseOperations2.find(HBaseTables.MAP_STATISTICS_CALLEE, scan, resultExtractor);
        logger.debug("Caller data. {}, {}", foundList, range);

        if (foundList == null) {
            logger.debug("There's no caller data. {}, {}", callerApplication, range);
            return new LinkDataMap();
        }

        return foundList;
    }

    private LinkDataMap merge(List<LinkDataMap> foundList) {
        final LinkDataMap result = new LinkDataMap();
        for (LinkDataMap foundData : foundList) {
            result.addLinkDataMap(foundData);
        }
        return result;
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
        RowMapper<LinkDataMap> mapper = new MapStatisticsCallerMapper(filter);
        return hbaseOperations2.find(HBaseTables.MAP_STATISTICS_CALLEE, scan, mapper);
    }

    private Scan createScan(Application application, Range range) {
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
        scan.addFamily(HBaseTables.MAP_STATISTICS_CALLEE_CF_COUNTER);
        scan.addFamily(HBaseTables.MAP_STATISTICS_CALLEE_CF_VER2_COUNTER);
        scan.setId("ApplicationStatisticsScan");

        return scan;
    }
}
