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
public class UidLinkRowKey implements UidRowKey {

    private final int serviceUid;
    private final String applicationName;
    private final int serviceType;
    private final long timestamp;

    // tail data
    private final String linkApplicationName;
    private final int linkServiceType;
    private final String subLink;

    public static RowKey of(Vertex selfVertex, long rowTimeSlot, String outApplicationName, int outServiceType, String outSubLink) {

        String applicationName = selfVertex.applicationName();
        return new UidLinkRowKey(selfVertex.serviceUid(), applicationName, selfVertex.serviceType().getCode(), rowTimeSlot, outApplicationName, outServiceType, outSubLink);
    }

    public static RowKey of(int serviceUid, String applicationName, ServiceType serviceType, long rowTimeSlot,
                            String outApplicationName, int outServiceType, String outSubLink) {
        return new UidLinkRowKey(serviceUid, applicationName, serviceType.getCode(), rowTimeSlot, outApplicationName, outServiceType, outSubLink);
    }

    public UidLinkRowKey(int serviceUid, String applicationName, int serviceType, long timestamp,
                         String outApplicationName, int outServiceType, String subLink) {
        this.serviceUid = serviceUid;
        this.applicationName = UidPrefix.requireNameLength(applicationName, "applicationName");
        this.serviceType = serviceType;
        this.timestamp = timestamp;

        this.linkApplicationName = Objects.requireNonNull(outApplicationName, "linkApplicationName");
        this.linkServiceType = outServiceType;
        this.subLink = Objects.requireNonNull(subLink, "outSubLink");
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

    public String getLinkApplicationName() {
        return linkApplicationName;
    }

    public int getLinkServiceType() {
        return linkServiceType;
    }

    public String getSubLink() {
        return subLink;
    }

    /**
     * <pre>
     * rowkey format = "UidPrefix(16) + outApplicationNameHash(4) + outServiceType(4) + applicationName + outApplicationName + outSubLink"
     * </pre>
     *
     * @param saltKeySize
     */
    public byte[] getRowKey(int saltKeySize) {
        return makeRowKey(saltKeySize, serviceUid, applicationName, serviceType, timestamp, linkApplicationName, linkServiceType, subLink);
    }

    public static byte[] makeRowKey(int saltKeySize, int serviceUid, String applicationName, int serviceType, long timestamp,
                                    String outApplicationName, int outServiceType, String outSubLink) {
        UidPrefix.requireNameLength(applicationName, "applicationName");

        byte[] applicationNameBytes = BytesUtils.toBytes(applicationName);
        byte[] outApplicationNameBytes = BytesUtils.toBytes(outApplicationName);
        byte[] outSubLinkBytes = BytesUtils.toBytes(outSubLink);

        final Buffer buffer = new AutomaticBuffer(saltKeySize +
                                                  UidPrefix.PREFIX_SIZE +

                                                  BytesUtils.INT_BYTE_LENGTH +
                                                  BytesUtils.INT_BYTE_LENGTH +
                                                  BytesUtils.computeVar32ByteArraySize(applicationNameBytes) +
                                                  BytesUtils.computeVar32ByteArraySize(outApplicationNameBytes) +
                                                  BytesUtils.computeVar32ByteArraySize(outSubLinkBytes)
        );
        buffer.skip(saltKeySize);
        UidPrefix.writePrefix(buffer, serviceUid, applicationNameBytes, serviceType, timestamp);

        // tail data
        buffer.putInt(UidPrefix.hash(outApplicationNameBytes));
        buffer.putInt(outServiceType);

        buffer.putPrefixedBytes(applicationNameBytes);
        buffer.putPrefixedBytes(outApplicationNameBytes);
        buffer.putPrefixedBytes(outSubLinkBytes);

        return buffer.getBuffer();
    }


    public static UidLinkRowKey read(int saltKey, byte[] bytes) {
        return read(saltKey, bytes, 0, bytes.length);
    }

    public static UidLinkRowKey read(int saltKey, byte[] bytes, int offset, int length) {

        final Buffer buffer = new OffsetFixedBuffer(bytes, offset, length);
        // skip offset & applicationNameHash
        buffer.skip(saltKey);

        UidPrefix prefix = UidPrefix.readPrefix(buffer);
        int serviceUid = prefix.getServiceUid(); // serviceUid
        int serviceType = prefix.getServiceType(); // serviceType

        long timestamp = prefix.getTimestamp();

        // outApplicationNameHash
        buffer.skip(BytesUtils.INT_BYTE_LENGTH);
        int outServiceType = buffer.readInt();

        String applicationName = buffer.readPrefixedString();
        String outApplicationName = buffer.readPrefixedString();
        String outSubLink = buffer.readPrefixedString();


        return new UidLinkRowKey(serviceUid, applicationName, serviceType, timestamp, outApplicationName, outServiceType, outSubLink);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        UidLinkRowKey that = (UidLinkRowKey) o;
        return serviceUid == that.serviceUid && serviceType == that.serviceType && timestamp == that.timestamp && linkServiceType == that.linkServiceType && Objects.equals(applicationName, that.applicationName) && Objects.equals(linkApplicationName, that.linkApplicationName) && Objects.equals(subLink, that.subLink);
    }

    @Override
    public int hashCode() {
        int result = serviceUid;
        result = 31 * result + Objects.hashCode(applicationName);
        result = 31 * result + serviceType;
        result = 31 * result + Long.hashCode(timestamp);
        result = 31 * result + Objects.hashCode(linkApplicationName);
        result = 31 * result + linkServiceType;
        result = 31 * result + Objects.hashCode(subLink);
        return result;
    }

    @Override
    public String toString() {
        return "UidLinkRowKey{" +
               "serviceUid=" + serviceUid +
               ", applicationName='" + applicationName + '\'' +
               ", serviceType=" + serviceType +
               ", timestamp=" + timestamp +
               ", linkApplicationName='" + linkApplicationName + '\'' +
               ", linkServiceType=" + linkServiceType +
               ", outSubLink='" + subLink + '\'' +
               '}';
    }
}
