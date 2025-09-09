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

package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

import java.util.Objects;

/**
 * <pre>
 * columnName format = SERVICETYPE(2bytes) + SLOT(2bytes) + APPNAMELEN(2bytes) + APPLICATIONNAME(str) + HOST(str)
 * </pre>
 *
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 */
public class ApplicationMapStatisticsUtils {
    private ApplicationMapStatisticsUtils() {
    }

    public static byte[] makeColumnName(short serviceType, String applicationName, String destHost, short slotNumber) {
        Objects.requireNonNull(applicationName, "applicationName");
        destHost = Objects.toString(destHost, "");

        // approximate size of destHost
        final Buffer buffer = new AutomaticBuffer(BytesUtils.SHORT_BYTE_LENGTH + PinpointConstants.APPLICATION_NAME_MAX_LEN + destHost.length() + BytesUtils.SHORT_BYTE_LENGTH);
        buffer.putShort(serviceType);
        buffer.putShort(slotNumber);
        buffer.put2PrefixedString(applicationName);
        buffer.putBytes(BytesUtils.toBytes(destHost));
        return buffer.getBuffer();
    }

    public static short getSlotNumber(ServiceType serviceType, int elapsed, boolean isError) {
        return findResponseHistogramSlotNo(serviceType, elapsed, isError);
    }

    /**
     * @deprecated Since 3.1.0. Use {@link #getPingSlotNumber(ServiceType)} instead.
     */
    @Deprecated
    public static short getPingSlotNumber(ServiceType serviceType, int elapsed, boolean isError) {
        return getPingSlotNumber(serviceType);
    }

    public static short getPingSlotNumber(ServiceType serviceType) {
        final HistogramSchema histogramSchema = serviceType.getHistogramSchema();
        return histogramSchema.getPingSlot().getSlotTime();
    }

    public static byte[] makeColumnName(String agentId, short columnSlotNumber) {
        Objects.requireNonNull(agentId, "agentId");

        final Buffer buffer = new AutomaticBuffer(agentId.length() + BytesUtils.SHORT_BYTE_LENGTH);
        buffer.putShort(columnSlotNumber);

        final byte[] agentIdBytes = BytesUtils.toBytes(agentId);
        buffer.putBytes(agentIdBytes);

        return buffer.getBuffer();
    }


    private static short findResponseHistogramSlotNo(ServiceType serviceType, int elapsed, boolean isError) {
        Objects.requireNonNull(serviceType, "serviceType");

        final HistogramSchema histogramSchema = serviceType.getHistogramSchema();
        final HistogramSlot histogramSlot = histogramSchema.findHistogramSlot(elapsed, isError);
        return histogramSlot.getSlotTime();
    }

    public static short getDestServiceTypeFromColumnName(byte[] bytes) {
        return ByteArrayUtils.bytesToShort(bytes, 0);
    }

    /**
     * @param bytes
     * @return <pre>
     *         0 > : ms
     *         0 : slow
     *         -1 : error
     *         </pre>
     */
    public static short getHistogramSlotFromColumnName(byte[] bytes) {
        return ByteArrayUtils.bytesToShort(bytes, 2);
    }

    public static String getDestApplicationNameFromColumnName(byte[] bytes) {
        final short length = ByteArrayUtils.bytesToShort(bytes, 4);
        return BytesUtils.toStringAndRightTrim(bytes, 6, length);
    }

    /**
     * @deprecated Since 3.1.0. Use {@link UserNodeUtils#newUserNodeName(String, ServiceType)} instead.
     */
    @Deprecated
    public static String getDestApplicationNameFromColumnNameForUser(byte[] bytes, ServiceType destServiceType) {
        String destApplicationName = getDestApplicationNameFromColumnName(bytes);
        String destServiceTypeName = destServiceType.getName();
        return destApplicationName + "_" + destServiceTypeName;
    }

    public static String getHost(byte[] bytes) {
        int offset = 6 + ByteArrayUtils.bytesToShort(bytes, 4);

        if (offset == bytes.length) {
            return null;
        }
        return BytesUtils.toStringAndRightTrim(bytes, offset, bytes.length - offset);
    }



    public static String getApplicationNameFromRowKey(byte[] bytes, int offset) {
        Objects.requireNonNull(bytes, "bytes");

        short applicationNameLength = ByteArrayUtils.bytesToShort(bytes, offset);
        return BytesUtils.toString(bytes, offset + 2, applicationNameLength); //.trim();
    }

    public static String getApplicationNameFromRowKey(byte[] bytes) {
        return getApplicationNameFromRowKey(bytes, 0);
    }

    public static short getApplicationTypeFromRowKey(byte[] bytes) {
        return getApplicationTypeFromRowKey(bytes, 0);
    }

    public static short getApplicationTypeFromRowKey(byte[] bytes, int offset) {
        Objects.requireNonNull(bytes, "bytes");

        short applicationNameLength = ByteArrayUtils.bytesToShort(bytes, offset);
        return ByteArrayUtils.bytesToShort(bytes, offset + applicationNameLength + 2);
    }

    public static long getTimestampFromRowKey(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");

        short applicationNameLength = ByteArrayUtils.bytesToShort(bytes, 0);
        return TimeUtils.recoveryTimeMillis(ByteArrayUtils.bytesToLong(bytes, applicationNameLength + 4));
    }
}
