/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.profiler.util;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

/**
 * <pre>
 * columnName format = SERVICETYPE(2bytes) + SLOT(2bytes) + APPNAMELEN(2bytes) + APPLICATIONNAME(str) + HOST(str)
 * </pre>
 *
 * @author netspider
 * @author emeroad
 */
public class ApplicationMapStatisticsUtils {
    private ApplicationMapStatisticsUtils() {
    }

    public static byte[] makeColumnName(short serviceType, String applicationName, String destHost, short slotNumber) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName");
        }
        if (destHost == null) {
            // throw new NullPointerException("destHost");
            destHost = "";
        }
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


    public static byte[] makeColumnName(String agentId, short columnSlotNumber) {
        if (agentId == null) {
            // null check ??
            agentId = "";
        }
        final Buffer buffer = new AutomaticBuffer(agentId.length() + BytesUtils.SHORT_BYTE_LENGTH);
        buffer.putShort(columnSlotNumber);

        final byte[] agentIdBytes = BytesUtils.toBytes(agentId);
        buffer.putBytes(agentIdBytes);

        return buffer.getBuffer();
    }


    private static short findResponseHistogramSlotNo(ServiceType serviceType, int elapsed, boolean isError) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType");
        }
        final HistogramSchema histogramSchema = serviceType.getHistogramSchema();
        final HistogramSlot histogramSlot = histogramSchema.findHistogramSlot(elapsed, isError);
        return histogramSlot.getSlotTime();
    }

    public static short getDestServiceTypeFromColumnName(byte[] bytes) {
        return BytesUtils.bytesToShort(bytes, 0);
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
        return BytesUtils.bytesToShort(bytes, 2);
    }

    public static String getDestApplicationNameFromColumnName(byte[] bytes) {
        final short length = BytesUtils.bytesToShort(bytes, 4);
        return BytesUtils.toStringAndRightTrim(bytes, 6, length);
    }
    
    public static String getDestApplicationNameFromColumnNameForUser(byte[] bytes, ServiceType destServiceType) {
        String destApplicationName = getDestApplicationNameFromColumnName(bytes);
        String destServiceTypeName = destServiceType.getName();
        return destApplicationName + "_" + destServiceTypeName;
    }
    
    public static String getHost(byte[] bytes) {
        int offset = 6 + BytesUtils.bytesToShort(bytes, 4);

        if (offset == bytes.length) {
            return null;
        }
        return BytesUtils.toStringAndRightTrim(bytes, offset, bytes.length - offset);
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
    public static byte[] makeRowKey(String applicationName, short applicationType, long timestamp) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName");
        }
        final byte[] applicationNameBytes= BytesUtils.toBytes(applicationName);

        final Buffer buffer = new AutomaticBuffer(2 + applicationNameBytes.length + 2 + 8);
//        buffer.put2PrefixedString(applicationName);
        buffer.putShort((short)applicationNameBytes.length);
        buffer.putBytes(applicationNameBytes);
        buffer.putShort(applicationType);
        long reverseTimeMillis = TimeUtils.reverseTimeMillis(timestamp);
        buffer.putLong(reverseTimeMillis);
        return buffer.getBuffer();
    }

    public static String getApplicationNameFromRowKey(byte[] bytes, int offset) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        short applicationNameLength = BytesUtils.bytesToShort(bytes, offset);
        return BytesUtils.toString(bytes, offset + 2, applicationNameLength); //.trim();
    }

    public static String getApplicationNameFromRowKey(byte[] bytes) {
        return getApplicationNameFromRowKey(bytes, 0);
    }

    public static short getApplicationTypeFromRowKey(byte[] bytes) {
        return getApplicationTypeFromRowKey(bytes, 0);
    }

    public static short getApplicationTypeFromRowKey(byte[] bytes, int offset) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        short applicationNameLength = BytesUtils.bytesToShort(bytes, offset);
        return BytesUtils.bytesToShort(bytes, offset + applicationNameLength + 2);
    }

    public static long getTimestampFromRowKey(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        short applicationNameLength = BytesUtils.bytesToShort(bytes, 0);
        return TimeUtils.recoveryTimeMillis(BytesUtils.bytesToLong(bytes, applicationNameLength + 4));
    }
}
