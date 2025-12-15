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

import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.SelfAgentNodeFactory;
import com.navercorp.pinpoint.collector.applicationmap.statistics.ResponseColumnName;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.LinkRowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

public class SelfAgentNodeFactoryV2 implements SelfAgentNodeFactory {

    private final TimeSlot timeSlot;

    public SelfAgentNodeFactoryV2(TimeSlot timeSlot) {
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");
    }

    @Override
    public Node newNode(String applicationName, ServiceType serviceType, String agentId) {
        return new SelfAgentNode(applicationName, serviceType, agentId);
    }

    public class SelfAgentNode implements Node {
        private final String applicationName;
        private final ServiceType serviceType;
        private final String agentId;


        public SelfAgentNode(String applicationName, ServiceType serviceType, String agentId) {
            this.applicationName = applicationName;
            this.serviceType = serviceType;
            this.agentId = agentId;
        }

        @Override
        public RowKey rowkey(long requestTime) {
            long timestamp = timeSlot.getTimeSlot(requestTime);
            return LinkRowKey.of(applicationName, serviceType, timestamp);
        }

        @Override
        public ColumnName histogram(int elapsed, boolean isError) {
            final HistogramSlot slot = getHistogramSchema().findHistogramSlot(elapsed, isError);
            return ResponseColumnName.histogram(agentId, slot.getSlotTime());
        }

        @Override
        public ColumnName sum() {
            HistogramSlot slot = getHistogramSchema().getSumStatSlot();
            return ResponseColumnName.histogram(agentId, slot.getSlotTime());
        }

        @Override
        public ColumnName max() {
            HistogramSlot slot = getHistogramSchema().getMaxStatSlot();
            return ResponseColumnName.histogram(agentId, slot.getSlotTime());
        }

        @Override
        public ColumnName ping() {
            HistogramSlot slot = getHistogramSchema().getPingSlot();
            return ResponseColumnName.histogram(agentId, slot.getSlotTime());
        }

        private HistogramSchema getHistogramSchema() {
            return serviceType.getHistogramSchema();
        }
    }
}
