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

import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.web.dao.MapResponseDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.RangeFactory;
import com.navercorp.pinpoint.web.vo.ResponseTime;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseMapResponseTimeDao implements MapResponseDao {

    private static final int MAP_STATISTICS_SELF_VER2_NUM_PARTITIONS = 8;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int scanCacheSize = 40;

    @Autowired
    @Qualifier("responseTimeMapper")
    private RowMapper<ResponseTime> responseTimeMapper;


    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    private RangeFactory rangeFactory;

    @Autowired
    @Qualifier("statisticsSelfRowKeyDistributor")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;


    @Override
    public List<ResponseTime> selectResponseTime(Application application, Range range) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("selectResponseTime applicationName:{}, {}", application, range);
        }

        Scan scan = createScan(application, range, HBaseTables.MAP_STATISTICS_SELF_VER2_CF_COUNTER);

        List<ResponseTime> responseTimeList = hbaseOperations2.findParallel(HBaseTables.MAP_STATISTICS_SELF_VER2, scan, rowKeyDistributorByHashPrefix, responseTimeMapper, MAP_STATISTICS_SELF_VER2_NUM_PARTITIONS);
        if (logger.isDebugEnabled()) {
            logger.debug("Self data {}", responseTimeList);
        }

        if (!responseTimeList.isEmpty()) {
            return responseTimeList;
        }

        return new ArrayList<>();
    }

    private Scan createScan(Application application, Range range, byte[] family) {
        range = rangeFactory.createStatisticsRange(range);
        if (logger.isDebugEnabled()) {
            logger.debug("scan time:{} ", range.prettyToString());
        }

        // start key is replaced by end key because timestamp has been reversed
        byte[] startKey = ApplicationMapStatisticsUtils.makeRowKey(application.getName(), application.getServiceTypeCode(), range.getTo());
        byte[] endKey = ApplicationMapStatisticsUtils.makeRowKey(application.getName(), application.getServiceTypeCode(), range.getFrom());

        final Scan scan = new Scan();
        scan.setCaching(this.scanCacheSize);
        scan.setStartRow(startKey);
        scan.setStopRow(endKey);
        scan.addFamily(family);
        scan.setId("ApplicationSelfScan");

        return scan;
    }
}