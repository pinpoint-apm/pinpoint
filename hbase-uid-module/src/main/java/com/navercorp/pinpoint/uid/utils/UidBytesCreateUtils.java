package com.navercorp.pinpoint.uid.utils;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;

public class UidBytesCreateUtils {

    private static final int AGENT_ID_OFFSET = ByteArrayUtils.INT_BYTE_LENGTH + ByteArrayUtils.LONG_BYTE_LENGTH;
    private static final int AGENT_START_TIME_OFFSET = AGENT_ID_OFFSET + HbaseTableConstants.AGENT_ID_MAX_LEN;

    public static byte[] createRowKey(ServiceUid serviceUid) {
        byte[] rowKey = new byte[ByteArrayUtils.INT_BYTE_LENGTH];
        ByteArrayUtils.writeInt(serviceUid.getUid(), rowKey, 0);
        return rowKey;
    }

    public static byte[] createRowKey(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKey = new byte[ByteArrayUtils.INT_BYTE_LENGTH + ByteArrayUtils.LONG_BYTE_LENGTH];
        ByteArrayUtils.writeInt(serviceUid.getUid(), rowKey, 0);
        ByteArrayUtils.writeLong(applicationUid.getUid(), rowKey, ByteArrayUtils.INT_BYTE_LENGTH);
        return rowKey;
    }

    // serviceUid + applicationName
    public static byte[] createRowKey(ServiceUid serviceUid, String applicationName) {
        final byte[] applicationNameBytes = BytesUtils.toBytes(applicationName);
        byte[] rowKey = new byte[ByteArrayUtils.INT_BYTE_LENGTH + applicationNameBytes.length];
        ByteArrayUtils.writeInt(serviceUid.getUid(), rowKey, 0);
        BytesUtils.writeBytes(rowKey, ByteArrayUtils.INT_BYTE_LENGTH, applicationNameBytes);
        return rowKey;
    }

    // serviceUid + applicationName + @(separator) + serviceTypeCode
    public static byte[] createApplicationUidRowKey(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        final byte[] applicationNameBytes = BytesUtils.toBytes(applicationName);
        byte[] rowKey = new byte[ByteArrayUtils.INT_BYTE_LENGTH + applicationNameBytes.length + 1 + ByteArrayUtils.INT_BYTE_LENGTH];
        ByteArrayUtils.writeInt(serviceUid.getUid(), rowKey, 0);
        writeApplicationUidAttr(rowKey, ByteArrayUtils.INT_BYTE_LENGTH, applicationNameBytes, serviceTypeCode);
        return rowKey;
    }

    public static byte[] createApplicationUidValue(ApplicationUid applicationUid) {
        byte[] rowKey = new byte[ByteArrayUtils.LONG_BYTE_LENGTH];
        ByteArrayUtils.writeLong(applicationUid.getUid(), rowKey, 0);
        return rowKey;
    }

    // ApplicationUidAttrValue = ApplicationName + @(separator) + ServiceTypeCode
    public static byte[] createApplicationUidAttrValue(String applicationName, int serviceTypeCode) {
        final byte[] applicationNameBytes = BytesUtils.toBytes(applicationName);
        byte[] rowKey = new byte[applicationNameBytes.length + 1 + ByteArrayUtils.INT_BYTE_LENGTH];
        writeApplicationUidAttr(rowKey, 0, applicationNameBytes, serviceTypeCode);
        return rowKey;
    }

    private static void writeApplicationUidAttr(byte[] buffer, int bufferOffset, byte[] applicationNameBytes, int serviceTypeCode) {
        BytesUtils.writeBytes(buffer, bufferOffset, applicationNameBytes);
        buffer[bufferOffset + applicationNameBytes.length] = ApplicationUidAttribute.SEPARATOR;
        ByteArrayUtils.writeInt(serviceTypeCode, buffer, bufferOffset + applicationNameBytes.length + 1);
    }

    public static byte[] createAgentNameRowKey(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId, long agentStartTime) {
        Buffer buffer = new FixedBuffer(AGENT_START_TIME_OFFSET + BytesUtils.LONG_BYTE_LENGTH);
        buffer.putInt(serviceUid.getUid());
        buffer.putLong(applicationUid.getUid());
        buffer.putPadString(agentId, HbaseTableConstants.AGENT_ID_MAX_LEN);
        buffer.putLong(TimeUtils.reverseTimeMillis(agentStartTime));
        return buffer.getBuffer();
    }

    public static byte[] createAgentNameRowKey(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId) {
        Buffer buffer = new FixedBuffer(AGENT_START_TIME_OFFSET);
        buffer.putInt(serviceUid.getUid());
        buffer.putLong(applicationUid.getUid());
        buffer.putPadString(agentId, HbaseTableConstants.AGENT_ID_MAX_LEN);
        return buffer.getBuffer();
    }
}


