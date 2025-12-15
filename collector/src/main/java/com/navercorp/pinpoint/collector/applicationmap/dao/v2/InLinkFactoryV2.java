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

import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.InLinkFactory;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.LinkRowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.v2.InLinkV2ColumnName;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

public class InLinkFactoryV2 implements InLinkFactory {

    private final TimeSlot timeSlot;

    public InLinkFactoryV2(TimeSlot timeSlot) {
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");
    }

    @Override
    public InLink newLink(String inApplicationName, ServiceType inServiceType, String selfApplicationName, ServiceType selfServiceType, String selfSubLink) {
        return new InLinkV2(inApplicationName, inServiceType, selfApplicationName, selfServiceType, selfSubLink);
    }

    public class InLinkV2 implements InLink {
        private final String inApplicationName;
        private final ServiceType inServiceType;

        private final String selfApplicationName;
        private final ServiceType selfServiceType;
        private final String selfSubLink;

        public InLinkV2(String inApplicationName, ServiceType inServiceType, String selfApplicationName, ServiceType selfServiceType, String selfSubLink) {
            this.inApplicationName = Objects.requireNonNull(inApplicationName, "inApplicationName");
            this.inServiceType = Objects.requireNonNull(inServiceType, "inServiceType");

            this.selfApplicationName = Objects.requireNonNull(selfApplicationName, "selfApplicationName");
            this.selfServiceType = Objects.requireNonNull(selfServiceType, "selfServiceType");
            this.selfSubLink = selfSubLink;
        }

        @Override
        public RowKey rowkey(long requestTime) {
            long timestamp = timeSlot.getTimeSlot(requestTime);
            return LinkRowKey.of(inApplicationName, inServiceType, timestamp);
        }

        @Override
        public ColumnName histogram(int elapsed, boolean isError) {
            HistogramSlot slot = inHistogramSchema().findHistogramSlot(elapsed, isError);
            return InLinkV2ColumnName.histogram(selfApplicationName, selfServiceType, selfSubLink, slot.getSlotTime());
        }

        @Override
        public ColumnName sum() {
            final short slotTime = inHistogramSchema().getSumStatSlot().getSlotTime();
            return InLinkV2ColumnName.histogram(selfApplicationName, selfServiceType, selfSubLink, slotTime);
        }

        @Override
        public ColumnName max() {
            final short slotTime = inHistogramSchema().getMaxStatSlot().getSlotTime();
            return InLinkV2ColumnName.histogram(selfApplicationName, selfServiceType, selfSubLink, slotTime);
        }

        private HistogramSchema inHistogramSchema() {
            return inServiceType.getHistogramSchema();
        }
    }
}
