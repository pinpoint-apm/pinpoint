package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;

public class ServiceGroupRowKeyPrefixUtils {

    private ServiceGroupRowKeyPrefixUtils() {
    }

    public static byte[] createRowKey(int serviceUid) {
        Buffer buffer = new FixedBuffer(4);
        buffer.putInt(serviceUid);
        return buffer.getBuffer();
    }

    public static byte[] createRowKey(int serviceUid, String applicationName) {
        Buffer buffer = new AutomaticBuffer(4 + applicationName.length() + 1);
        buffer.putInt(serviceUid);
        buffer.putNullTerminatedString(applicationName);
        return buffer.getBuffer();
    }

    public static byte[] createRowKey(int serviceUid, String applicationName, int serviceTypeCode) {
        Buffer buffer = new AutomaticBuffer(4 + applicationName.length() + 1 + 4);
        buffer.putInt(serviceUid);
        buffer.putNullTerminatedString(applicationName);
        buffer.putInt(serviceTypeCode);
        return buffer.getBuffer();
    }
}
