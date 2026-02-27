package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;

public class ServiceGroupRowKeyPrefixUtils {

    private ServiceGroupRowKeyPrefixUtils() {
    }

    public static byte[] createRowKey(int serviceUid) {
        byte[] serviceUidBytes = new byte[BytesUtils.INT_BYTE_LENGTH];
        ByteArrayUtils.writeInt(serviceUid, serviceUidBytes, 0);
        return serviceUidBytes;
    }

    public static byte[] createRowKey(int serviceUid, String applicationName) {
        Buffer buffer = new AutomaticBuffer(BytesUtils.INT_BYTE_LENGTH + applicationName.length() + 1);
        buffer.putInt(serviceUid);
        buffer.putNullTerminatedString(applicationName);
        return buffer.getBuffer();
    }

    public static byte[] createRowKey(int serviceUid, String applicationName, int serviceTypeCode) {
        Buffer buffer = new AutomaticBuffer(BytesUtils.INT_BYTE_LENGTH + applicationName.length() + 1 + BytesUtils.INT_BYTE_LENGTH);
        buffer.putInt(serviceUid);
        buffer.putNullTerminatedString(applicationName);
        buffer.putInt(serviceTypeCode);
        return buffer.getBuffer();
    }
}
