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
package com.navercorp.pinpoint.collector.applicationmap.redis;

import com.navercorp.pinpoint.collector.applicationmap.dao.SelfDao;
import com.navercorp.pinpoint.collector.applicationmap.redis.schema.ApplicationMapTable;
import com.navercorp.pinpoint.collector.applicationmap.redis.schema.TimeSeriesKey;
import com.navercorp.pinpoint.collector.applicationmap.redis.schema.TimeSeriesValue;
import com.navercorp.pinpoint.collector.applicationmap.redis.statistics.RedisBulkWriter;
import com.navercorp.pinpoint.collector.applicationmap.config.MapLinkConfiguration;
import com.navercorp.pinpoint.common.server.util.ApplicationMapStatisticsUtils;
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
public class RedisSelfDao implements SelfDao {

    private final Logger logger = LogManager.getLogger(this.getClass());


    private final RedisBulkWriter bulkWriter;
    private final MapLinkConfiguration mapLinkConfiguration;

    public RedisSelfDao(
            MapLinkConfiguration mapLinkConfiguration,
            @Qualifier("applicationMapSelfBulkWriter") RedisBulkWriter bulkWriter
    ) {
        this.mapLinkConfiguration = Objects.requireNonNull(mapLinkConfiguration, "mapLinkConfiguration");
        this.bulkWriter = Objects.requireNonNull(bulkWriter, "bulkWriter");
    }


    @Override
    public void received(
            long requestTime,
            int serviceId, String applicationName, ServiceType applicationType,
            int elapsed, boolean isError
    ) {
        Objects.requireNonNull(applicationName, "applicationName");

        if (logger.isDebugEnabled()) {
            logger.debug("[Received] {} {} ({})", serviceId, applicationName, applicationType);
        }

        final short slotNumber = ApplicationMapStatisticsUtils.getSlotNumber(applicationType, elapsed, isError);
        HistogramSchema histogramSchema = applicationType.getHistogramSchema();

        // for self, main is me
        // and sub is also me
        final TimeSeriesKey applicationTypeKey = new TimeSeriesKey(
                ApplicationMapTable.Self, "tenantId",
                serviceId, applicationName, applicationType.getCode(),
                serviceId, applicationName, applicationType.getCode(),
                slotNumber
        );
        TimeSeriesValue addOne = new TimeSeriesValue(requestTime);
        this.bulkWriter.increment(applicationTypeKey, addOne);

        if (mapLinkConfiguration.isEnableAvg()) {
            final TimeSeriesKey sumStatKey = new TimeSeriesKey(
                    ApplicationMapTable.Self, "tenantId",
                    serviceId, applicationName, applicationType.getCode(),
                    serviceId, applicationName, applicationType.getCode(),
                    histogramSchema.getSumStatSlot().getSlotTime()
            );
            final TimeSeriesValue sumValue = new TimeSeriesValue(requestTime);
            this.bulkWriter.increment(sumStatKey, sumValue, elapsed);
        }
        if (mapLinkConfiguration.isEnableMax()) {
            final TimeSeriesKey maxStatKey = new TimeSeriesKey(
                    ApplicationMapTable.Self, "tenantId",
                    serviceId, applicationName, applicationType.getCode(),
                    serviceId, applicationName, applicationType.getCode(),
                    histogramSchema.getMaxStatSlot().getSlotTime()
            );
            final TimeSeriesValue maxValue = new TimeSeriesValue(requestTime);
            this.bulkWriter.updateMax(maxStatKey, maxValue, elapsed);
        }
    }

    @Override
    public void updatePing(
            long requestTime,
            int serviceId, String applicationName, ServiceType applicationType,
            int elapsed, boolean isError
    ) {
        Objects.requireNonNull(applicationName, "applicationName");

        if (logger.isDebugEnabled()) {
            logger.debug("[Received] {} {} ({})", serviceId, applicationName, applicationType);
        }

        // make row key. rowkey is me
        final short slotNumber = ApplicationMapStatisticsUtils.getPingSlotNumber(applicationType, elapsed, isError);

        final TimeSeriesKey selfPingKey = new TimeSeriesKey(
                ApplicationMapTable.Self, "tenantId",
                serviceId, applicationName, applicationType.getCode(),
                serviceId, applicationName, applicationType.getCode(),
                slotNumber
        );
        TimeSeriesValue addOne = new TimeSeriesValue(requestTime);
        this.bulkWriter.increment(selfPingKey, addOne);
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
