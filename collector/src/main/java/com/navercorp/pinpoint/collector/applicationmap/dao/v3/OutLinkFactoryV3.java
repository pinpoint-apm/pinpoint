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

import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.OutLinkFactory;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidLinkRowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.v3.ColumnNameV3;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;

import java.util.Objects;

public class OutLinkFactoryV3 implements OutLinkFactory {

    private final TimeSlot timeSlot;

    public OutLinkFactoryV3(TimeSlot timeSlot) {
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");
    }

    public OutLink newOutLink(Vertex selfVertex, String selfAgentId, Vertex outVertex, String outSubLink) {
        return new OutLinkV3(selfVertex, outVertex, outSubLink);
    }

    public class OutLinkV3 implements OutLink {
        private final Vertex selfVertex;
        private final Vertex outVertex;
        private final String outSubLink;

        public OutLinkV3(Vertex selfVertex, Vertex outVertex, String outSubLink) {
            this.selfVertex = Objects.requireNonNull(selfVertex, "selfVertex");
            this.outVertex = Objects.requireNonNull(outVertex, "outVertex");
            this.outSubLink = outSubLink;
        }

        @Override
        public RowKey rowkey(long requestTime) {
            long timestamp = timeSlot.getTimeSlot(requestTime);
            return UidLinkRowKey.of(
                    selfVertex.serviceUid(), selfVertex.applicationName(), selfVertex.serviceType(),
                    timestamp,
                    outVertex.serviceUid(), outVertex.applicationName(), outVertex.serviceType().getCode(), outSubLink
            );
        }

        @Override
        public ColumnName histogram(int elapsed, boolean isError) {
            HistogramSlot outSlot = outSchema().findHistogramSlot(elapsed, isError);
            return ColumnNameV3.histogram(outSlot.getSlotCode());
        }


        @Override
        public ColumnName sum() {
            HistogramSlot outSlot = outSchema().getSumStatSlot();
            return ColumnNameV3.histogram(outSlot.getSlotCode());
        }

        @Override
        public ColumnName max() {
            HistogramSlot outSlot = outSchema().getMaxStatSlot();
            return ColumnNameV3.histogram(outSlot.getSlotCode());
        }

        private HistogramSchema outSchema() {
            return outVertex.serviceType().getHistogramSchema();
        }

    }
}
