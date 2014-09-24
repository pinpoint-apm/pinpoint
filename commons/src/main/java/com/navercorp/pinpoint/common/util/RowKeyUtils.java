package com.nhn.pinpoint.common.util;

import static com.nhn.pinpoint.common.PinpointConstants.AGENT_NAME_MAX_LEN;
import static com.nhn.pinpoint.common.util.BytesUtils.INT_BYTE_LENGTH;
import static com.nhn.pinpoint.common.util.BytesUtils.LONG_BYTE_LENGTH;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * @author emeroad
 */
public class RowKeyUtils {

	public static byte[] concatFixedByteAndLong(byte[] fixedBytes, int maxFixedLength, long l) {
		if (fixedBytes == null) {
			throw new NullPointerException("fixedBytes must not null");
		}
		if (fixedBytes.length > maxFixedLength) {
			throw new IllegalArgumentException("fixedBytes.length too big. length:" + fixedBytes.length);
		}
		byte[] rowKey = new byte[maxFixedLength + LONG_BYTE_LENGTH];
		Bytes.putBytes(rowKey, 0, fixedBytes, 0, fixedBytes.length);
		BytesUtils.writeLong(l, rowKey, maxFixedLength);
		return rowKey;
	}


	public static byte[] getMetaInfoRowKey(String agentId, long agentStartTime, int keyCode) {
		// TODO 일단 agent의 조회 시간 로직을 따로 만들어야 되므로 그냥0으로 하자.
		if (agentId == null) {
			throw new NullPointerException("agentId must not be null");
		}

		final byte[] agentBytes = Bytes.toBytes(agentId);
		if (agentBytes.length > AGENT_NAME_MAX_LEN) {
			throw new IllegalArgumentException("agent.length too big. agent:" + agentId + " length:" + agentId.length());
		}

		final byte[] buffer = new byte[AGENT_NAME_MAX_LEN + LONG_BYTE_LENGTH + INT_BYTE_LENGTH];
		Bytes.putBytes(buffer, 0, agentBytes, 0, agentBytes.length);

		long reverseCurrentTimeMillis = TimeUtils.reverseTimeMillis(agentStartTime);
		BytesUtils.writeLong(reverseCurrentTimeMillis, buffer, AGENT_NAME_MAX_LEN);

        BytesUtils.writeInt(keyCode, buffer, AGENT_NAME_MAX_LEN + LONG_BYTE_LENGTH);
		return buffer;
	}


}
