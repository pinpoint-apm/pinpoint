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

import com.navercorp.pinpoint.collector.dao.MapStatisticsCallerDao;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.CallRowKey;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.CalleeColumnName;
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
 * Update statistics of caller node
 * 
 * @author netspider
 * @author emeroad
 * @author HyunGil Jeong
 */
@Repository
public class HbaseMapStatisticsCallerDao implements MapStatisticsCallerDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AcceptedTimeService acceptedTimeService;

    private final TimeSlot timeSlot;
    private final BulkWriter bulkWriter;
    private final MapLinkConfiguration mapLinkConfiguration;

    public HbaseMapStatisticsCallerDao(MapLinkConfiguration mapLinkConfiguration,
                                       AcceptedTimeService acceptedTimeService,
                                       TimeSlot timeSlot,
                                       @Qualifier("callerBulkWriter") BulkWriter bulkWriter) {
        this.mapLinkConfiguration = Objects.requireNonNull(mapLinkConfiguration, "mapLinkConfiguration");
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");

        this.bulkWriter = Objects.requireNonNull(bulkWriter, "bulkWriter");
    }


    @Override
    public void update(String callerApplicationName, ServiceType callerServiceType, String callerAgentid, String calleeApplicationName, ServiceType calleeServiceType, String calleeHost, int elapsed, boolean isError) {
        Objects.requireNonNull(callerApplicationName, "callerApplicationName");
        Objects.requireNonNull(calleeApplicationName, "calleeApplicationName");


        if (logger.isDebugEnabled()) {
            logger.debug("[Caller] {} ({}) {} -> {} ({})[{}]", callerApplicationName, callerServiceType, callerAgentid,
                    calleeApplicationName, calleeServiceType, calleeHost);
        }

        // there may be no endpoint in case of httpclient
        calleeHost = StringUtils.defaultString(calleeHost);

        // make row key. row key is me
        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        final long rowTimeSlot = timeSlot.getTimeSlot(acceptedTime);
        final RowKey callerRowKey = new CallRowKey(callerApplicationName, callerServiceType.getCode(), rowTimeSlot);

        final short calleeSlotNumber = ApplicationMapStatisticsUtils.getSlotNumber(calleeServiceType, elapsed, isError);

        HistogramSchema histogramSchema = callerServiceType.getHistogramSchema();

        final ColumnName calleeColumnName = new CalleeColumnName(callerAgentid, calleeServiceType.getCode(), calleeApplicationName, calleeHost, calleeSlotNumber);
        this.bulkWriter.increment(callerRowKey, calleeColumnName);

        if (mapLinkConfiguration.isEnableAvg()) {
            final ColumnName sumColumnName = new CalleeColumnName(callerAgentid, calleeServiceType.getCode(), calleeApplicationName, calleeHost, histogramSchema.getSumStatSlot().getSlotTime());
            this.bulkWriter.increment(callerRowKey, sumColumnName, elapsed);
        }
        if (mapLinkConfiguration.isEnableMax()) {
            final ColumnName maxColumnName = new CalleeColumnName(callerAgentid, calleeServiceType.getCode(), calleeApplicationName, calleeHost, histogramSchema.getMaxStatSlot().getSlotTime());
            this.bulkWriter.updateMax(callerRowKey, maxColumnName, elapsed);
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