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

import com.navercorp.pinpoint.collector.applicationmap.dao.OutboundDao;
import com.navercorp.pinpoint.collector.applicationmap.redis.schema.ApplicationMapTable;
import com.navercorp.pinpoint.collector.applicationmap.redis.schema.TimeSeriesKey;
import com.navercorp.pinpoint.collector.applicationmap.redis.schema.TimeSeriesValue;
import com.navercorp.pinpoint.collector.applicationmap.redis.statistics.RedisBulkWriter;
import com.navercorp.pinpoint.collector.dao.hbase.IgnoreStatFilter;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.MapLinkConfiguration;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
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
public class RedisOutboundDao implements OutboundDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AcceptedTimeService acceptedTimeService;
    private final RedisBulkWriter bulkWriter;
    private final MapLinkConfiguration mapLinkConfiguration;

    public RedisOutboundDao(
            MapLinkConfiguration mapLinkConfiguration,
            AcceptedTimeService acceptedTimeService,
            IgnoreStatFilter ignoreStatFilter,
            @Qualifier("outboundBulkWriter") RedisBulkWriter bulkWriter
    ) {
        this.mapLinkConfiguration = Objects.requireNonNull(mapLinkConfiguration, "mapLinkConfiguration");
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
        this.bulkWriter = Objects.requireNonNull(bulkWriter, "outboundBulkWriter");
    }


    @Override
    public void update(
            String srcServiceName, String srcApplicationName, ServiceType srcApplicationType,
            String destServiceName, String destApplicationName, ServiceType destApplicationType,
            String srcHost, int elapsed, boolean isError
    ) {
        // outbound (rowKey src -> columnName dest)
        Objects.requireNonNull(destServiceName, "destServiceName");
        Objects.requireNonNull(srcServiceName, "srcServiceName");
        Objects.requireNonNull(destApplicationName, "destApplicationName");
        Objects.requireNonNull(srcServiceName, "srcApplicationName");

        if (logger.isDebugEnabled()) {
            logger.debug("[Outbound] {} {}({})[{}] -> {} {}({})",
                    srcServiceName, srcApplicationName, srcApplicationType, srcHost,
                    destServiceName, destApplicationName, destApplicationType
            );
        }

        final short destSlotNumber = ApplicationMapStatisticsUtils.getSlotNumber(destApplicationType, elapsed, isError);
        HistogramSchema histogramSchema = destApplicationType.getHistogramSchema();
        final long acceptedTime = acceptedTimeService.getAcceptedTime();

        // for outbound, main is source
        // and sub is destination
        final TimeSeriesKey applicationTypeKey = new TimeSeriesKey(
                ApplicationMapTable.Outbound, "tenantId",
                srcServiceName, srcApplicationName, srcApplicationType.getCode(),
                destServiceName, destApplicationName, destApplicationType.getCode(),
                destSlotNumber
        );

        final TimeSeriesValue addOne = new TimeSeriesValue(acceptedTime);
        this.bulkWriter.increment(applicationTypeKey, addOne);

        if (mapLinkConfiguration.isEnableAvg()) {
            final TimeSeriesKey sumStatKey = new TimeSeriesKey(
                    ApplicationMapTable.Outbound, "tenantId",
                    srcServiceName, srcApplicationName, srcApplicationType.getCode(),
                    destServiceName, destApplicationName, destApplicationType.getCode(),
                    histogramSchema.getSumStatSlot().getSlotTime()
            );
            final TimeSeriesValue sumValue = new TimeSeriesValue(acceptedTime);
            this.bulkWriter.increment(sumStatKey, sumValue, elapsed);
        }
        if (mapLinkConfiguration.isEnableMax()) {
            final TimeSeriesKey maxStatKey = new TimeSeriesKey(
                    ApplicationMapTable.Outbound, "tenantId",
                    srcServiceName, srcApplicationName, srcApplicationType.getCode(),
                    destServiceName, destApplicationName, destApplicationType.getCode(),
                    histogramSchema.getMaxStatSlot().getSlotTime()
            );
            final TimeSeriesValue maxValue = new TimeSeriesValue(acceptedTime);
            this.bulkWriter.updateMax(maxStatKey, maxValue, elapsed);
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
