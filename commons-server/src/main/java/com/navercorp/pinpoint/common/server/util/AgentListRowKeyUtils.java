package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

public class AgentListRowKeyUtils {
    private static final int AGENT_ID_OFFSET = BytesUtils.INT_BYTE_LENGTH + BytesUtils.LONG_BYTE_LENGTH;
    private static final int AGENT_START_TIME_OFFSET = AGENT_ID_OFFSET + HbaseTableConstants.AGENT_ID_MAX_LEN;

    private AgentListRowKeyUtils() {
    }

    public static byte[] makeRowKey(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId, long agentStartTime) {
        Buffer buffer = new FixedBuffer(AGENT_START_TIME_OFFSET + BytesUtils.LONG_BYTE_LENGTH);

        buffer.putInt(serviceUid.getUid());
        buffer.putLong(applicationUid.getUid());
        buffer.putPadString(agentId, HbaseTableConstants.AGENT_ID_MAX_LEN);
        buffer.putLong(TimeUtils.reverseTimeMillis(agentStartTime));

        return buffer.getBuffer();
    }

    public static byte[] makeRowKey(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId) {
        Buffer buffer = new FixedBuffer(AGENT_START_TIME_OFFSET);
        buffer.putInt(serviceUid.getUid());
        buffer.putLong(applicationUid.getUid());
        buffer.putPadString(agentId, HbaseTableConstants.AGENT_ID_MAX_LEN);
        return buffer.getBuffer();
    }

    public static byte[] makeRowKey(ServiceUid serviceUid, ApplicationUid applicationUid) {
        Buffer buffer = new FixedBuffer(AGENT_ID_OFFSET);
        buffer.putInt(serviceUid.getUid());
        buffer.putLong(applicationUid.getUid());
        return buffer.getBuffer();
    }

    public static byte[] makeRowKey(ServiceUid serviceUid) {
        Buffer buffer = new FixedBuffer(BytesUtils.INT_BYTE_LENGTH);
        buffer.putInt(serviceUid.getUid());
        return buffer.getBuffer();
    }

    public static String getAgentId(byte[] rowKey) {
        Buffer buffer = new FixedBuffer(rowKey);
        buffer.setOffset(AGENT_ID_OFFSET);
        return buffer.readPadStringAndRightTrim(HbaseTableConstants.AGENT_ID_MAX_LEN);
    }

    public static long getAgentStartTime(byte[] rowKey) {
        Buffer buffer = new FixedBuffer(rowKey);
        buffer.setOffset(AGENT_START_TIME_OFFSET);
        return TimeUtils.recoveryTimeMillis(buffer.readLong());
    }
}
