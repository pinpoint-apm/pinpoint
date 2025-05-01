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

package com.navercorp.pinpoint.collector.applicationmap.dao.hbase;

import com.navercorp.pinpoint.collector.applicationmap.config.MapLinkConfiguration;
import com.navercorp.pinpoint.collector.applicationmap.dao.MapResponseTimeDao;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.collector.applicationmap.statistics.LinkRowKey;
import com.navercorp.pinpoint.collector.applicationmap.statistics.ResponseColumnName;
import com.navercorp.pinpoint.collector.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.common.server.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * Save response time data of WAS
 *
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 * @author HyunGil Jeong
 */
@Repository
public class HbaseMapResponseTimeDao implements MapResponseTimeDao {

    private final Logger logger = LogManager.getLogger(this.getClass());


    private final TimeSlot timeSlot;
    private final BulkWriter<RowKey, ColumnName> bulkWriter;
    private final MapLinkConfiguration mapLinkConfiguration;

    public HbaseMapResponseTimeDao(MapLinkConfiguration mapLinkConfiguration,
                                   TimeSlot timeSlot,
                                   @Qualifier("selfBulkWriter") BulkWriter<RowKey, ColumnName> bulkWriter) {
        this.mapLinkConfiguration = Objects.requireNonNull(mapLinkConfiguration, "mapLinkConfiguration");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");
        this.bulkWriter = Objects.requireNonNull(bulkWriter, "bulkWriter");
    }


    @Override
    public void received(long requestTime, String applicationName, ServiceType applicationServiceType, String agentId, int elapsed, boolean isError) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(agentId, "agentId");

        if (logger.isDebugEnabled()) {
            logger.debug("[Self] {} ({})[{}]", applicationName, applicationServiceType, agentId);
        }

        // make row key. rowkey is me
        final long rowTimeSlot = timeSlot.getTimeSlot(requestTime);
        final RowKey selfRowKey = LinkRowKey.of(applicationName, applicationServiceType, rowTimeSlot);

        final short slotNumber = ApplicationMapStatisticsUtils.getSlotNumber(applicationServiceType, elapsed, isError);
        final ColumnName selfColumnName = ResponseColumnName.histogram(agentId, slotNumber);
        this.bulkWriter.increment(selfRowKey, selfColumnName);

        if (mapLinkConfiguration.isEnableAvg()) {
            final ColumnName sumColumnName = ResponseColumnName.sum(agentId, applicationServiceType);
            this.bulkWriter.increment(selfRowKey, sumColumnName, elapsed);
        }

        if (mapLinkConfiguration.isEnableMax()) {
            final ColumnName maxColumnName = ResponseColumnName.max(agentId, applicationServiceType);
            this.bulkWriter.updateMax(selfRowKey, maxColumnName, elapsed);
        }
    }

    @Override
    public void updatePing(long requestTime, String applicationName, ServiceType applicationServiceType, String agentId, int elapsed, boolean isError) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(agentId, "agentId");

        if (logger.isDebugEnabled()) {
            logger.debug("[Self] {} ({})[{}]", applicationName, applicationServiceType, agentId);
        }

        // make row key. rowkey is me
        final long rowTimeSlot = timeSlot.getTimeSlot(requestTime);
        final RowKey selfRowKey = LinkRowKey.of(applicationName, applicationServiceType, rowTimeSlot);

        final short slotNumber = ApplicationMapStatisticsUtils.getPingSlotNumber(applicationServiceType);
        final ColumnName selfColumnName = ResponseColumnName.histogram(agentId, slotNumber);
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
