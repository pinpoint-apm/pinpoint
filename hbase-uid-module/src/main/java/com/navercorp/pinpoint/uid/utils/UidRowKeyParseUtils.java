package com.navercorp.pinpoint.uid.utils;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

public class UidRowKeyParseUtils {

    private static final int AGENT_ID_OFFSET = ByteArrayUtils.INT_BYTE_LENGTH + ByteArrayUtils.LONG_BYTE_LENGTH;
    private static final int AGENT_START_TIME_OFFSET = AGENT_ID_OFFSET + HbaseTableConstants.AGENT_ID_MAX_LEN;

    public static ServiceUid getServiceUid(byte[] rowKey) {
        return ServiceUid.of(ByteArrayUtils.bytesToInt(rowKey, 0));
    }

    public static ApplicationUid getApplicationUid(byte[] rowKey) {
        return ApplicationUid.of(ByteArrayUtils.bytesToLong(rowKey, ByteArrayUtils.INT_BYTE_LENGTH));
    }


    public static String getApplicationName(byte[] ApplicationUidRowKey) {
        return BytesUtils.toString(ApplicationUidRowKey, ByteArrayUtils.INT_BYTE_LENGTH, ApplicationUidRowKey.length - ByteArrayUtils.INT_BYTE_LENGTH);
    }

    public static String getAgentId(byte[] agentNameRowKey) {
        Buffer buffer = new FixedBuffer(agentNameRowKey);
        buffer.setOffset(AGENT_ID_OFFSET);
        return buffer.readPadStringAndRightTrim(HbaseTableConstants.AGENT_ID_MAX_LEN);
    }

    public static long getAgentStartTime(byte[] agentNameRowKey) {
        return TimeUtils.recoveryTimeMillis(ByteArrayUtils.bytesToLong(agentNameRowKey, AGENT_START_TIME_OFFSET));
    }
}


