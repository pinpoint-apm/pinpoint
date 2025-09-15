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

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

import java.util.Objects;

/**
 * @author emeroad
 */
public class UidLinkRowKey implements TimestampRowKey {
    private final int serviceUid;
    private final String applicationName;
    private final int serviceType;
    private final long timestamp;

    public static RowKey of(Vertex vertex, long rowTimeSlot) {
        return new UidLinkRowKey(vertex.serviceUid(), vertex.applicationName(), vertex.serviceType().getCode(), rowTimeSlot);
    }

    public static RowKey of(int serviceUid, String applicationName, ServiceType serviceType, long rowTimeSlot) {
        return new UidLinkRowKey(serviceUid, applicationName, serviceType.getCode(), rowTimeSlot);
    }

    public UidLinkRowKey(int serviceUid, String applicationName, int serviceType, long timestamp) {
        this.serviceUid = serviceUid;
        this.applicationName = Objects.requireNonNull(applicationName, "callApplicationName");
        this.serviceType = serviceType;
        this.timestamp = timestamp;
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

    /**
     * <pre>
     * rowkey format = "APPLICATIONNAME(max 254)" + apptype(4) + serivceUid(4)+ "TIMESTAMP(8)"
     * </pre>
     *
     * @param saltKeySize
     */
    public byte[] getRowKey(int saltKeySize) {
        return makeRowKey(saltKeySize, serviceUid, applicationName, serviceType, timestamp);
    }

    public static byte[] makeRowKey(int saltKeySize, int serviceUid, String applicationName, int serviceType, long timestamp) {
        final Buffer buffer = new AutomaticBuffer(saltKeySize +
                                                  PinpointConstants.UID_SERVICE_NAME_LEN +
                                                  BytesUtils.INT_BYTE_LENGTH +
                                                  BytesUtils.INT_BYTE_LENGTH +
                                                  BytesUtils.LONG_BYTE_LENGTH);
        buffer.setOffset(saltKeySize);
        buffer.putPadString(applicationName, PinpointConstants.UID_SERVICE_NAME_LEN);
        buffer.putInt(serviceType);
        buffer.putInt(serviceUid);
        long reverseTimeMillis = TimeUtils.reverseTimeMillis(timestamp);
        buffer.putLong(reverseTimeMillis);
        return buffer.getBuffer();
    }

    public static UidLinkRowKey read(int saltKey, byte[] bytes) {

        int offset = saltKey;

        String applicationName = BytesUtils.toStringAndRightTrim(bytes, offset, PinpointConstants.UID_SERVICE_NAME_LEN);
        offset += PinpointConstants.UID_SERVICE_NAME_LEN;

        int applicationServiceType = BytesUtils.bytesToInt(bytes, offset);
        offset += BytesUtils.INT_BYTE_LENGTH;

        int serviceUid = BytesUtils.bytesToInt(bytes, offset);
        offset += BytesUtils.INT_BYTE_LENGTH;

        long timestamp = TimeUtils.recoveryTimeMillis(BytesUtils.bytesToLong(bytes, offset));

        return new UidLinkRowKey(serviceUid, applicationName, applicationServiceType, timestamp);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        UidLinkRowKey that = (UidLinkRowKey) o;
        return serviceUid == that.serviceUid && serviceType == that.serviceType && timestamp == that.timestamp && applicationName.equals(that.applicationName);
    }

    @Override
    public int hashCode() {
        int result = serviceUid;
        result = 31 * result + applicationName.hashCode();
        result = 31 * result + serviceType;
        result = 31 * result + Long.hashCode(timestamp);
        return result;
    }

    @Override
    public String toString() {
        return "UidLinkRowKey{" +
               "serviceUid=" + serviceUid +
               ", applicationName='" + applicationName + '\'' +
               ", serviceType=" + serviceType +
               ", timestamp=" + timestamp +
               '}';
    }
}
