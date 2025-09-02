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
public class InLinkUidColumnName implements ColumnName {
    private final int selfServiceUid;
    private final long selfApplication;
    private final int selfAppServiceType;

    // called or calling host
    private final String selfSubLink;
    private final short columnSlotNumber;

    public static ColumnName histogram(int selfServiceUid, long selfApplication, ServiceType selfAppServiceType, String selfSubLink, short columnSlotNumber) {
        return new InLinkUidColumnName(selfServiceUid, selfApplication, selfAppServiceType.getCode(), selfSubLink, columnSlotNumber);
    }

    public static ColumnName sum(int selfServiceUid, long selfApplication, ServiceType selfServiceType, String selfSubLink, ServiceType outAppServiceType) {
        final short slotTime = outAppServiceType.getHistogramSchema().getSumStatSlot().getSlotTime();
        return histogram(selfServiceUid, selfApplication, selfServiceType, selfSubLink, slotTime);
    }


    public static ColumnName max(int selfServiceUid, long selfApplication, ServiceType selfServiceType, String selfSubLink, ServiceType outAppServiceType) {
        final short slotTime = outAppServiceType.getHistogramSchema().getMaxStatSlot().getSlotTime();
        return histogram(selfServiceUid, selfApplication, selfServiceType, selfSubLink, slotTime);
    }

    public InLinkUidColumnName(int selfServiceUid, long selfApplication, int selfAppServiceType, String selfSubLink, short columnSlotNumber) {
        this.selfServiceUid = selfServiceUid;
        this.selfApplication = selfApplication;
        this.selfAppServiceType = selfAppServiceType;
        this.selfSubLink = Objects.requireNonNull(selfSubLink, "selfSubLink");
        this.columnSlotNumber = columnSlotNumber;
    }


    public byte[] getColumnName() {
        final Buffer buffer = new AutomaticBuffer(64);
        buffer.putInt(selfServiceUid);
        buffer.putLong(selfApplication);
        buffer.putInt(selfAppServiceType);
        buffer.putPrefixedString(selfSubLink);
        buffer.putShort(columnSlotNumber);
        return buffer.getBuffer();
    }

}
