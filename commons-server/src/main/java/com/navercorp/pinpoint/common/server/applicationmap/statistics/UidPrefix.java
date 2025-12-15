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
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.timeseries.util.IntInverter;
import com.navercorp.pinpoint.common.timeseries.util.SecondTimestamp;
import com.navercorp.pinpoint.common.util.BytesUtils;

public class UidPrefix {

    public static final int KEY_SIZE = 1024;

    public static final int TIMESTAMP_SIZE = BytesUtils.INT_BYTE_LENGTH;
    public static final int PREFIX_SIZE = BytesUtils.INT_BYTE_LENGTH +
                                          BytesUtils.INT_BYTE_LENGTH +
                                          BytesUtils.INT_BYTE_LENGTH +
                                          TIMESTAMP_SIZE;

    static final HashFunction hashFunction = Hashing.murmur3_32_fixed();

    private final int serviceUid;
    private final int applicationNameHash;
    private final int serviceType;
    private final long timestamp;

    public UidPrefix(int serviceUid, int applicationNameHash, int serviceType, long timestamp) {
        this.serviceUid = serviceUid;
        this.applicationNameHash = applicationNameHash;
        this.serviceType = serviceType;
        this.timestamp = timestamp;
    }

    public int getServiceUid() {
        return serviceUid;
    }

    public int getApplicationNameHash() {
        return applicationNameHash;
    }

    public int getServiceType() {
        return serviceType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static void writePrefix(Buffer buffer, int serviceUid, byte[] applicationNameBytes,
                                   int serviceType, long timestamp) {

        buffer.putInt(hash(applicationNameBytes));
        buffer.putInt(serviceUid);
        buffer.putInt(IntInverter.invert(serviceType));

        int secondTimestamp = SecondTimestamp.convertSecondTimestamp(timestamp);
        int reverseTimeMillis = IntInverter.invert(secondTimestamp);
        buffer.putInt(reverseTimeMillis);
    }

    public static UidPrefix readPrefix(Buffer buffer) {
        int applicationNameHash = buffer.readInt();
        int serviceUid = buffer.readInt(); // serviceUid
        int serviceType = IntInverter.restore(buffer.readInt()); // serviceType

        int secondTimestamp = IntInverter.restore(buffer.readInt());
        long msTimestamp = SecondTimestamp.restoreSecondTimestamp(secondTimestamp);
        return new UidPrefix(serviceUid, applicationNameHash, serviceType, msTimestamp);
    }


    public static int hash(byte[] bytes) {
        HashCode hashCode = hashFunction.hashBytes(bytes);
        return hashCode.hashCode();
    }

    public static String requireNameLength(String name, String fieldName) {
        return requireNameLength(name, fieldName, KEY_SIZE);
    }

    public static String requireNameLength(String name, String fieldName, int maxKeySize) {
        if (name == null) {
            throw new NullPointerException(fieldName);
        }
        if (name.length() > maxKeySize) {
            throw new IllegalArgumentException(fieldName + " too long:" + name.length());
        }
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        UidPrefix uidPrefix = (UidPrefix) o;
        return serviceUid == uidPrefix.serviceUid && applicationNameHash == uidPrefix.applicationNameHash && serviceType == uidPrefix.serviceType && timestamp == uidPrefix.timestamp;
    }

    @Override
    public int hashCode() {
        int result = serviceUid;
        result = 31 * result + applicationNameHash;
        result = 31 * result + serviceType;
        result = 31 * result + Long.hashCode(timestamp);
        return result;
    }

    @Override
    public String toString() {
        return "UidPrefix{" +
               "serviceUid=" + serviceUid +
               ", applicationNameHash=" + applicationNameHash +
               ", serviceType=" + serviceType +
               ", timestamp=" + timestamp +
               '}';
    }
}
