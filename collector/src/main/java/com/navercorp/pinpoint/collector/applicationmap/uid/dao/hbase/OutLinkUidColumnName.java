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

package com.navercorp.pinpoint.collector.applicationmap.uid.dao.hbase;

import com.navercorp.pinpoint.collector.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

/**
 * @author emeroad
 */
public class OutLinkUidColumnName implements ColumnName {
    private final String outApplication;
    private final int outAppServiceType;
    // called or calling host
    private final String outSubLink;
    private final short columnSlotNumber;

    public static ColumnName histogram(String outApplication, ServiceType outAppServiceType, String outHost, short columnSlotNumber) {
        return new OutLinkUidColumnName(outApplication, outAppServiceType.getCode(), outHost, columnSlotNumber);
    }

    public static ColumnName sum(String outApplication, ServiceType inServiceType, String outHost, ServiceType outAppServiceType) {
        final short slotTime = outAppServiceType.getHistogramSchema().getSumStatSlot().getSlotTime();
        return histogram(outApplication, inServiceType, outHost, slotTime);
    }


    public static ColumnName max(String outApplication, ServiceType outServiceType, String outHost, ServiceType outAppServiceType) {
        final short slotTime = outAppServiceType.getHistogramSchema().getMaxStatSlot().getSlotTime();
        return histogram(outApplication, outServiceType, outHost, slotTime);
    }

    public OutLinkUidColumnName(String outApplication, int outAppServiceType, String outSubLink, short columnSlotNumber) {
        this.outApplication = Objects.requireNonNull(outApplication, "outApplication");
        this.outAppServiceType = outAppServiceType;
        this.outSubLink = Objects.requireNonNull(outSubLink, "outSubLink");
        this.columnSlotNumber = columnSlotNumber;
    }


    public byte[] getColumnName() {
        final Buffer buffer = new AutomaticBuffer(64);
        buffer.putInt(outAppServiceType);
        buffer.putPrefixedString(outApplication);
        buffer.putPrefixedString(outSubLink);
        buffer.putShort(columnSlotNumber);
        return buffer.getBuffer();
    }

}
