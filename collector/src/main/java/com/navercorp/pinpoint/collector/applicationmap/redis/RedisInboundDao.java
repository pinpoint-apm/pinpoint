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

import com.navercorp.pinpoint.collector.applicationmap.dao.InboundDao;
import com.navercorp.pinpoint.collector.applicationmap.redis.schema.ApplicationMapTable;
import com.navercorp.pinpoint.collector.applicationmap.redis.schema.TimeSeriesKey;
import com.navercorp.pinpoint.collector.applicationmap.redis.schema.TimeSeriesValue;
import com.navercorp.pinpoint.collector.dao.hbase.IgnoreStatFilter;
import com.navercorp.pinpoint.collector.applicationmap.config.MapLinkConfiguration;
import com.navercorp.pinpoint.collector.applicationmap.redis.statistics.RedisBulkWriter;
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
public class RedisInboundDao implements InboundDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final IgnoreStatFilter ignoreStatFilter;
    private final RedisBulkWriter bulkWriter;
    private final MapLinkConfiguration mapLinkConfiguration;

    public RedisInboundDao(
            MapLinkConfiguration mapLinkConfiguration,
            IgnoreStatFilter ignoreStatFilter,
            @Qualifier("inboundBulkWriter") RedisBulkWriter bulkWriter
    ) {
        this.mapLinkConfiguration = Objects.requireNonNull(mapLinkConfiguration, "mapLinkConfiguration");
        this.ignoreStatFilter = Objects.requireNonNull(ignoreStatFilter, "ignoreStatFilter");
        this.bulkWriter = Objects.requireNonNull(bulkWriter, "inboundBulkWriter");
    }


    @Override
    public void update(
            long requestTime,
            int srcServiceId, String srcApplicationName, ServiceType srcApplicationType,
            int destServiceId, String destApplicationName, ServiceType destApplicationType,
            String srcHost, int elapsed, boolean isError
    ) {
        Objects.requireNonNull(srcApplicationName, "srcApplicationName");
        Objects.requireNonNull(destApplicationName, "destApplicationName");

        if (logger.isDebugEnabled()) {
            logger.debug("[Inbound] {} {}({}) <- {} {}({})[{}]",
                    destServiceId, destApplicationName, destApplicationType,
                    srcServiceId, srcApplicationName, srcApplicationType, srcHost
            );
        }

        if (ignoreStatFilter.filter(srcApplicationType, srcHost)) {
            logger.debug("[Ignore-Inbound] {} {}({}) <- {} {}({})[{}]",
                    destServiceId, destApplicationName, destApplicationType,
                    srcServiceId, srcApplicationName, srcApplicationType, srcHost
            );
            return;
        }

        final short srcSlotNumber = ApplicationMapStatisticsUtils.getSlotNumber(srcApplicationType, elapsed, isError);
        HistogramSchema histogramSchema = srcApplicationType.getHistogramSchema();

        // for inbound, main is destination
        // and sub is source
        final TimeSeriesKey applicationTypeKey = new TimeSeriesKey(
                ApplicationMapTable.Inbound, "tenantId",
                destServiceId, destApplicationName, destApplicationType.getCode(),
                srcServiceId, srcApplicationName, srcApplicationType.getCode(),
                srcSlotNumber
        );
        TimeSeriesValue addOne = new TimeSeriesValue(requestTime);
        this.bulkWriter.increment(applicationTypeKey, addOne);

        if (mapLinkConfiguration.isEnableAvg()) {
            final TimeSeriesKey sumStatKey = new TimeSeriesKey(
                    ApplicationMapTable.Inbound, "tenantId",
                    destServiceId, destApplicationName, destApplicationType.getCode(),
                    srcServiceId, srcApplicationName, srcApplicationType.getCode(),
                    histogramSchema.getSumStatSlot().getSlotTime()
            );
            final TimeSeriesValue sumValue = new TimeSeriesValue(requestTime);
            this.bulkWriter.increment(sumStatKey, sumValue, elapsed);
        }
        if (mapLinkConfiguration.isEnableMax()) {
            final TimeSeriesKey maxStatKey = new TimeSeriesKey(
                    ApplicationMapTable.Inbound, "tenantId",
                    destServiceId, destApplicationName, destApplicationType.getCode(),
                    srcServiceId, srcApplicationName, srcApplicationType.getCode(),
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