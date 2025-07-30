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

public class UidBytesParseUtils {

    private static final int AGENT_ID_OFFSET = ByteArrayUtils.INT_BYTE_LENGTH + ByteArrayUtils.LONG_BYTE_LENGTH;    //serviceUid + applicationUid
    private static final int AGENT_START_TIME_OFFSET = AGENT_ID_OFFSET + HbaseTableConstants.AGENT_ID_MAX_LEN;

    // serviceUid always comes first in the row key
    public static ServiceUid parseServiceUidFromRowKey(byte[] rowKey) {
        return ServiceUid.of(ByteArrayUtils.bytesToInt(rowKey, 0));
    }

    // applicationUid or applicationName comes second in the row key
    public static ApplicationUid parseApplicationUidFromRowKey(byte[] rowKey) {
        return ApplicationUid.of(ByteArrayUtils.bytesToLong(rowKey, ByteArrayUtils.INT_BYTE_LENGTH));
    }

    // serviceUid + applicationName + @(separator) + serviceTypeCode
    public static ApplicationUidAttribute parseApplicationUidAttrFromRowKey(byte[] ApplicationUidRowKey) {
        return parseApplicationUidAttr(ApplicationUidRowKey, ByteArrayUtils.INT_BYTE_LENGTH, ApplicationUidRowKey.length - ByteArrayUtils.INT_BYTE_LENGTH);
    }

    // applicationName + @(separator) + serviceTypeCode
    public static ApplicationUidAttribute parseApplicationUidAttrFromValue(byte[] valueArray, int offset, int length) {
        return parseApplicationUidAttr(valueArray, offset, length);
    }

    private static ApplicationUidAttribute parseApplicationUidAttr(byte[] byteArray, int offset, int length) {
        String applicationName = BytesUtils.toString(byteArray, offset, length - ByteArrayUtils.INT_BYTE_LENGTH - 1);
        int serviceTypeCode = ByteArrayUtils.bytesToInt(byteArray, offset + length - ByteArrayUtils.INT_BYTE_LENGTH);
        return new ApplicationUidAttribute(applicationName, serviceTypeCode);
    }

    public static String parseAgentId(byte[] agentNameRowKey) {
        Buffer buffer = new FixedBuffer(agentNameRowKey);
        buffer.setOffset(AGENT_ID_OFFSET);
        return buffer.readPadStringAndRightTrim(HbaseTableConstants.AGENT_ID_MAX_LEN);
    }

    public static long parseAgentStartTime(byte[] agentNameRowKey) {
        return TimeUtils.recoveryTimeMillis(ByteArrayUtils.bytesToLong(agentNameRowKey, AGENT_START_TIME_OFFSET));
    }
}


