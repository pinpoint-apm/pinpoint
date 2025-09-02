/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.applicationmap.uid.dao.hbase;

import com.navercorp.pinpoint.collector.applicationmap.SelfUidVertex;
import com.navercorp.pinpoint.collector.applicationmap.config.MapLinkProperties;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.collector.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.collector.applicationmap.statistics.uid.UidLinkRowKey;
import com.navercorp.pinpoint.collector.applicationmap.statistics.uid.UidResponseColumnName;
import com.navercorp.pinpoint.collector.applicationmap.uid.dao.MapSelfUidDao;
import com.navercorp.pinpoint.collector.dao.CachedStatisticsDao;
import com.navercorp.pinpoint.common.server.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class HbaseMapSelfUidDao implements MapSelfUidDao, CachedStatisticsDao {

    private final Logger logger = LogManager.getLogger(this.getClass());


    private final TimeSlot timeSlot;
    private final BulkWriter bulkWriter;
    private final MapLinkProperties mapLinkProperties;

    public HbaseMapSelfUidDao(MapLinkProperties mapLinkProperties,
                              TimeSlot timeSlot,
                              BulkWriter bulkWriter) {
        this.mapLinkProperties = Objects.requireNonNull(mapLinkProperties, "mapLinkProperties");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");
        this.bulkWriter = Objects.requireNonNull(bulkWriter, "bulkWriter");
    }


    @Override
    public void self(long requestTime, SelfUidVertex selfVertex, int elapsed, boolean isError) {
        Objects.requireNonNull(selfVertex, "selfVertex");

        if (logger.isDebugEnabled()) {
            logger.debug("[SelfUid] {}", selfVertex);
        }

        // make row key. rowkey is me
        final long rowTimeSlot = timeSlot.getTimeSlot(requestTime);
        final RowKey selfRowKey = UidLinkRowKey.of(selfVertex, rowTimeSlot);

        final short slotNumber = ApplicationMapStatisticsUtils.getSlotNumber(selfVertex.serviceType(), elapsed, isError);
        final ColumnName selfColumnName = UidResponseColumnName.histogram(slotNumber);
        this.bulkWriter.increment(selfRowKey, selfColumnName);

        if (mapLinkProperties.isEnableAvg()) {
            final ColumnName sumColumnName = UidResponseColumnName.sum(selfVertex.serviceType());
            this.bulkWriter.increment(selfRowKey, sumColumnName, elapsed);
        }

        if (mapLinkProperties.isEnableMax()) {
            final ColumnName maxColumnName = UidResponseColumnName.max(selfVertex.serviceType());
            this.bulkWriter.updateMax(selfRowKey, maxColumnName, elapsed);
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
