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

package com.navercorp.pinpoint.collector.applicationmap.dao.v3;

import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.InLinkFactory;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidLinkRowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.v3.ColumnNameV3;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;

import java.util.Objects;

public class InLinkFactoryV3 implements InLinkFactory {

    private final TimeSlot timeSlot;

    public InLinkFactoryV3(TimeSlot timeSlot) {
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");
    }

    @Override
    public InLink newLink(Vertex inVertex, Vertex selfVertex, String selfSubLink) {
        return new InLinkV3(inVertex, selfVertex, selfSubLink);
    }

    public class InLinkV3 implements InLink {
        private final Vertex inVertex;
        private final Vertex selfVertex;
        private final String selfSubLink;

        public InLinkV3(Vertex inVertex, Vertex selfVertex, String selfSubLink) {
            this.inVertex = Objects.requireNonNull(inVertex, "inVertex");
            this.selfVertex = Objects.requireNonNull(selfVertex, "selfVertex");
            this.selfSubLink = selfSubLink;
        }

        @Override
        public RowKey rowkey(long requestTime) {
            final long timestamp = timeSlot.getTimeSlot(requestTime);
            return UidLinkRowKey.of(
                    inVertex.serviceUid(), inVertex.applicationName(), inVertex.serviceType(),
                    timestamp,
                    selfVertex.serviceUid(), selfVertex.applicationName(), selfVertex.serviceType().getCode(), selfSubLink
            );
        }

        @Override
        public ColumnName histogram(int elapsed, boolean isError) {
            HistogramSlot slot = inSchema().findHistogramSlot(elapsed, isError);
            return ColumnNameV3.histogram(slot.getSlotCode());
        }

        @Override
        public ColumnName sum() {
            HistogramSlot slot = inSchema().getSumStatSlot();
            return ColumnNameV3.histogram(slot.getSlotCode());
        }

        @Override
        public ColumnName max() {
            HistogramSlot slot = inSchema().getMaxStatSlot();
            return ColumnNameV3.histogram(slot.getSlotCode());
        }

        private HistogramSchema inSchema() {
            return inVertex.serviceType().getHistogramSchema();
        }
    }
}
