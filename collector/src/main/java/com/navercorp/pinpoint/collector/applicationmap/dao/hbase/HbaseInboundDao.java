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

import com.navercorp.pinpoint.collector.applicationmap.dao.InboundDao;
import com.navercorp.pinpoint.collector.dao.hbase.IgnoreStatFilter;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.ColumnName;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.MapLinkConfiguration;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.RowKey;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.statistics.ApplicationMapColumnName;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.statistics.ApplicationMapRowKey;
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
public class HbaseInboundDao implements InboundDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AcceptedTimeService acceptedTimeService;

    private final TimeSlot timeSlot;
    private final IgnoreStatFilter ignoreStatFilter;
    private final BulkWriter bulkWriter;
    private final MapLinkConfiguration mapLinkConfiguration;

    public HbaseInboundDao(
            MapLinkConfiguration mapLinkConfiguration,
            IgnoreStatFilter ignoreStatFilter,
            AcceptedTimeService acceptedTimeService,
            TimeSlot timeSlot,
            @Qualifier("inboundBulkWriter") BulkWriter bulkWriter
    ) {
        this.mapLinkConfiguration = Objects.requireNonNull(mapLinkConfiguration, "mapLinkConfiguration");
        this.ignoreStatFilter = Objects.requireNonNull(ignoreStatFilter, "ignoreStatFilter");
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");

        this.bulkWriter = Objects.requireNonNull(bulkWriter, "inboundBulkWriter");
    }


    @Override
    public void update(
            String srcServiceName, String srcApplicationName, ServiceType srcApplicationType,
            String destServiceName, String destApplicationName, ServiceType destApplicationType,
            String srcHost, int elapsed, boolean isError
    ) {
        Objects.requireNonNull(srcServiceName, "srcServiceName");
        Objects.requireNonNull(destServiceName, "destServiceName");
        Objects.requireNonNull(srcApplicationName, "srcApplicationName");
        Objects.requireNonNull(destServiceName, "destApplicationName");

        if (logger.isDebugEnabled()) {
            logger.debug("[Inbound] {} {}({}) <- {} {}({})[{}]",
                    destServiceName, destApplicationName, destApplicationType,
                    srcServiceName, srcApplicationName, srcApplicationType, srcHost
            );
        }


        // TODO dest, src parameter normalization
        if (ignoreStatFilter.filter(srcApplicationType, srcHost)) {
            logger.debug("[Ignore-Inbound] {} {}({}) <- {} {}({})[{}]",
                    destServiceName, destApplicationName, destApplicationType,
                    srcServiceName, srcApplicationName, srcApplicationType, srcHost
            );
            return;
        }

        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        final long rowTimeSlot = timeSlot.getTimeSlot(acceptedTime);

        // rowKey is dest in inbound
        final RowKey destRowKey = new ApplicationMapRowKey(destServiceName, destApplicationType.getCode(), destApplicationName, rowTimeSlot);

        // columnName is src in outbound
        final short srcSlotNumber = ApplicationMapStatisticsUtils.getSlotNumber(srcApplicationType, elapsed, isError);
        HistogramSchema histogramSchema = srcApplicationType.getHistogramSchema();

        final ColumnName srcColumnName = new ApplicationMapColumnName(srcServiceName, srcApplicationType.getCode(), srcApplicationName, srcSlotNumber);
        this.bulkWriter.increment(destRowKey, srcColumnName);

        if (mapLinkConfiguration.isEnableAvg()) {
            final ColumnName sumColumnName = new ApplicationMapColumnName(srcServiceName, srcApplicationType.getCode(), srcApplicationName, histogramSchema.getSumStatSlot().getSlotTime());
            this.bulkWriter.increment(destRowKey, sumColumnName, elapsed);
        }
        if (mapLinkConfiguration.isEnableMax()) {
            final ColumnName maxColumnName = new ApplicationMapColumnName(srcServiceName, srcApplicationType.getCode(), srcApplicationName, histogramSchema.getMaxStatSlot().getSlotTime());
            this.bulkWriter.updateMax(destRowKey, maxColumnName, elapsed);
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
