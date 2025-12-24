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
public class UidAgentRowKey implements UidRowKey {

    private final int serviceUid;
    private final String applicationName;
    private final int serviceType;
    private final long timestamp;

    private final String agentId;

    public static RowKey of(Vertex vertex, long rowTimeSlot, String agentId) {

        String applicationName = vertex.applicationName();
        return new UidAgentRowKey(vertex.serviceUid(), applicationName, vertex.serviceType().getCode(),
                rowTimeSlot, agentId);
    }

    public static RowKey of(int serviceUid, String applicationName, ServiceType serviceType,
                            long timestamp, String agentId) {
        return new UidAgentRowKey(serviceUid, applicationName, serviceType.getCode(), timestamp, agentId);
    }

    public UidAgentRowKey(int serviceUid, String applicationName, int serviceType, long timestamp, String agentId) {
        this.serviceUid = serviceUid;
        this.applicationName = UidPrefix.requireNameLength(applicationName, "applicationName");
        this.serviceType = serviceType;
        this.timestamp = timestamp;

        this.agentId = Objects.requireNonNull(agentId, "agentId");
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

    public String getAgentId() {
        return agentId;
    }

    /**
     * <pre>
     * rowkey format = "UidPrefix(16) + AgentIdHash(4) + applicationName + agentId"
     * </pre>
     *
     * @param saltKeySize
     */
    public byte[] getRowKey(int saltKeySize) {
        return makeRowKey(saltKeySize, serviceUid, applicationName, serviceType, timestamp, agentId);
    }

    public static byte[] makeRowKey(int saltKeySize, int serviceUid, String applicationName,
                                    int serviceType, long timestamp, String agentId) {
        UidPrefix.requireNameLength(applicationName, "applicationName");

        byte[] applicationNameBytes = BytesUtils.toBytes(applicationName);
        byte[] agentIdBytes = BytesUtils.toBytes(agentId);

        final Buffer buffer = new AutomaticBuffer(saltKeySize +
                                                  UidPrefix.PREFIX_SIZE +

                                                  BytesUtils.INT_BYTE_LENGTH +
                                                  BytesUtils.computeVar32ByteArraySize(applicationNameBytes) +
                                                  BytesUtils.computeVar32ByteArraySize(agentIdBytes)
        );
        buffer.skip(saltKeySize);

        UidPrefix.writePrefix(buffer, serviceUid, applicationNameBytes, serviceType, timestamp);

        buffer.putInt(UidPrefix.hash(agentIdBytes));

        buffer.putPrefixedBytes(applicationNameBytes);
        buffer.putPrefixedBytes(agentIdBytes);

        return buffer.getBuffer();
    }


    public static UidAgentRowKey read(int saltKey, byte[] bytes) {
        return read(saltKey, bytes, 0, bytes.length);
    }

    public static UidAgentRowKey read(int saltKey, byte[] bytes, int offset, int length) {

        final Buffer buffer = new OffsetFixedBuffer(bytes, offset, length);
        // skip offset & applicationNameHash
        buffer.skip(saltKey);

        UidPrefix prefix = UidPrefix.readPrefix(buffer);

        int serviceUid = prefix.getServiceUid(); // serviceUid
        int serviceType = prefix.getServiceType(); // serviceType

        long timestamp = prefix.getTimestamp();

        // agentIdHash
        buffer.skip(BytesUtils.INT_BYTE_LENGTH);

        String applicationName = buffer.readPrefixedString();
        String agentId = buffer.readPrefixedString();

        return new UidAgentRowKey(serviceUid, applicationName, serviceType, timestamp, agentId);
    }



    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        UidAgentRowKey that = (UidAgentRowKey) o;
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
        return "UidAgentRowKey{" +
               "serviceUid=" + serviceUid +
               ", applicationName='" + applicationName + '\'' +
               ", serviceType=" + serviceType +
               ", timestamp=" + timestamp +
               ", agentId='" + agentId +
               '}';
    }
}
