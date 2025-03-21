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
public class RedisOutboundDao implements OutboundDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final RedisBulkWriter bulkWriter;
    private final MapLinkConfiguration mapLinkConfiguration;

    public RedisOutboundDao(
            MapLinkConfiguration mapLinkConfiguration,
            IgnoreStatFilter ignoreStatFilter,
            @Qualifier("outboundBulkWriter") RedisBulkWriter bulkWriter
    ) {
        this.mapLinkConfiguration = Objects.requireNonNull(mapLinkConfiguration, "mapLinkConfiguration");
        this.bulkWriter = Objects.requireNonNull(bulkWriter, "outboundBulkWriter");
    }


    @Override
    public void update(
            long requestTime,
            int srcServiceId, String srcApplicationName, ServiceType srcApplicationType,
            int destServiceId, String destApplicationName, ServiceType destApplicationType,
            String srcHost, int elapsed, boolean isError
    ) {
        // outbound (rowKey src -> columnName dest)
        Objects.requireNonNull(destApplicationName, "destApplicationName");
        Objects.requireNonNull(srcApplicationName, "srcApplicationName");

        if (logger.isDebugEnabled()) {
            logger.debug("[Outbound] {} {}({})[{}] -> {} {}({})",
                    srcServiceId, srcApplicationName, srcApplicationType, srcHost,
                    destServiceId, destApplicationName, destApplicationType
            );
        }

        final short destSlotNumber = ApplicationMapStatisticsUtils.getSlotNumber(destApplicationType, elapsed, isError);
        HistogramSchema histogramSchema = destApplicationType.getHistogramSchema();

        // for outbound, main is source
        // and sub is destination
        final TimeSeriesKey applicationTypeKey = new TimeSeriesKey(
                ApplicationMapTable.Outbound, "tenantId",
                srcServiceId, srcApplicationName, srcApplicationType.getCode(),
                destServiceId, destApplicationName, destApplicationType.getCode(),
                destSlotNumber
        );

        final TimeSeriesValue addOne = new TimeSeriesValue(requestTime);
        this.bulkWriter.increment(applicationTypeKey, addOne);

        if (mapLinkConfiguration.isEnableAvg()) {
            final TimeSeriesKey sumStatKey = new TimeSeriesKey(
                    ApplicationMapTable.Outbound, "tenantId",
                    srcServiceId, srcApplicationName, srcApplicationType.getCode(),
                    destServiceId, destApplicationName, destApplicationType.getCode(),
                    histogramSchema.getSumStatSlot().getSlotTime()
            );
            final TimeSeriesValue sumValue = new TimeSeriesValue(requestTime);
            this.bulkWriter.increment(sumStatKey, sumValue, elapsed);
        }
        if (mapLinkConfiguration.isEnableMax()) {
            final TimeSeriesKey maxStatKey = new TimeSeriesKey(
                    ApplicationMapTable.Outbound, "tenantId",
                    srcServiceId, srcApplicationName, srcApplicationType.getCode(),
                    destServiceId, destApplicationName, destApplicationType.getCode(),
                    histogramSchema.getMaxStatSlot().getSlotTime()
            );
            final TimeSeriesValue maxValue = new TimeSeriesValue(requestTime);
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
