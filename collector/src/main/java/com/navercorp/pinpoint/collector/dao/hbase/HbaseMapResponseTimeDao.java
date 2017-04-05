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

import com.google.common.util.concurrent.AtomicLongMap;
import com.navercorp.pinpoint.collector.dao.MapResponseTimeDao;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.*;
import com.navercorp.pinpoint.collector.util.AtomicLongMapUtils;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.common.util.TimeSlot;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.client.Increment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.*;

/**
 * Save response time data of WAS
 * 
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 */
@Repository
public class HbaseMapResponseTimeDao implements MapResponseTimeDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    private AcceptedTimeService acceptedTimeService;

    @Autowired
    private TimeSlot timeSlot;

    @Autowired
    @Qualifier("selfMerge")
    private RowKeyMerge rowKeyMerge;

    @Autowired
    @Qualifier("statisticsSelfRowKeyDistributor")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    private final boolean useBulk;

    private final AtomicLongMap<RowInfo> counter = AtomicLongMap.create();

    public HbaseMapResponseTimeDao() {
        this(true);
    }

    public HbaseMapResponseTimeDao(boolean useBulk) {
        this.useBulk = useBulk;
    }

    @Override
    public void received(String applicationName, ServiceType applicationServiceType, String agentId, int elapsed, boolean isError) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("[Received] {} ({})[{}]", applicationName, applicationServiceType, agentId);
        }

        // make row key. rowkey is me
        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        final long rowTimeSlot = timeSlot.getTimeSlot(acceptedTime);
        final RowKey selfRowKey = new CallRowKey(applicationName, applicationServiceType.getCode(), rowTimeSlot);

        final short slotNumber = ApplicationMapStatisticsUtils.getSlotNumber(applicationServiceType, elapsed, isError);
        final ColumnName selfColumnName = new ResponseColumnName(agentId, slotNumber);
        if (useBulk) {
            RowInfo rowInfo = new DefaultRowInfo(selfRowKey, selfColumnName);
            this.counter.incrementAndGet(rowInfo);
        } else {
            final byte[] rowKey = getDistributedKey(selfRowKey.getRowKey());
            // column name is the name of caller app.
            byte[] columnName = selfColumnName.getColumnName();
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
        hbaseTemplate.incrementColumnValue(MAP_STATISTICS_SELF_VER2, rowKey, MAP_STATISTICS_SELF_VER2_CF_COUNTER, columnName, increment);
    }


    @Override
    public void flushAll() {
        if (!useBulk) {
            throw new IllegalStateException("useBulk is " + useBulk);
        }

        // update statistics by rowkey and column for now. need to update it by rowkey later.
        final Map<RowInfo, Long> remove = AtomicLongMapUtils.remove(this.counter);

        final List<Increment> merge = rowKeyMerge.createBulkIncrement(remove, rowKeyDistributorByHashPrefix);
        if (merge.isEmpty()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("flush {} Increment:{}", this.getClass().getSimpleName(), merge.size());
        }
        hbaseTemplate.increment(MAP_STATISTICS_SELF_VER2, merge);
    }

    private byte[] getDistributedKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getDistributedKey(rowKey);
    }
}
