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
import com.navercorp.pinpoint.collector.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidLinkRowKey;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;

public class InLinkFactoryV3 implements InLinkFactory {
    @Override
    public RowKey rowkey(Vertex vertex, long rowTimeSlot) {
        return UidLinkRowKey.of(vertex, rowTimeSlot);
    }

    @Override
    public ColumnName histogram(Vertex selfVertex, String selfHost, HistogramSlot outSlot) {
        return InLinkV3ColumnName.histogram(selfVertex, selfHost, outSlot.getSlotTime());
    }

    @Override
    public ColumnName sum(Vertex selfVertex, String selfHost, ServiceType inServiceType) {
        short sumStatSlot = inServiceType.getHistogramSchema().getSumStatSlot().getSlotTime();
        return InLinkV3ColumnName.histogram(selfVertex, selfHost, sumStatSlot);
    }

    @Override
    public ColumnName max(Vertex selfVertex, String selfHost, ServiceType inServiceType) {
        short maxStatSlot = inServiceType.getHistogramSchema().getMaxStatSlot().getSlotTime();
        return InLinkV3ColumnName.histogram(selfVertex, selfHost, maxStatSlot);
    }
}
