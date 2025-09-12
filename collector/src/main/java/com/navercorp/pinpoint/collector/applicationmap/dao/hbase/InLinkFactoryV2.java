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

import com.navercorp.pinpoint.collector.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.collector.applicationmap.statistics.InLinkV2ColumnName;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.LinkRowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.common.trace.ServiceType;

public class InLinkFactoryV2 implements InLinkFactory {
    @Override
    public RowKey rowkey(Vertex vertex, long rowTimeSlot) {
        return LinkRowKey.of(vertex, rowTimeSlot);
    }

    @Override
    public ColumnName histogram(Vertex selfVertex, String selfHost, short outSlotNumber) {
        return InLinkV2ColumnName.histogram(selfVertex, selfHost, outSlotNumber);
    }

    @Override
    public ColumnName sum(Vertex selfVertex, String selfHost, ServiceType inServiceType) {
        return InLinkV2ColumnName.sum(selfVertex, selfHost, inServiceType);
    }

    @Override
    public ColumnName max(Vertex selfVertex, String selfHost, ServiceType inServiceType) {
        return InLinkV2ColumnName.max(selfVertex, selfHost, inServiceType);
    }
}
