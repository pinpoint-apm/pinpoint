package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.PinpointConstants;
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
        Buffer buffer = new FixedBuffer(4 + PinpointConstants.APPLICATION_NAME_MAX_LEN_V3);
        buffer.putInt(serviceUid.getUid());
        buffer.putPadString(applicationName, PinpointConstants.APPLICATION_NAME_MAX_LEN_V3);
        return buffer.getBuffer();
    }

    public static byte[] createRowKey(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        Buffer buffer = new FixedBuffer(4 + PinpointConstants.APPLICATION_NAME_MAX_LEN_V3 + 4);
        buffer.putInt(serviceUid.getUid());
        buffer.putPadString(applicationName, PinpointConstants.APPLICATION_NAME_MAX_LEN_V3);
        buffer.putInt(serviceTypeCode);
        return buffer.getBuffer();
    }

    public static byte[] createRowKey(ServiceUid serviceUid, String applicationName, int serviceTypeCode, String agentId) {
        Buffer buffer = new AutomaticBuffer(4 + PinpointConstants.APPLICATION_NAME_MAX_LEN_V3 + 4 + 1 + agentId.length());
        buffer.putInt(serviceUid.getUid());
        buffer.putPadString(applicationName, PinpointConstants.APPLICATION_NAME_MAX_LEN_V3);
        buffer.putInt(serviceTypeCode);
        buffer.putPrefixedString(agentId);
        return buffer.getBuffer();
    }
}
