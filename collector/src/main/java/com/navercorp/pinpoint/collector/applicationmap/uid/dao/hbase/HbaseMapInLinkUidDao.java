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
import com.navercorp.pinpoint.collector.applicationmap.statistics.LinkRowKey;
import com.navercorp.pinpoint.collector.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.collector.applicationmap.uid.dao.MapInLinkUidDao;
import com.navercorp.pinpoint.collector.dao.CachedStatisticsDao;
import com.navercorp.pinpoint.collector.dao.hbase.IgnoreStatFilter;
import com.navercorp.pinpoint.common.server.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class HbaseMapInLinkUidDao implements MapInLinkUidDao, CachedStatisticsDao {

    private final Logger logger = LogManager.getLogger(this.getClass());


    private final TimeSlot timeSlot;

    private final IgnoreStatFilter ignoreStatFilter;
    private final BulkWriter bulkWriter;
    private final MapLinkProperties mapLinkProperties;

    public HbaseMapInLinkUidDao(MapLinkProperties mapLinkProperties,
                                IgnoreStatFilter ignoreStatFilter,
                                TimeSlot timeSlot,
                                BulkWriter bulkWriter) {
        this.mapLinkProperties = Objects.requireNonNull(mapLinkProperties, "mapLinkConfiguration");
        this.ignoreStatFilter = Objects.requireNonNull(ignoreStatFilter, "ignoreStatFilter");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");

        this.bulkWriter = Objects.requireNonNull(bulkWriter, "bulkWriter");
    }


    @Override
    public void insertInLink(long requestTime,
                             String inLinkApplicationName,
                             ServiceType inLinkServiceType,
                             SelfUidVertex selfVertex,
                             String selfEndPoint,
                             int elapsed, boolean isError) {
        Objects.requireNonNull(inLinkApplicationName, "inLinkApplicationName");
        Objects.requireNonNull(inLinkServiceType, "inLinkServiceType");

        if (logger.isDebugEnabled()) {
            logger.debug("[InLinkUid] {}/{} <- {}/{}", inLinkApplicationName, inLinkServiceType, selfVertex, selfEndPoint);
        }

        // there may be no endpoint in case of httpclient
        selfEndPoint = Objects.toString(selfEndPoint, "");

        // TODO callee, caller parameter normalization
        if (ignoreStatFilter.filter(inLinkServiceType, selfEndPoint)) {
            logger.debug("[Ignore-InLinkUid] {}/{} <- {}/{}",  inLinkApplicationName, inLinkServiceType, selfVertex, selfEndPoint);
            return;
        }

        // make row key. rowkey is me
        final long rowTimeSlot = timeSlot.getTimeSlot(requestTime);
        final RowKey inLinkRowKey = LinkRowKey.of(inLinkApplicationName, inLinkServiceType, rowTimeSlot);

        final short outSlotNumber = ApplicationMapStatisticsUtils.getSlotNumber(inLinkServiceType, elapsed, isError);

        final ColumnName outLink = InLinkUidColumnName.histogram(selfVertex.service(), selfVertex.application(), selfVertex.serviceType(), selfEndPoint, outSlotNumber);
        this.bulkWriter.increment(inLinkRowKey, outLink);

        if (mapLinkProperties.isEnableAvg()) {
            final ColumnName sumOutLink = InLinkUidColumnName.sum(selfVertex.service(), selfVertex.application(), selfVertex.serviceType(),
                    selfEndPoint, inLinkServiceType);
            this.bulkWriter.increment(inLinkRowKey, sumOutLink, elapsed);
        }
        if (mapLinkProperties.isEnableMax()) {
            final ColumnName maxOutLink = InLinkUidColumnName.max(selfVertex.service(), selfVertex.application(), selfVertex.serviceType(),
                    selfEndPoint, inLinkServiceType);
            this.bulkWriter.updateMax(inLinkRowKey, maxOutLink, elapsed);
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
