package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public class ServiceGroupRowKeyPrefixUtils {

    private ServiceGroupRowKeyPrefixUtils() {
    }

    public static byte[] createRowKey(ServiceUid serviceUid) {
        Buffer buffer = new FixedBuffer(4);
        buffer.putInt(serviceUid.getUid());
        return buffer.getBuffer();
    }

    public static byte[] createRowKey(ServiceUid serviceUid, String applicationName) {
        Buffer buffer = new AutomaticBuffer(4 + applicationName.length() + 1);
        buffer.putInt(serviceUid.getUid());
        buffer.putNullTerminatedString(applicationName);
        return buffer.getBuffer();
    }

    public static byte[] createRowKey(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        Buffer buffer = new AutomaticBuffer(4 + applicationName.length() + 1 + 4);
        buffer.putInt(serviceUid.getUid());
        buffer.putNullTerminatedString(applicationName);
        buffer.putInt(serviceTypeCode);
        return buffer.getBuffer();
    }
}
