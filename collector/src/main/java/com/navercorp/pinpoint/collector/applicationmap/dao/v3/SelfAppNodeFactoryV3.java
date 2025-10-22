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

import com.navercorp.pinpoint.collector.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidLinkRowKey;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;

public class SelfAppNodeFactoryV3 implements SelfAppNodeFactory {
    @Override
    public RowKey rowkey(Vertex selfVertex, long rowTimeSlot) {
        return UidLinkRowKey.of(selfVertex, rowTimeSlot);
    }

    @Override
    public ColumnName histogram(HistogramSlot slot) {
        return ApplicationResponseColumnName.histogram(slot.getSlotCode());
    }

    public ColumnName sum(ServiceType selfServiceType) {
        HistogramSlot slot = selfServiceType.getHistogramSchema().getSumStatSlot();
        return histogram(slot);
    }

    public ColumnName max(ServiceType selfServiceType) {
        HistogramSlot slot = selfServiceType.getHistogramSchema().getSumStatSlot();
        return histogram(slot);
    }

}
