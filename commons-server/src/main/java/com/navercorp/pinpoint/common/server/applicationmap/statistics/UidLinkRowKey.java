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
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
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
    private final int linkServiceUid;

    public static RowKey of(int serviceUid, String applicationName, ServiceType serviceType,
                            long rowTimeSlot,
                            int outServiceUid, String outApplicationName, int outServiceType, String outSubLink) {
        return new UidLinkRowKey(serviceUid, applicationName, serviceType.getCode(), rowTimeSlot, outServiceUid, outApplicationName, outServiceType, outSubLink);
    }

    public UidLinkRowKey(int serviceUid, String applicationName, int serviceType, long timestamp,
                         int outServiceUid, String outApplicationName, int outServiceType, String subLink) {
        this.serviceUid = serviceUid;
        this.applicationName = UidPrefix.requireNameLength(applicationName, "applicationName");
        this.serviceType = serviceType;
        this.timestamp = timestamp;

        this.linkServiceUid = outServiceUid;
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

    public int getLinkServiceUid() {
        return linkServiceUid;
    }

    public String getSubLink() {
        return subLink;
    }

    /**
     * <pre>
     * rowkey format = "UidPrefix(16) + outApplicationNameHash(4) + outServiceType(4) + applicationName + outApplicationName + outSubLink + outServiceUid(4)"
     * outServiceUid is always appended at the tail. Legacy rows written before this field omit it; {@link #read} treats a
     * missing tail as {@link ServiceUid#DEFAULT_SERVICE_UID_CODE} (no non-DEFAULT legacy data exists).
     * </pre>
     *
     * @param saltKeySize
     */
    public byte[] getRowKey(int saltKeySize) {
        return makeRowKey(saltKeySize, serviceUid, applicationName, serviceType, timestamp, linkServiceUid, linkApplicationName, linkServiceType, subLink);
    }

    public static byte[] makeRowKey(int saltKeySize, int serviceUid, String applicationName, int serviceType,
                                    long timestamp,
                                    int outServiceUid, String outApplicationName, int outServiceType, String outSubLink) {
        UidPrefix.requireNameLength(applicationName, "applicationName");

        byte[] applicationNameBytes = BytesUtils.toBytes(applicationName);
        byte[] outApplicationNameBytes = BytesUtils.toBytes(outApplicationName);
        byte[] outSubLinkBytes = BytesUtils.toBytes(outSubLink);

        final Buffer buffer = new AutomaticBuffer(saltKeySize +
                                                  UidPrefix.PREFIX_SIZE +

                                                  BytesUtils.INT_BYTE_LENGTH +
                                                  BytesUtils.INT_BYTE_LENGTH +
                                                  BytesUtils.computeSVar32ByteArraySize(applicationNameBytes) +
                                                  BytesUtils.computeSVar32ByteArraySize(outApplicationNameBytes) +
                                                  BytesUtils.computeSVar32ByteArraySize(outSubLinkBytes) +
                                                  BytesUtils.INT_BYTE_LENGTH
        );
        buffer.skip(saltKeySize);
        UidPrefix.writePrefix(buffer, serviceUid, applicationNameBytes, serviceType, timestamp);

        // tail data
        buffer.putInt(UidPrefix.hash(outApplicationNameBytes));
        buffer.putInt(outServiceType);

        buffer.putPrefixedBytes(applicationNameBytes);
        buffer.putPrefixedBytes(outApplicationNameBytes);
        buffer.putPrefixedBytes(outSubLinkBytes);

        // tail-appended link serviceUid (kept last for backward compatibility with legacy rows that omit it)
        buffer.putInt(outServiceUid);

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

        // tail-appended link serviceUid; absent in legacy rows -> assume DEFAULT service (no non-DEFAULT legacy data exists)
        int outServiceUid = getOutServiceUid(buffer);

        return new UidLinkRowKey(serviceUid, applicationName, serviceType,
                timestamp,
                outServiceUid, outApplicationName, outServiceType, outSubLink);
    }

    private static int getOutServiceUid(Buffer buffer) {
        final int remaining = buffer.remaining();
        if (remaining == 0) {
            return ServiceUid.DEFAULT_SERVICE_UID_CODE;
        }
        // fail fast on a truncated tail: readInt() does not check the slice bound
        if (remaining < BytesUtils.INT_BYTE_LENGTH) {
            throw new IllegalArgumentException("truncated outServiceUid tail, remaining:" + remaining);
        }
        return buffer.readInt();
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        UidLinkRowKey that = (UidLinkRowKey) o;
        return serviceUid == that.serviceUid && serviceType == that.serviceType && timestamp == that.timestamp && linkServiceType == that.linkServiceType && linkServiceUid == that.linkServiceUid && Objects.equals(applicationName, that.applicationName) && Objects.equals(linkApplicationName, that.linkApplicationName) && Objects.equals(subLink, that.subLink);
    }

    @Override
    public int hashCode() {
        int result = serviceUid;
        result = 31 * result + Objects.hashCode(applicationName);
        result = 31 * result + serviceType;
        result = 31 * result + Long.hashCode(timestamp);
        result = 31 * result + Objects.hashCode(linkApplicationName);
        result = 31 * result + linkServiceType;
        result = 31 * result + linkServiceUid;
        result = 31 * result + Objects.hashCode(subLink);
        return result;
    }

    @Override
    public String toString() {
        return "UidLinkRowKey{" +
               serviceUid + '/' + applicationName + '/' + serviceType +
               " -> " +
               linkServiceUid + '/' + linkApplicationName + '/' + linkServiceType +
               ", t=" + timestamp +
               ", subLink='" + subLink + '\'' +
               '}';
    }
}
