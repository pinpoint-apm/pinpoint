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

package com.navercorp.pinpoint.common.server.applicationmap.statistics;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.util.ByteUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

import java.util.Objects;

/**
 * @author emeroad
 */
public class UidLinkRowKey implements RowKey {
    private final int serviceUid;
    private final String applicationName;
    private final short serviceType;
    private final long rowTimeSlot;

    public static RowKey of(Vertex vertex, long rowTimeSlot) {
        return new UidLinkRowKey(vertex.serviceUid(), vertex.applicationName(), vertex.serviceType().getCode(), rowTimeSlot);
    }

    public static RowKey of(int serviceUid, String applicationName, ServiceType serviceType, long rowTimeSlot) {
        return new UidLinkRowKey(serviceUid, applicationName, serviceType.getCode(), rowTimeSlot);
    }

    UidLinkRowKey(int serviceUid, String applicationName, short serviceType, long rowTimeSlot) {
        this.serviceUid = serviceUid;
        this.applicationName = Objects.requireNonNull(applicationName, "callApplicationName");
        this.serviceType = serviceType;
        this.rowTimeSlot = rowTimeSlot;
    }

    public byte[] getRowKey(int saltKeySize) {
        return makeRowKey(saltKeySize, serviceUid, applicationName, serviceType, rowTimeSlot);
    }

    /**
     * <pre>
     * rowkey format = "APPLICATIONNAME(max 255bytes)" + apptype(2byte) + "TIMESTAMP(8byte)"
     * </pre>
     *
     * @param applicationName
     * @param timestamp
     * @return
     */
    public static byte[] makeRowKey(int saltKeySize, int serviceUid, String applicationName, short applicationType, long timestamp) {
        Objects.requireNonNull(applicationName, "applicationName");

        final byte[] applicationNameBytes = BytesUtils.toBytes(applicationName);

        final Buffer buffer = new AutomaticBuffer(saltKeySize + 1 +
                                                  applicationNameBytes.length +
                                                  BytesUtils.SHORT_BYTE_LENGTH +
                                                  BytesUtils.INT_BYTE_LENGTH +
                                                  BytesUtils.LONG_BYTE_LENGTH);
        buffer.setOffset(saltKeySize);
        buffer.putByte(ByteUtils.toUnsignedByte(applicationNameBytes.length));
        buffer.putBytes(applicationNameBytes);
        buffer.putShort(applicationType);
        buffer.putInt(serviceUid);
        long reverseTimeMillis = TimeUtils.reverseTimeMillis(timestamp);
        buffer.putLong(reverseTimeMillis);
        return buffer.getBuffer();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        UidLinkRowKey that = (UidLinkRowKey) o;
        return serviceUid == that.serviceUid && serviceType == that.serviceType && rowTimeSlot == that.rowTimeSlot && applicationName.equals(that.applicationName);
    }

    @Override
    public int hashCode() {
        int result = serviceUid;
        result = 31 * result + applicationName.hashCode();
        result = 31 * result + serviceType;
        result = 31 * result + Long.hashCode(rowTimeSlot);
        return result;
    }

    @Override
    public String toString() {
        return "UidLinkRowKey{" +
                "serviceUid=" + serviceUid +
                ", applicationName='" + applicationName + '\'' +
                ", serviceType=" + serviceType +
                ", rowTimeSlot=" + rowTimeSlot +
                '}';
    }
}
