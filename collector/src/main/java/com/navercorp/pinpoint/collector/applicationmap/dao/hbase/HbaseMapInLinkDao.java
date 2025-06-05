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
import com.navercorp.pinpoint.collector.applicationmap.dao.MapInLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.collector.applicationmap.statistics.LinkRowKey;
import com.navercorp.pinpoint.collector.applicationmap.statistics.OutLinkColumnName;
import com.navercorp.pinpoint.collector.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.collector.dao.hbase.IgnoreStatFilter;
import com.navercorp.pinpoint.common.server.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class HbaseMapInLinkDao implements MapInLinkDao {

    private final Logger logger = LogManager.getLogger(this.getClass());


    private final TimeSlot timeSlot;

    private final IgnoreStatFilter ignoreStatFilter;
    private final BulkWriter bulkWriter;
    private final MapLinkConfiguration mapLinkConfiguration;

    public HbaseMapInLinkDao(MapLinkConfiguration mapLinkConfiguration,
                             IgnoreStatFilter ignoreStatFilter,
                             TimeSlot timeSlot,
                             @Qualifier("inLinkBulkWriter") BulkWriter bulkWriter) {
        this.mapLinkConfiguration = Objects.requireNonNull(mapLinkConfiguration, "mapLinkConfiguration");
        this.ignoreStatFilter = Objects.requireNonNull(ignoreStatFilter, "ignoreStatFilter");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");

        this.bulkWriter = Objects.requireNonNull(bulkWriter, "bulkWriter");
    }


    @Override
    public void inLink(long requestTime, String inApplicationName, ServiceType inServiceType,
                       String outApplicationName, ServiceType outServiceType, String outHost, int elapsed, boolean isError) {
        Objects.requireNonNull(inApplicationName, "inApplicationName");
        Objects.requireNonNull(outApplicationName, "outApplicationName");

        if (logger.isDebugEnabled()) {
            logger.debug("[InLink] {} ({}) <- {} ({})[{}]",
                    inApplicationName, inServiceType, outApplicationName, outServiceType, outHost);
        }

        // there may be no endpoint in case of httpclient
        outHost = Objects.toString(outHost, "");

        // TODO callee, caller parameter normalization
        if (ignoreStatFilter.filter(inServiceType, outHost)) {
            logger.debug("[Ignore-InLink] {} ({}) <- {} ({})[{}]",
                    inApplicationName, inServiceType, outApplicationName, outServiceType, outHost);
            return;
        }

        // make row key. rowkey is me
        final long rowTimeSlot = timeSlot.getTimeSlot(requestTime);
        final RowKey inLinkRowKey = LinkRowKey.of(inApplicationName, inServiceType, rowTimeSlot);

        final short outSlotNumber = ApplicationMapStatisticsUtils.getSlotNumber(inServiceType, elapsed, isError);

        final ColumnName outLink = OutLinkColumnName.histogram(outApplicationName, outServiceType, outHost, outSlotNumber);
        this.bulkWriter.increment(inLinkRowKey, outLink);

        if (mapLinkConfiguration.isEnableAvg()) {
            final ColumnName sumOutLink = OutLinkColumnName.sum(outApplicationName, outServiceType, outHost, inServiceType);
            this.bulkWriter.increment(inLinkRowKey, sumOutLink, elapsed);
        }
        if (mapLinkConfiguration.isEnableMax()) {
            final ColumnName maxOutLink = OutLinkColumnName.max(outApplicationName, outServiceType, outHost, inServiceType);
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
