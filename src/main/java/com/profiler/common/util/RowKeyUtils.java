package com.profiler.common.util;

import static com.profiler.common.hbase.HBaseTables.AGENT_NAME_MAX_LEN;
import static com.profiler.common.util.BytesUtils.INT_BYTE_LENGTH;
import static com.profiler.common.util.BytesUtils.LONG_BYTE_LENGTH;
import static com.profiler.common.util.BytesUtils.SHORT_BYTE_LENGTH;

import org.apache.hadoop.hbase.util.Bytes;

import com.profiler.common.bo.ApiMetaDataBo;
import com.profiler.common.bo.SqlMetaDataBo;

/**
 *
 */
public class RowKeyUtils {

	public static byte[] concatFixedByteAndLong(byte[] fixedBytes, int maxFixedLength, long l) {
		if (fixedBytes == null) {
			throw new IllegalArgumentException("fixedBytes must not null");
		}
		if (fixedBytes.length > maxFixedLength) {
			throw new IllegalArgumentException("fixedBytes.length too big. length:" + fixedBytes.length);
		}
		byte[] rowKey = new byte[maxFixedLength + LONG_BYTE_LENGTH];
		Bytes.putBytes(rowKey, 0, fixedBytes, 0, fixedBytes.length);
		BytesUtils.writeLong(l, rowKey, maxFixedLength);
		return rowKey;
	}

	public static byte[] getApiId(String agentId, short identifier, int apiCode, long agentStartTime) {
		return getMetaInfoRowKey(agentId, identifier, apiCode, agentStartTime);
	}

	public static byte[] getSqlId(String agentId, short identifier, int hashCode, long agentStartTime) {
		return getMetaInfoRowKey(agentId, identifier, hashCode, agentStartTime);
	}

	private static byte[] getMetaInfoRowKey(String agentId, short identifier, int keyCode, long agentStartTime) {
		// TODO 일단 agent의 조회 시간 로직을 따로 만들어야 되므로 그냥0으로 하자.
		if (agentId == null) {
			throw new IllegalArgumentException("agentId must not be null");
		}

		byte[] agentBytes = Bytes.toBytes(agentId);
		if (agentBytes.length > AGENT_NAME_MAX_LEN) {
			throw new IllegalArgumentException("agent.length too big. agent:" + agentId + " length:" + agentId.length());
		}

		byte[] buffer = new byte[AGENT_NAME_MAX_LEN + SHORT_BYTE_LENGTH + INT_BYTE_LENGTH + LONG_BYTE_LENGTH];
		Bytes.putBytes(buffer, 0, agentBytes, 0, agentBytes.length);
        BytesUtils.writeShort(identifier, buffer, AGENT_NAME_MAX_LEN);
        BytesUtils.writeInt(keyCode, buffer, AGENT_NAME_MAX_LEN + SHORT_BYTE_LENGTH);
		long reverseCurrentTimeMillis = TimeUtils.reverseCurrentTimeMillis(agentStartTime);
		BytesUtils.writeLong(reverseCurrentTimeMillis, buffer, AGENT_NAME_MAX_LEN + SHORT_BYTE_LENGTH + INT_BYTE_LENGTH);
		return buffer;
	}

	public static SqlMetaDataBo parseSqlId(byte[] rowKey) {
		String agentId = Bytes.toString(rowKey, 0, AGENT_NAME_MAX_LEN).trim();
        short identifier = readIdentifier(rowKey);
		int hashCode = readKeyCode(rowKey);
		long startTIme = TimeUtils.recoveryCurrentTimeMillis(readTime(rowKey));
		SqlMetaDataBo sqlMetaDataBo = new SqlMetaDataBo(agentId, identifier, hashCode, startTIme);
		return sqlMetaDataBo;
	}


    public static ApiMetaDataBo parseApiId(byte[] rowKey) {
		String agentId = Bytes.toString(rowKey, 0, AGENT_NAME_MAX_LEN).trim();
        short identifier = readIdentifier(rowKey);
		int apiId = readKeyCode(rowKey);
		long startTIme = TimeUtils.recoveryCurrentTimeMillis(readTime(rowKey));
		ApiMetaDataBo apiMetaDataBo = new ApiMetaDataBo(agentId, identifier, apiId, startTIme);
		return apiMetaDataBo;
	}

    private static long readTime(byte[] rowKey) {
        return BytesUtils.bytesToLong(rowKey, AGENT_NAME_MAX_LEN + SHORT_BYTE_LENGTH + INT_BYTE_LENGTH);
    }

    private static int readKeyCode(byte[] rowKey) {
        return BytesUtils.bytesToInt(rowKey, AGENT_NAME_MAX_LEN + SHORT_BYTE_LENGTH);
    }

    private static short readIdentifier(byte[] rowKey) {
        return BytesUtils.bytesToShort(rowKey, AGENT_NAME_MAX_LEN);
    }
}
