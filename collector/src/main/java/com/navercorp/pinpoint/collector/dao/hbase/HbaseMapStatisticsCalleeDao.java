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

package com.navercorp.pinpoint.collector.dao.hbase;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.*;

import com.google.common.util.concurrent.AtomicLongMap;
import com.navercorp.pinpoint.collector.dao.MapStatisticsCalleeDao;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.*;
import com.navercorp.pinpoint.collector.util.AtomicLongMapUtils;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.common.util.TimeSlot;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.client.Increment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Update statistics of callee node
 * 
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseMapStatisticsCalleeDao implements MapStatisticsCalleeDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    private AcceptedTimeService acceptedTimeService;

    @Autowired
    private TimeSlot timeSlot;

    @Autowired
    @Qualifier("calleeMerge")
    private RowKeyMerge rowKeyMerge;

    @Autowired
    @Qualifier("statisticsCalleeRowKeyDistributor")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    private final boolean useBulk;

    private final AtomicLongMap<RowInfo> counter = AtomicLongMap.create();

    public HbaseMapStatisticsCalleeDao() {
        this(true);
    }

    public HbaseMapStatisticsCalleeDao(boolean useBulk) {
        this.useBulk = useBulk;
    }


    @Override
    public void update(String calleeApplicationName, ServiceType calleeServiceType, String callerApplicationName, ServiceType callerServiceType, String callerHost, int elapsed, boolean isError) {
        if (callerApplicationName == null) {
            throw new NullPointerException("callerApplicationName must not be null");
        }
        if (calleeApplicationName == null) {
            throw new NullPointerException("calleeApplicationName must not be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("[Callee] {} ({}) <- {} ({})[{}]",
                    calleeApplicationName, calleeServiceType, callerApplicationName, callerServiceType, callerHost);
        }

        // there may be no endpoint in case of httpclient
        callerHost = StringUtils.defaultString(callerHost);

        // make row key. rowkey is me
        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        final long rowTimeSlot = timeSlot.getTimeSlot(acceptedTime);
        final RowKey calleeRowKey = new CallRowKey(calleeApplicationName, calleeServiceType.getCode(), rowTimeSlot);

        final short callerSlotNumber = ApplicationMapStatisticsUtils.getSlotNumber(calleeServiceType, elapsed, isError);
        final ColumnName callerColumnName = new CallerColumnName(callerServiceType.getCode(), callerApplicationName, callerHost, callerSlotNumber);

        if (useBulk) {
            RowInfo rowInfo = new DefaultRowInfo(calleeRowKey, callerColumnName);
            counter.incrementAndGet(rowInfo);
        } else {
            final byte[] rowKey = getDistributedKey(calleeRowKey.getRowKey());

            // column name is the name of caller app.
            byte[] columnName = callerColumnName.getColumnName();
            increment(rowKey, columnName, 1L);
        }
    }

    private void increment(byte[] rowKey, byte[] columnName, long increment) {
        if (rowKey == null) {
            throw new NullPointerException("rowKey must not be null");
        }
        if (columnName == null) {
            throw new NullPointerException("columnName must not be null");
        }
        hbaseTemplate.incrementColumnValue(MAP_STATISTICS_CALLER_VER2, rowKey, MAP_STATISTICS_CALLER_VER2_CF_COUNTER, columnName, increment);
    }

    @Override
    public void flushAll() {
        if (!useBulk) {
            throw new IllegalStateException();
        }

        final Map<RowInfo, Long> remove = AtomicLongMapUtils.remove(this.counter);

        final List<Increment> merge = rowKeyMerge.createBulkIncrement(remove, rowKeyDistributorByHashPrefix);
        if (merge.isEmpty()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("flush {} Increment:{}", this.getClass().getSimpleName(), merge.size());
        }
        hbaseTemplate.increment(MAP_STATISTICS_CALLER_VER2, merge);

    }

    private byte[] getDistributedKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getDistributedKey(rowKey);
    }
}
