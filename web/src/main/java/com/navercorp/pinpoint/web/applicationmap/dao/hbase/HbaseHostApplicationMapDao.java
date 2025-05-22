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

package com.navercorp.pinpoint.web.applicationmap.dao.hbase;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.applicationmap.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.applicationmap.map.AcceptApplication;
import com.navercorp.pinpoint.web.vo.Application;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * 
 * @author netspider
 * @author emeroad
 * 
 */
@Repository
public class HbaseHostApplicationMapDao implements HostApplicationMapDao {

    private static final int HOST_APPLICATION_MAP_VER2_NUM_PARTITIONS = 4;

    private final Logger logger = LogManager.getLogger(this.getClass());
    private int scanCacheSize = 32;

    private final HbaseOperations hbaseOperations;

    private final TableNameProvider tableNameProvider;

    private final ResultsExtractor<Set<AcceptApplication>> hostApplicationResultExtractor;

    private final TimeSlot timeSlot;

    private final AbstractRowKeyDistributor acceptApplicationRowKeyDistributor;

    public HbaseHostApplicationMapDao(HbaseOperations hbaseOperations,
                                      TableNameProvider tableNameProvider,
                                      ResultsExtractor<Set<AcceptApplication>> hostApplicationResultExtractor,
                                      TimeSlot timeSlot,
                                      AbstractRowKeyDistributor acceptApplicationRowKeyDistributor) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.hostApplicationResultExtractor = Objects.requireNonNull(hostApplicationResultExtractor, "hostApplicationResultExtractor");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");
        this.acceptApplicationRowKeyDistributor = Objects.requireNonNull(acceptApplicationRowKeyDistributor, "acceptApplicationRowKeyDistributor");
    }


    @Override
    public Set<AcceptApplication> findAcceptApplicationName(Application fromApplication, Range range) {
        Objects.requireNonNull(fromApplication, "fromApplication");
        final Scan scan = createScan(fromApplication, range);

        TableName hostApplicationMapTableName = tableNameProvider.getTableName(HbaseTable.HOST_APPLICATION_MAP_VER2);
        final Set<AcceptApplication> result = hbaseOperations.findParallel(hostApplicationMapTableName, scan, acceptApplicationRowKeyDistributor, hostApplicationResultExtractor, HOST_APPLICATION_MAP_VER2_NUM_PARTITIONS);
        if (CollectionUtils.isNotEmpty(result)) {
            logger.debug("findAcceptApplicationName result:{}", result);
            return result;
        } else {
            return Collections.emptySet();
        }
    }




    private Scan createScan(Application parentApplication, Range range) {
        Objects.requireNonNull(parentApplication, "parentApplication");

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
        scan.withStartRow(startKey);
        scan.withStopRow(endKey);
        scan.setId("HostApplicationScan_Ver2");

        return scan;
    }

    private byte[] createKey(Application parentApplication, long time) {
        Buffer buffer = new AutomaticBuffer();
        buffer.putPadString(parentApplication.getName(), HbaseTableConstants.APPLICATION_NAME_MAX_LEN);
        buffer.putShort((short) parentApplication.getServiceTypeCode());
        buffer.putLong(time);
        return buffer.getBuffer();
    }



}
