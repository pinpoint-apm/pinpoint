/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.collector.applicationmap.dao.hbase;

import com.navercorp.pinpoint.collector.applicationmap.dao.SelfDao;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.ColumnName;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.MapLinkConfiguration;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.RowKey;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.statistics.ApplicationMapRowKey;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.statistics.ApplicationMapSelfColumnName;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.server.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.common.server.util.TimeSlot;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author intr3p1d
 */
@Repository
public class HbaseSelfDao implements SelfDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AcceptedTimeService acceptedTimeService;

    private final TimeSlot timeSlot;
    private final BulkWriter bulkWriter;
    private final MapLinkConfiguration mapLinkConfiguration;

    public HbaseSelfDao(MapLinkConfiguration mapLinkConfiguration,
                        AcceptedTimeService acceptedTimeService, TimeSlot timeSlot,
                        @Qualifier("applicationMapSelfBulkWriter") BulkWriter bulkWriter) {
        this.mapLinkConfiguration = Objects.requireNonNull(mapLinkConfiguration, "mapLinkConfiguration");
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");
        this.bulkWriter = Objects.requireNonNull(bulkWriter, "bulkWriter");
    }


    @Override
    public void received(
            String serviceName, String applicationName, ServiceType applicationType,
            int elapsed, boolean isError
    ) {
        Objects.requireNonNull(serviceName, "serviceName");
        Objects.requireNonNull(applicationName, "applicationName");

        if (logger.isDebugEnabled()) {
            logger.debug("[Received] {} {} ({})", serviceName, applicationName, applicationType);
        }

        // make row key. rowkey is me
        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        final long rowTimeSlot = timeSlot.getTimeSlot(acceptedTime);
        final RowKey selfRowKey = new ApplicationMapRowKey(serviceName, applicationType.getCode(), applicationName, rowTimeSlot);

        final short slotNumber = ApplicationMapStatisticsUtils.getSlotNumber(applicationType, elapsed, isError);
        final ColumnName selfColumnName = new ApplicationMapSelfColumnName(applicationName, applicationType.getCode(), slotNumber);
        this.bulkWriter.increment(selfRowKey, selfColumnName);

        HistogramSchema histogramSchema = applicationType.getHistogramSchema();
        if (mapLinkConfiguration.isEnableAvg()) {
            final ColumnName sumColumnName = new ApplicationMapSelfColumnName(applicationName, applicationType.getCode(), histogramSchema.getSumStatSlot().getSlotTime());
            this.bulkWriter.increment(selfRowKey, sumColumnName, elapsed);
        }

        final ColumnName maxColumnName = new ApplicationMapSelfColumnName(applicationName, applicationType.getCode(), histogramSchema.getMaxStatSlot().getSlotTime());
        if (mapLinkConfiguration.isEnableMax()) {
            this.bulkWriter.updateMax(selfRowKey, maxColumnName, elapsed);
        }
    }

    @Override
    public void updatePing(
            String serviceName, String applicationName, ServiceType applicationType,
            int elapsed, boolean isError
    ) {
        Objects.requireNonNull(serviceName, "serviceName");
        Objects.requireNonNull(applicationName, "applicationName");

        if (logger.isDebugEnabled()) {
            logger.debug("[Received] {} {} ({})", serviceName, applicationName, applicationType);
        }

        // make row key. rowkey is me
        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        final long rowTimeSlot = timeSlot.getTimeSlot(acceptedTime);
        final RowKey selfRowKey = new ApplicationMapRowKey(serviceName, applicationType.getCode(), applicationName, rowTimeSlot);

        final short slotNumber = ApplicationMapStatisticsUtils.getPingSlotNumber(applicationType, elapsed, isError);
        final ColumnName selfColumnName = new ApplicationMapSelfColumnName(applicationName, applicationType.getCode(), slotNumber);
        this.bulkWriter.increment(selfRowKey, selfColumnName);
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
