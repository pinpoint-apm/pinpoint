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

package com.navercorp.pinpoint.collector.applicationmap.dao.hbase;

import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.LinkRowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.v2.OutLinkV2ColumnName;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;

public class OutLinkFactoryV2 implements OutLinkFactory {
    @Override
    public RowKey rowkey(Vertex vertex, long rowTimeSlot) {
        return LinkRowKey.of(vertex, rowTimeSlot);
    }

    @Override
    public ColumnName histogram(String selfAgentId, Vertex outVertex, String outSubLink, HistogramSlot outSlot) {
        return OutLinkV2ColumnName.histogram(selfAgentId, outVertex, outSubLink, outSlot.getSlotTime());
    }

    @Override
    public ColumnName sum(String selfAgentId, Vertex outVertex, String outHost, ServiceType selfServiceType) {
        final short slotTime = selfServiceType.getHistogramSchema().getSumStatSlot().getSlotTime();
        return OutLinkV2ColumnName.histogram(selfAgentId, outVertex, outHost, slotTime);
    }

    @Override
    public ColumnName max(String selfAgentId, Vertex outVertex, String outHost, ServiceType selfServiceType) {
        final short slotTime = selfServiceType.getHistogramSchema().getMaxStatSlot().getSlotTime();
        return OutLinkV2ColumnName.histogram(selfAgentId, outVertex, outHost, slotTime);
    }
}
