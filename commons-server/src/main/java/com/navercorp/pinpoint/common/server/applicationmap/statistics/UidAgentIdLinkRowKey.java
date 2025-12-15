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

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.timeseries.util.IntInverter;
import com.navercorp.pinpoint.common.timeseries.util.SecondTimestamp;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Objects;

/**
 * @author emeroad
 */
public class UidAgentIdLinkRowKey implements TimestampRowKey {
    public static final int KEY_SIZE = 1024;

    public static final int TIMESTAMP_SIZE = BytesUtils.INT_BYTE_LENGTH;
    public static final int PREFIX_SIZE = BytesUtils.INT_BYTE_LENGTH +
                                          BytesUtils.INT_BYTE_LENGTH +
                                          BytesUtils.INT_BYTE_LENGTH +
                                          TIMESTAMP_SIZE;

    private static final HashFunction hashFunction = Hashing.murmur3_32_fixed();

    private final int serviceUid;
    private final String applicationName;
    private final int serviceType;
    private final long timestamp;

    private final String agentId;

    public static RowKey of(Vertex vertex, long rowTimeSlot, String agentId) {

        String applicationName = vertex.applicationName();
        return new UidAgentIdLinkRowKey(vertex.serviceUid(), applicationName, vertex.serviceType().getCode(),
                rowTimeSlot, agentId);
    }

    public static RowKey of(int serviceUid, String applicationName, ServiceType serviceType,
                            long timestamp, String agentId) {
        return new UidAgentIdLinkRowKey(serviceUid, applicationName, serviceType.getCode(), timestamp, agentId);
    }

    public UidAgentIdLinkRowKey(int serviceUid, String applicationName, int serviceType, long timestamp, String agentId) {
        if (requireLength(applicationName) > KEY_SIZE) {
            throw new IllegalArgumentException("applicationName too long:" + applicationName);
        }
        this.serviceUid = serviceUid;
        this.applicationName = applicationName;
        this.serviceType = serviceType;
        this.timestamp = timestamp;

        this.agentId = Objects.requireNonNull(agentId, "agentId");
    }

    private static int requireLength(String applicationName) {
        return Objects.requireNonNull(applicationName, "applicationName").length();
    }

    public int getServiceUid() {
        return serviceUid;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public int getServiceType() {
        return serviceType;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public String getAgentId() {
        return agentId;
    }

    /**
     * <pre>
     * rowkey format = "hash(APPLICATIONNAME)(4)" + serivceUid(4) + apptype(4) + "TIMESTAMP(4) + APPLICATIONNAME"
     * </pre>
     *
     * @param saltKeySize
     */
    public byte[] getRowKey(int saltKeySize) {
        return makeRowKey(saltKeySize, serviceUid, applicationName, serviceType, timestamp, agentId);
    }

    public static byte[] makeRowKey(int saltKeySize, int serviceUid, String applicationName,
                                    int serviceType, long timestamp, String agentId) {
        if (requireLength(applicationName) > KEY_SIZE) {
            throw new IllegalArgumentException("applicationName too long:" + applicationName);
        }

        byte[] applicationNameBytes = BytesUtils.toBytes(applicationName);
        byte[] agentIdBytes = BytesUtils.toBytes(agentId);

        final Buffer buffer = new AutomaticBuffer(saltKeySize +
                                                  PREFIX_SIZE +
                                                  applicationNameBytes.length
                                                  );
        buffer.setOffset(saltKeySize);
        buffer.putInt(hash(applicationNameBytes));
        buffer.putInt(serviceUid);
        buffer.putInt(IntInverter.invert(serviceType));

        int secondTimestamp = SecondTimestamp.convertSecondTimestamp(timestamp);
        int reverseTimeMillis = IntInverter.invert(secondTimestamp);
        buffer.putInt(reverseTimeMillis);

        buffer.putInt(hash(agentIdBytes));
        buffer.putPrefixedBytes(agentIdBytes);

        buffer.putPrefixedBytes(applicationNameBytes);
        return buffer.getBuffer();
    }


    static int hash(byte[] bytes) {
        HashCode hashCode = hashFunction.hashBytes(bytes);
        return hashCode.hashCode();
    }

    public static UidAgentIdLinkRowKey read(int saltKey, byte[] bytes) {

        int offset = saltKey;
        final Buffer buffer = new FixedBuffer(bytes);
        // skip offset & applicationNameHash
        buffer.setOffset(offset + BytesUtils.INT_BYTE_LENGTH);

        int serviceUid = buffer.readInt(); // serviceUid
        int serviceType = IntInverter.restore(buffer.readInt()); // serviceType

        int secondTimestamp = IntInverter.restore(buffer.readInt());
        long msTimestamp = SecondTimestamp.restoreSecondTimestamp(secondTimestamp);

        int agentIdHash = buffer.readInt();
        String agentId = buffer.readPrefixedString();

        String applicationName = buffer.readPrefixedString();
        return new UidAgentIdLinkRowKey(serviceUid, applicationName, serviceType, msTimestamp, agentId);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        UidAgentIdLinkRowKey that = (UidAgentIdLinkRowKey) o;
        return serviceUid == that.serviceUid && serviceType == that.serviceType && timestamp == that.timestamp && Objects.equals(applicationName, that.applicationName) && Objects.equals(agentId, that.agentId);
    }

    @Override
    public int hashCode() {
        int result = serviceUid;
        result = 31 * result + Objects.hashCode(applicationName);
        result = 31 * result + serviceType;
        result = 31 * result + Long.hashCode(timestamp);
        result = 31 * result + Objects.hashCode(agentId);
        return result;
    }

    @Override
    public String toString() {
        return "UidLinkRowKey{" +
               "serviceUid=" + serviceUid +
               ", applicationName='" + applicationName + '\'' +
               ", serviceType=" + serviceType +
               ", timestamp=" + timestamp +
               ", agentId='" + agentId +
               '}';
    }
}
