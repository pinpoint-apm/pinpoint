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

package com.navercorp.pinpoint.collector.applicationmap.statistics.uid;

import com.navercorp.pinpoint.collector.applicationmap.SelfUidVertex;
import com.navercorp.pinpoint.collector.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.hbase.wd.SaltKey;
import com.navercorp.pinpoint.common.server.uid.UidRowKeyUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.TimeUtils;

import java.util.Objects;

/**
 * @author emeroad
 */
public record UidLinkRowKey(int service, long application, int serviceType, long rowTimeSlot) implements RowKey {
    public static RowKey of(SelfUidVertex vertex, long rowTimeSlot) {
        return of(vertex.service(), vertex.application(), vertex.serviceType(), rowTimeSlot);
    }

    public static RowKey of(int service, long application, ServiceType serviceType, long rowTimeSlot) {
        return new UidLinkRowKey(service, application, serviceType.getCode(), rowTimeSlot);
    }

    @Override
    public byte[] getRowKey(int saltKeySize) {
        return UidRowKeyUtils.writeRowKey(saltKeySize, this.service, this.application, this.serviceType, this.rowTimeSlot);
    }


    public static UidLinkRowKey of(SaltKey saltKey, byte[] rowKey) {
        Objects.requireNonNull(saltKey, "saltKey");

        int offset = saltKey.size();

        long applicationUid = ByteArrayUtils.bytesToLong(rowKey, offset);
        offset += ByteArrayUtils.LONG_BYTE_LENGTH;

        int serviceType = ByteArrayUtils.bytesToInt(rowKey, offset);
        offset += ByteArrayUtils.INT_BYTE_LENGTH;

        int serviceUid = ByteArrayUtils.bytesToInt(rowKey, offset);
        offset += ByteArrayUtils.INT_BYTE_LENGTH;

        long timestamp = ByteArrayUtils.bytesToLong(rowKey, offset);
        timestamp = TimeUtils.reverseTimeMillis(timestamp);

        return new UidLinkRowKey(serviceUid, applicationUid, serviceType, timestamp);
    }


}
