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

import com.navercorp.pinpoint.common.server.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidAppRowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.v3.ColumnNameV3;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

public class SelfAppNodeFactoryV3 implements SelfAppNodeFactory {

    public static final ServiceUid DEFAULT = ServiceUid.DEFAULT;

    private final TimeSlot timeSlot;

    public SelfAppNodeFactoryV3(TimeSlot timeSlot) {
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");
    }

    @Override
    public SelfAppNodeFactory.Node newNode(String applicationName, ServiceType serviceType) {
        return new SelfAppNode(applicationName, serviceType);
    }

    public class SelfAppNode implements Node {

        private final String applicationName;
        private final ServiceType serviceType;

        public SelfAppNode(String applicationName, ServiceType serviceType) {
            this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
            this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
        }

        @Override
        public RowKey rowkey(long requestTime) {
            final long timestamp = timeSlot.getTimeSlot(requestTime);
            return UidAppRowKey.of(DEFAULT.getUid(), applicationName, serviceType, timestamp);
        }

        @Override
        public ColumnName histogram(int elapsed, boolean isError) {
            HistogramSlot slot = getHistogramSchema().findHistogramSlot(elapsed, isError);
            return ColumnNameV3.histogram(slot.getSlotCode());
        }

        @Override
        public ColumnName sum() {
            HistogramSlot slot = getHistogramSchema().getSumStatSlot();
            return ColumnNameV3.histogram(slot.getSlotCode());
        }

        @Override
        public ColumnName max() {
            HistogramSlot slot = getHistogramSchema().getMaxStatSlot();
            return ColumnNameV3.histogram(slot.getSlotCode());
        }

        private HistogramSchema getHistogramSchema() {
            return serviceType.getHistogramSchema();
        }
    }
}
