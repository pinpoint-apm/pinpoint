/*
 * Copyright 2021 NAVER Corp.
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

import com.navercorp.pinpoint.collector.dao.MapStatisticsCalleeCompactDao;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.CallRowKey;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.CallerCompactColumnName;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.ColumnName;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.MapLinkConfiguration;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.RowKey;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.server.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.common.server.util.TimeSlot;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * Update statistics of callee node
 */
@Repository
public class HbaseMapStatisticsCalleeCompactDao implements MapStatisticsCalleeCompactDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AcceptedTimeService acceptedTimeService;

    private final TimeSlot timeSlot;

    private final IgnoreStatFilter ignoreStatFilter;
    private final BulkWriter bulkWriter;
    private final MapLinkConfiguration mapLinkConfiguration;

    @Autowired
    public HbaseMapStatisticsCalleeCompactDao(MapLinkConfiguration mapLinkConfiguration,
                                              IgnoreStatFilter ignoreStatFilter,
                                              AcceptedTimeService acceptedTimeService, TimeSlot timeSlot,
                                              @Qualifier("calleeCompactBulkWriter") BulkWriter bulkWriter) {
        this.mapLinkConfiguration = Objects.requireNonNull(mapLinkConfiguration, "mapLinkConfiguration");
        this.ignoreStatFilter = Objects.requireNonNull(ignoreStatFilter, "ignoreStatFilter");
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");

        this.bulkWriter = Objects.requireNonNull(bulkWriter, "bulkWriter");
    }

    @Override
    public void update(String calleeApplicationName, ServiceType calleeServiceType, String callerApplicationName, ServiceType callerServiceType, int elapsed, boolean isError) {
        Objects.requireNonNull(calleeApplicationName, "calleeApplicationName");
        Objects.requireNonNull(callerApplicationName, "callerApplicationName");

        if (logger.isDebugEnabled()) {
            logger.debug("[Callee] {} ({}) <- {} ({})",
                    calleeApplicationName, calleeServiceType, callerApplicationName, callerServiceType);
        }

        // make row key. rowkey is me
        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        final long rowTimeSlot = timeSlot.getTimeSlot(acceptedTime);
        final RowKey calleeRowKey = new CallRowKey(calleeApplicationName, calleeServiceType.getCode(), rowTimeSlot);
        final short callerSlotNumber = ApplicationMapStatisticsUtils.getSlotNumber(calleeServiceType, elapsed, isError);
        HistogramSchema histogramSchema = calleeServiceType.getHistogramSchema();

        final ColumnName callerColumnName = new CallerCompactColumnName(callerServiceType.getCode(), callerApplicationName, callerSlotNumber);
        this.bulkWriter.increment(calleeRowKey, callerColumnName);

        if (mapLinkConfiguration.isEnableAvg()) {
            final ColumnName sumColumnName = new CallerCompactColumnName(callerServiceType.getCode(), callerApplicationName, histogramSchema.getSumStatSlot().getSlotTime());
            this.bulkWriter.increment(calleeRowKey, sumColumnName, elapsed);
        }
        if (mapLinkConfiguration.isEnableMax()) {
            final ColumnName maxColumnName = new CallerCompactColumnName(callerServiceType.getCode(), callerApplicationName, histogramSchema.getMaxStatSlot().getSlotTime());
            this.bulkWriter.updateMax(calleeRowKey, maxColumnName, elapsed);
        }
    }

    @Override
    public void flushLink() {
        this.bulkWriter.flushLink();
    }

    @Override
    public void flushAvgMax() {
        this.bulkWriter.flushAvgMax();
    }
}