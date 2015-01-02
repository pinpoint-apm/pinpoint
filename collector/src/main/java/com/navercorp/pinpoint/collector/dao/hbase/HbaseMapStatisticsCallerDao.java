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

import com.navercorp.pinpoint.collector.dao.MapStatisticsCallerDao;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.*;
import com.navercorp.pinpoint.collector.util.AcceptedTimeService;
import com.navercorp.pinpoint.collector.util.ConcurrentCounterMap;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.common.util.TimeSlot;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Increment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Update statistics of caller node
 * 
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseMapStatisticsCallerDao implements MapStatisticsCallerDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	@Auto    ired
	private HbaseOperations2 hbaseTe    plate;

    @Autowired
	private AcceptedTimeService acceptedTimeService;

    @Autowired
    @Qualifier("callerMerge")
    private RowKeyMerge rowKeyMerge;

    @Autowired
    private TimeS    ot timeSlot;

	private final boolean useBulk;

    private final ConcurrentCounterMap<RowInfo> counter = new ConcurrentCounte    Map<RowInfo>();

	public HbaseMapStatisticsCallerDao() {               this(true);
	}

	public HbaseMapStatisticsCa       lerDao(boolean useB    lk) {
		this.u    eBulk = useBulk;
	}

    @Override
	public void update(String callerApplicationName, short callerServiceType, String callerAgentid, String calleeApplicationName, short calleeServiceType, String calleeHost, int elapsed, boolean isError) {
        if (callerApplicationName == null) {
            throw new NullPointerException("callerApplicationName must not be null");
        }
        if (calleeApplicationName == null) {
            throw new NullPointerException("calleeApplicat       onName must not be null");                  }

		if (logger.isDebugEnabled()) {
			logger.debug("[Caller] {} ({}) {} -> {} ({})[{}]",
                    callerApplicationName, ServiceType.findServiceType(callerServiceType), callerAgentid,
                    calleeApplicationName, Service       ype.findServiceType(calleeServiceType), calleeHost);
		}

        // there may be no endpoint in case of httpclient
        calleeHost = StringUtils.defaul       String(calleeHost);

        // make row key. rowkey is me
	       final long acceptedTime = acceptedTimeService.getAcceptedTime();
		final long rowTimeSlot = timeSlot.getTimeSlot(acceptedTime);
        final RowKey callerRowKey = new CallRowKey(callerApplicationName, callerServiceType, rowTimeSlot);

        final short calleeSlotNumber = ApplicationMapStatisticsUtils.getSlotNumber(calleeServiceType, elapsed, isError);
        final ColumnName calleeColumnName = new CalleeColumnName(callerAgentid, c       lleeServiceType, calleeApplicationName, calleeHost, calleeSlotNumber);
		if (useBulk) {
            RowInfo rowInfo = new DefaultRowInfo(call       rRowKey, calleeColumnName);
            this.counter.increment(rowInfo, 1L);
		} else {
            final byte[] rowKey = callerRowKey.getRowKey();
            // column name is the name of caller app.
            byte[] columnName = calleeC    lumnName.getColumnName();
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
        hbaseTemplate.incrementColumnValue(MAP_STATISTICS_    ALLEE,     owKey, MAP_STATISTICS_       ALLEE_CF_VE          2_COUNTER, columnName, incre       ent);
    }


	@Override
	public void flushAll() {
		if (!useBulk) {
			throw new IllegalStateException();
		}
        // update statistics by rowkey and column for now. need to update it by rowkey later.
        Map<RowInfo,ConcurrentCounterMap.LongAdder> remove = this.counter.remove();
        List<Increment> merge = rowKeyMerge.createBulkIncrement(remove);
        if (!merge.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("flush {} Increment:{}", this.getClass().getSimpleN    me(), merge.size());
            }
            hbaseTemplate.increment(MAP_STATISTICS_CALLEE, merge);
        }

	}
}
