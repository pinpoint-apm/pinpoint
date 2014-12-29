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
import com.navercorp.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.web.dao.MapResponseDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.RangeFactory;
import com.navercorp.pinpoint.web.vo.ResponseTime;

import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseMapResponseTimeDao implements MapResponseDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String tableName = HBaseTables.MAP_STATISTICS_SELF;

    private int scanCacheSize = 40;

    @Autowired
    private RowMapper<ResponseTime> responseTimeMapper;

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    private RangeFactory rangeFactory;


    @Override
    public List<ResponseTime> selectResponseTime(Application application, Range range) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("selectResponseTime applicationName:{}, {}", application, range);
        }
        Scan scan = createScan(application, range);
        List<ResponseTime> responseTimeList = hbaseOperations2.find(tableName, scan, responseTimeMapper);
        if (logger.isDebugEnabled()) {
            logger.debug("row:{}", responseTimeList.size());
            for (ResponseTime responseTime : responseTimeList) {
                logger.trace("responseTime:{}", responseTime);
            }
        }

        return responseTimeList;
    }

    private Scan createScan(Application application, Range range) {
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
        scan.addFamily(HBaseTables.MAP_STATISTICS_SELF_CF_COUNTER);
        scan.setId("ApplicationSelfScan");

        return scan;
    }


}
