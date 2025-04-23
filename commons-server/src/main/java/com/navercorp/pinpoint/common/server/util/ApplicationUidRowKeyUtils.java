package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.util.BytesUtils;

public class ApplicationUidRowKeyUtils {

    public static byte[] makeRowKey(ServiceUid serviceUid) {
        byte[] rowKey = new byte[ByteArrayUtils.INT_BYTE_LENGTH];
        ByteArrayUtils.writeInt(serviceUid.getUid(), rowKey, 0);
        return rowKey;
    }

    public static byte[] makeRowKey(ServiceUid serviceUid, String applicationName) {
        final byte[] applicationNameBytes = BytesUtils.toBytes(applicationName);
        byte[] rowKey = new byte[ByteArrayUtils.INT_BYTE_LENGTH + applicationNameBytes.length];
        ByteArrayUtils.writeInt(serviceUid.getUid(), rowKey, 0);
        BytesUtils.writeBytes(rowKey, ByteArrayUtils.INT_BYTE_LENGTH, applicationNameBytes);
        return rowKey;
    }

    public static String getApplicationName(byte[] rowKey) {
        return BytesUtils.toString(rowKey, ByteArrayUtils.INT_BYTE_LENGTH, rowKey.length - ByteArrayUtils.INT_BYTE_LENGTH);
    }

    public static byte[] makeRowKey(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKey = new byte[ByteArrayUtils.INT_BYTE_LENGTH + ByteArrayUtils.LONG_BYTE_LENGTH];
        ByteArrayUtils.writeInt(serviceUid.getUid(), rowKey, 0);
        ByteArrayUtils.writeLong(applicationUid.getUid(), rowKey, ByteArrayUtils.INT_BYTE_LENGTH);
        return rowKey;
    }

    public static ServiceUid getServiceUid(byte[] rowKey) {
        return ServiceUid.of(ByteArrayUtils.bytesToInt(rowKey, 0));
    }

    public static ApplicationUid getApplicationUidLong(byte[] rowKey) {
        return ApplicationUid.of(ByteArrayUtils.bytesToLong(rowKey, ByteArrayUtils.INT_BYTE_LENGTH));
    }

}
