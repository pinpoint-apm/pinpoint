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
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Objects;

/**
 * @author emeroad
 */
public class LinkRowKey implements TimestampRowKey {
    private final String applicationName;
    private final short serviceType;
    private final long timestamp;

    public static RowKey of(Vertex vertex, long rowTimeSlot) {
        return new LinkRowKey(vertex.applicationName(), vertex.serviceType().getCode(), rowTimeSlot);
    }

    public static RowKey of(String applicationName, ServiceType serviceType, long rowTimeSlot) {
        return new LinkRowKey(applicationName, serviceType.getCode(), rowTimeSlot);
    }

    LinkRowKey(String applicationName, short serviceType, long timestamp) {
        this.applicationName = Objects.requireNonNull(applicationName, "callApplicationName");
        this.serviceType = serviceType;
        this.timestamp = timestamp;
    }

    @Override
    public byte[] getRowKey(int saltKeySize) {
        return makeRowKey(saltKeySize, applicationName, serviceType, timestamp);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public short getServiceType() {
        return serviceType;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * <pre>
     * rowkey format = "APPLICATIONNAME(max 24bytes)" + apptype(2byte) + "TIMESTAMP(8byte)"
     * </pre>
     *
     * @param applicationName
     * @param timestamp
     * @return
     */
    public static byte[] makeRowKey(int saltKeySize, String applicationName, short applicationType, long timestamp) {
        Objects.requireNonNull(applicationName, "applicationName");

        final byte[] applicationNameBytes = BytesUtils.toBytes(applicationName);

        final Buffer buffer = new AutomaticBuffer(saltKeySize + BytesUtils.SHORT_BYTE_LENGTH
                                                  + applicationNameBytes.length
                                                  + BytesUtils.SHORT_BYTE_LENGTH
                                                  + BytesUtils.LONG_BYTE_LENGTH);
        buffer.skip(saltKeySize);
        buffer.put2PrefixedBytes(applicationNameBytes);
        buffer.putShort(applicationType);
        long reverseTimeMillis = LongInverter.invert(timestamp);
        buffer.putLong(reverseTimeMillis);
        return buffer.getBuffer();
    }

    public static LinkRowKey read(int saltKey, byte[] bytes) {

        int offset = saltKey;

        short length = BytesUtils.bytesToShort(bytes, offset);
        offset += BytesUtils.SHORT_BYTE_LENGTH;

        String applicationName = BytesUtils.toString(bytes, offset, length);
        offset += length;

        short serviceType = BytesUtils.bytesToShort(bytes, offset);
        offset += BytesUtils.SHORT_BYTE_LENGTH;

        long timestamp = LongInverter.restore(BytesUtils.bytesToLong(bytes, offset));
        return new LinkRowKey(applicationName, serviceType, timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        LinkRowKey that = (LinkRowKey) o;
        return serviceType == that.serviceType
               && timestamp == that.timestamp
               && applicationName.equals(that.applicationName);
    }

    @Override
    public int hashCode() {
        int result = applicationName.hashCode();
        result = 31 * result + serviceType;
        result = 31 * result + Long.hashCode(timestamp);
        return result;
    }

    @Override
    public String toString() {
        return "LinkRowKey{" +
               "applicationName='" + applicationName + '\'' +
               ", serviceType=" + serviceType +
               ", timestamp=" + timestamp +
               '}';
    }
}
