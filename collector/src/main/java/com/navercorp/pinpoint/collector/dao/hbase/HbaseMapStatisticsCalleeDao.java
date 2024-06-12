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

package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.MapStatisticsCalleeDao;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.CallRowKey;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.CallerColumnName;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.ColumnName;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.MapLinkConfiguration;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.RowKey;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.server.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.common.server.util.TimeSlot;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * Update statistics of callee node
 *
 * @author netspider
 * @author emeroad
 * @author HyunGil Jeong
 */
@Repository
public class HbaseMapStatisticsCalleeDao implements MapStatisticsCalleeDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AcceptedTimeService acceptedTimeService;

    private final TimeSlot timeSlot;

    private final IgnoreStatFilter ignoreStatFilter;
    private final BulkWriter bulkWriter;
    private final MapLinkConfiguration mapLinkConfiguration;

    public HbaseMapStatisticsCalleeDao(MapLinkConfiguration mapLinkConfiguration,
                                       IgnoreStatFilter ignoreStatFilter,
                                       AcceptedTimeService acceptedTimeService, TimeSlot timeSlot,
                                       @Qualifier("calleeBulkWriter") BulkWriter bulkWriter) {
        this.mapLinkConfiguration = Objects.requireNonNull(mapLinkConfiguration, "mapLinkConfiguration");
        this.ignoreStatFilter = Objects.requireNonNull(ignoreStatFilter, "ignoreStatFilter");
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");

        this.bulkWriter = Objects.requireNonNull(bulkWriter, "bulkWriter");
    }


    @Override
    public void update(String calleeApplicationName, ServiceType calleeServiceType, String callerApplicationName, ServiceType callerServiceType, String callerHost, int elapsed, boolean isError) {
        Objects.requireNonNull(calleeApplicationName, "calleeApplicationName");
        Objects.requireNonNull(callerApplicationName, "callerApplicationName");

        if (logger.isDebugEnabled()) {
            logger.debug("[Callee] {} ({}) <- {} ({})[{}]",
                    calleeApplicationName, calleeServiceType, callerApplicationName, callerServiceType, callerHost);
        }

        // there may be no endpoint in case of httpclient
        callerHost = StringUtils.defaultString(callerHost);

        // TODO: callee, caller parameter normalization
        if (ignoreStatFilter.filter(calleeServiceType, callerHost)) {
            logger.debug("[Ignore-Callee] {} ({}) <- {} ({})[{}]",
                    calleeApplicationName, calleeServiceType, callerApplicationName, callerServiceType, callerHost);
            return;
        }

        // make row key. row key is me
        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        final long rowTimeSlot = timeSlot.getTimeSlot(acceptedTime);
        final RowKey calleeRowKey = new CallRowKey(calleeApplicationName, calleeServiceType.getCode(), rowTimeSlot);

        final short callerSlotNumber = ApplicationMapStatisticsUtils.getSlotNumber(calleeServiceType, elapsed, isError);

        HistogramSchema histogramSchema = calleeServiceType.getHistogramSchema();

        final ColumnName callerColumnName = new CallerColumnName(callerServiceType.getCode(), callerApplicationName, callerHost, callerSlotNumber);
        this.bulkWriter.increment(calleeRowKey, callerColumnName);

        if (mapLinkConfiguration.isEnableAvg()) {
            final ColumnName sumColumnName = new CallerColumnName(callerServiceType.getCode(), callerApplicationName, callerHost, histogramSchema.getSumStatSlot().getSlotTime());
            this.bulkWriter.increment(calleeRowKey, sumColumnName, elapsed);
        }
        if (mapLinkConfiguration.isEnableMax()) {
            final ColumnName maxColumnName = new CallerColumnName(callerServiceType.getCode(), callerApplicationName, callerHost, histogramSchema.getMaxStatSlot().getSlotTime());
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
