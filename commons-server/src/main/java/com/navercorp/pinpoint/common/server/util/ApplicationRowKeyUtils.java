package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.util.BytesUtils;

public class ApplicationRowKeyUtils {

    private ApplicationRowKeyUtils() {
    }

    public static byte[] createRowKey(int serviceUid) {
        Buffer buffer = new FixedBuffer(4);
        buffer.putInt(serviceUid);
        return buffer.getBuffer();
    }

    public static byte[] createRowKey(int serviceUid, String applicationName) {
        Buffer buffer = new AutomaticBuffer(4 +
                PinpointConstants.APPLICATION_NAME_MAX_LEN_V3
        );
        buffer.putInt(serviceUid);
        buffer.putPadString(applicationName, PinpointConstants.APPLICATION_NAME_MAX_LEN_V3);
        return buffer.getBuffer();
    }

    public static byte[] createRowKey(int serviceUid, String applicationName, int serviceTypeCode) {
        Buffer buffer = new AutomaticBuffer(4 +
                PinpointConstants.APPLICATION_NAME_MAX_LEN_V3 + 4
        );
        buffer.putInt(serviceUid);
        buffer.putPadString(applicationName, PinpointConstants.APPLICATION_NAME_MAX_LEN_V3);
        buffer.putInt(serviceTypeCode);
        return buffer.getBuffer();
    }

    public static int extractServiceUid(byte[] row) {
        return ByteArrayUtils.bytesToInt(row, 0);
    }

    public static String extractApplicationName(byte[] row) {
        return BytesUtils.toStringAndRightTrim(row, 4, PinpointConstants.APPLICATION_NAME_MAX_LEN_V3);
    }

    public static int extractServiceTypeCode(byte[] row) {
        return ByteArrayUtils.bytesToInt(row, 4 +
                PinpointConstants.APPLICATION_NAME_MAX_LEN_V3);
    }
}
