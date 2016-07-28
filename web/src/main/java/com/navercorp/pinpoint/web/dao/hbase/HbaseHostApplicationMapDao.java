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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.util.TimeSlot;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.service.map.AcceptApplication;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;

import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author netspider
 * @author emeroad
 * 
 */
@Repository
public class HbaseHostApplicationMapDao implements HostApplicationMapDao {

    private static final int HOST_APPLICATION_MAP_VER2_NUM_PARTITIONS = 4;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private int scanCacheSize = 10;

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    @Qualifier("hostApplicationMapperVer2")
    private RowMapper<List<AcceptApplication>> hostApplicationMapperVer2;

    @Autowired
    private TimeSlot timeSlot;

    @Autowired
    @Qualifier("acceptApplicationRowKeyDistributor")
    private AbstractRowKeyDistributor acceptApplicationRowKeyDistributor;


    @Override
    public Set<AcceptApplication> findAcceptApplicationName(Application fromApplication, Range range) {
        if (fromApplication == null) {
            throw new NullPointerException("fromApplication must not be null");
        }
        final Scan scan = createScan(fromApplication, range);
        final List<List<AcceptApplication>> result = hbaseOperations2.findParallel(HBaseTables.HOST_APPLICATION_MAP_VER2, scan, acceptApplicationRowKeyDistributor, hostApplicationMapperVer2, HOST_APPLICATION_MAP_VER2_NUM_PARTITIONS);
        if (CollectionUtils.isNotEmpty(result)) {
            final Set<AcceptApplication> resultSet = new HashSet<>();
            for (List<AcceptApplication> resultList : result) {
                resultSet.addAll(resultList);
            }
            logger.debug("findAcceptApplicationName result:{}", resultSet);
            return resultSet;
        } else {
            return Collections.emptySet();
        }
    }




    private Scan createScan(Application parentApplication, Range range) {
        if (parentApplication == null) {
            throw new NullPointerException("parentApplication must not be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("scan parentApplication:{}, range:{}", parentApplication, range);
        }

        // TODO need common logic for creating scanner
        final long startTime = TimeUtils.reverseTimeMillis(timeSlot.getTimeSlot(range.getFrom()));
        final long endTime = TimeUtils.reverseTimeMillis(timeSlot.getTimeSlot(range.getTo()) + 1);
        // start key is replaced by end key because timestamp has been reversed
        final byte[] startKey = createKey(parentApplication, endTime);
        final byte[] endKey = createKey(parentApplication, startTime);

        Scan scan = new Scan();
        scan.setCaching(this.scanCacheSize);
        scan.setStartRow(startKey);
        scan.setStopRow(endKey);
        scan.setId("HostApplicationScan_Ver2");

        return scan;
    }

    private byte[] createKey(Application parentApplication, long time) {
        Buffer buffer = new AutomaticBuffer();
        buffer.putPadString(parentApplication.getName(), HBaseTables.APPLICATION_NAME_MAX_LEN);
        buffer.putShort(parentApplication.getServiceTypeCode());
        buffer.putLong(time);
        return buffer.getBuffer();
    }



}
