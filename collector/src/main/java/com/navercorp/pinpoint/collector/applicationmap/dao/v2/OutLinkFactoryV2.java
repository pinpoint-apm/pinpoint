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

package com.navercorp.pinpoint.collector.applicationmap.dao.v2;

import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.OutLinkFactory;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.LinkRowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.v2.OutLinkV2ColumnName;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

public class OutLinkFactoryV2 implements OutLinkFactory {
    private final TimeSlot timeSlot;

    public OutLinkFactoryV2(TimeSlot timeSlot) {
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");
    }

    public OutLink newOutLink(String selfApplicationName, ServiceType selfServiceType, String selfAgentId,
                              String outApplicationName, ServiceType outServiceType, String outSubLink) {
        return new OutLinkV2(selfApplicationName, selfServiceType, selfAgentId,
                outApplicationName, outServiceType, outSubLink);
    }

    public class OutLinkV2 implements OutLink {
        private final String selfApplicationName;
        private final ServiceType selfServiceType;
        private final String selfAgentId;

        private final String outApplicationName;
        private final ServiceType outServiceType;
        private final String outSubLink;

        public OutLinkV2(String selfApplicationName, ServiceType selfServiceType, String selfAgentId,
                         String outApplicationName, ServiceType outServiceType, String outSubLink) {
            this.selfApplicationName = Objects.requireNonNull(selfApplicationName, "selfApplicationName");
            this.selfServiceType = Objects.requireNonNull(selfServiceType, "selfServiceType");
            this.selfAgentId = selfAgentId;

            this.outApplicationName = Objects.requireNonNull(outApplicationName, "outApplicationName");
            this.outServiceType = Objects.requireNonNull(outServiceType, "outServiceType");
            this.outSubLink = outSubLink;
        }

        @Override
        public RowKey rowkey(long requestTime) {
            long timestamp = timeSlot.getTimeSlot(requestTime);
            return LinkRowKey.of(selfApplicationName, selfServiceType, timestamp);
        }

        @Override
        public ColumnName histogram(int elapsed, boolean isError) {
            final HistogramSlot outSlot = outServiceType.getHistogramSchema().findHistogramSlot(elapsed, isError);
            return OutLinkV2ColumnName.histogram(selfAgentId, outServiceType, outApplicationName, outSubLink, outSlot.getSlotTime());
        }

        @Override
        public ColumnName sum() {
            // for backward compatibility outServiceType.getHistogramSchema ??
            HistogramSlot outSlot = selfServiceType.getHistogramSchema().getSumStatSlot();
            return OutLinkV2ColumnName.histogram(selfAgentId, outServiceType, outApplicationName, outSubLink, outSlot.getSlotTime());
        }

        @Override
        public ColumnName max() {
            // for backward compatibility. outServiceType.getHistogramSchema ??
            HistogramSlot outSlot = selfServiceType.getHistogramSchema().getMaxStatSlot();
            return OutLinkV2ColumnName.histogram(selfAgentId, outServiceType, outApplicationName, outSubLink, outSlot.getSlotTime());
        }
    }
}
