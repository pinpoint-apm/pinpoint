/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.common.server.applicationmap.util;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

import java.util.Objects;

/**
 * @author intr3p1d
 */
public class ApplicationMapUtils {

    private ApplicationMapUtils() {
    }

    public static byte[] makeRowKey(
            String serviceName,
            String applicationName, short applicationType,
            long timestamp
    ) {
        Objects.requireNonNull(serviceName, "serviceName");
        Objects.requireNonNull(applicationName, "applicationName");

        final byte[] serviceNameBytes = BytesUtils.toBytes(serviceName);
        final byte[] applicationNameBytes = BytesUtils.toBytes(applicationName);

        final Buffer buffer = new AutomaticBuffer(64);
        buffer.putShort((short) serviceNameBytes.length);
        buffer.putBytes(serviceNameBytes);
        buffer.putShort((short) applicationNameBytes.length);
        buffer.putBytes(applicationNameBytes);
        buffer.putShort(applicationType);
        long reverseTimeMillis = TimeUtils.reverseTimeMillis(timestamp);
        buffer.putLong(reverseTimeMillis);
        return buffer.getBuffer();
    }

    public static byte[] makeColumnName(
            String serviceName,
            String applicationName, short applicationType,
            short columnSlotNumber
    ) {
        Objects.requireNonNull(serviceName, "serviceName");
        Objects.requireNonNull(applicationName, "applicationName");

        final byte[] serviceNameBytes = BytesUtils.toBytes(serviceName);
        final byte[] applicationNameBytes = BytesUtils.toBytes(applicationName);

        final Buffer buffer = new AutomaticBuffer(64);
        buffer.putShort(columnSlotNumber);
        buffer.putShort(applicationType);
        buffer.putShort((short) applicationNameBytes.length);
        buffer.putBytes(applicationNameBytes);
        buffer.putShort((short) serviceNameBytes.length);
        buffer.putBytes(serviceNameBytes);
        return buffer.getBuffer();
    }

    public static byte[] makeSelfColumnName(
            String applicationName, short applicationType,
            short columnSlotNumber
    ) {
        final Buffer buffer = new AutomaticBuffer(
                applicationName.length() + BytesUtils.SHORT_BYTE_LENGTH * 2
        );
        buffer.putShort(columnSlotNumber);
        buffer.putShort(applicationType);
        buffer.put2PrefixedString(applicationName);
        return buffer.getBuffer();
    }

    public static short getServiceTypeFromColumnName(byte[] bytes) {
        return BytesUtils.bytesToShort(bytes, 2);
    }

    public static String getApplicationNameFromColumnName(byte[] bytes) {
        final short length = BytesUtils.bytesToShort(bytes, 4);
        return BytesUtils.toStringAndRightTrim(bytes, 6, length);
    }

    public static String getApplicationNameFromColumnNameForUser(byte[] bytes, ServiceType destServiceType) {
        String destApplicationName = getApplicationNameFromColumnName(bytes);
        String destServiceTypeName = destServiceType.getName();
        return destApplicationName + "_" + destServiceTypeName;
    }

    public static short getHistogramSlotFromColumnName(byte[] bytes) {
        return BytesUtils.bytesToShort(bytes, 0);
    }
}
