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
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Objects;

/**
 * @author emeroad
 */
public class UidAppRowKey implements UidRowKey {

    private final int serviceUid;
    private final String applicationName;
    private final int serviceType;
    private final long timestamp;

    public static RowKey of(Vertex selfVertex, long rowTimeSlot) {

        String applicationName = selfVertex.applicationName();
        return new UidAppRowKey(selfVertex.serviceUid(), applicationName, selfVertex.serviceType().getCode(), rowTimeSlot);
    }

    public static RowKey of(int serviceUid, String applicationName, ServiceType serviceType, long rowTimeSlot) {
        return new UidAppRowKey(serviceUid, applicationName, serviceType.getCode(), rowTimeSlot);
    }

    public UidAppRowKey(int serviceUid, String applicationName, int serviceType, long timestamp) {
        this.serviceUid = serviceUid;
        this.applicationName = UidPrefix.requireNameLength(applicationName, "applicationName");
        this.serviceType = serviceType;
        this.timestamp = timestamp;
    }


    @Override
    public int getServiceUid() {
        return serviceUid;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public int getServiceType() {
        return serviceType;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * <pre>
     * rowkey format = "UidPrefix(16) + applicationName"
     * </pre>
     *
     * @param saltKeySize
     */
    public byte[] getRowKey(int saltKeySize) {
        return makeRowKey(saltKeySize, serviceUid, applicationName, serviceType, timestamp);
    }

    public static byte[] makeRowKey(int saltKeySize, int serviceUid, String applicationName, int serviceType, long timestamp) {
        UidPrefix.requireNameLength(applicationName, "applicationName");

        byte[] applicationNameBytes = BytesUtils.toBytes(applicationName);

        final Buffer buffer = new AutomaticBuffer(saltKeySize +
                                                  UidPrefix.PREFIX_SIZE +
                                                  BytesUtils.computeVar32ByteArraySize(applicationNameBytes)
        );
        buffer.skip(saltKeySize);

        UidPrefix.writePrefix(buffer, serviceUid, applicationNameBytes, serviceType, timestamp);

        // tail data
        buffer.putPrefixedBytes(applicationNameBytes);
        return buffer.getBuffer();
    }

    public static UidAppRowKey read(int saltKey, byte[] bytes) {
        return read(saltKey, bytes, 0, bytes.length);
    }

    public static UidAppRowKey read(int saltKey, byte[] bytes, int offset, int length) {

        final Buffer buffer = new OffsetFixedBuffer(bytes, offset, length);
        // skip offset & applicationNameHash
        buffer.skip(saltKey);

        UidPrefix prefix = UidPrefix.readPrefix(buffer);
        int serviceUid = prefix.getServiceUid(); // serviceUid
        int serviceType = prefix.getServiceType(); // serviceType

        long timestamp = prefix.getTimestamp();

        // tail data
        String applicationName = buffer.readPrefixedString();
        return new UidAppRowKey(serviceUid, applicationName, serviceType, timestamp);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        UidAppRowKey that = (UidAppRowKey) o;
        return serviceUid == that.serviceUid && serviceType == that.serviceType && timestamp == that.timestamp && Objects.equals(applicationName, that.applicationName);
    }

    @Override
    public int hashCode() {
        int result = serviceUid;
        result = 31 * result + Objects.hashCode(applicationName);
        result = 31 * result + serviceType;
        result = 31 * result + Long.hashCode(timestamp);
        return result;
    }

    @Override
    public String toString() {
        return "UidAppRowKey{" +
               "serviceUid=" + serviceUid +
               ", applicationName='" + applicationName + '\'' +
               ", serviceType=" + serviceType +
               ", timestamp=" + timestamp +
               '}';
    }
}
