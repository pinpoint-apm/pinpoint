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

package com.navercorp.pinpoint.common.server.uid;

import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

public final class UidRowKeyUtils {

    static final int UID_ROW_KEY_SIZE = BytesUtils.INT_BYTE_LENGTH + BytesUtils.LONG_BYTE_LENGTH + BytesUtils.INT_BYTE_LENGTH;
    static final int UID_ROW_KEY_TIMESTAMP_SIZE = UID_ROW_KEY_SIZE + BytesUtils.LONG_BYTE_LENGTH;

    public static byte[] writeRowKey(int saltKeySize, int serviceUid, long applicationUid, int serviceType) {
        byte[] rowkey = new byte[UID_ROW_KEY_SIZE + saltKeySize];
        writeUid(rowkey, saltKeySize, serviceUid, applicationUid, serviceType);
        return rowkey;
    }

    public static byte[] writeRowKey(int saltKeySize, int serviceUid, long applicationUid, int serviceType, long timestamp) {
        byte[] rowkey = new byte[UID_ROW_KEY_TIMESTAMP_SIZE + saltKeySize];

        final int offset = writeUid(rowkey, saltKeySize, serviceUid, applicationUid, serviceType);

        long reverseTimeMillis = TimeUtils.reverseTimeMillis(timestamp);
        ByteArrayUtils.writeLong(reverseTimeMillis, rowkey, offset);
        return rowkey;
    }

    private static int writeUid(byte[] rowkey, int saltKeySize, int serviceUid, long applicationUid, int serviceType) {
        int offset = ByteArrayUtils.writeLong(applicationUid, rowkey, saltKeySize);
        offset = ByteArrayUtils.writeInt(serviceType, rowkey, offset);
        return ByteArrayUtils.writeInt(serviceUid, rowkey, offset);
    }
}
