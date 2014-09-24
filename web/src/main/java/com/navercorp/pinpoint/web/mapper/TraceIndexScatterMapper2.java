package com.nhn.pinpoint.web.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.data.hadoop.hbase.RowMapper;

import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.OffsetFixedBuffer;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.TimeUtils;
import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.web.vo.scatter.Dot;

/**
 * @author netspider
 */
public class TraceIndexScatterMapper2 implements RowMapper<List<Dot>> {

	private final int responseOffsetFrom;
	private final int responseOffsetTo;

	public TraceIndexScatterMapper2(int responseOffsetFrom, int responseOffsetTo) {
		this.responseOffsetFrom = responseOffsetFrom;
		this.responseOffsetTo = responseOffsetTo;
	}

	@Override
	public List<Dot> mapRow(Result result, int rowNum) throws Exception {
		if (result.isEmpty()) {
			return Collections.emptyList();
		}

		KeyValue[] raw = result.raw();
		List<Dot> list = new ArrayList<Dot>(raw.length);
		for (KeyValue kv : raw) {
			final Dot dot = createDot(kv);
			if (dot != null) {
				list.add(dot);
			}
		}

		return list;
	}

	private Dot createDot(KeyValue kv) {
		final byte[] buffer = kv.getBuffer();

		final int valueOffset = kv.getValueOffset();
		final Buffer valueBuffer = new OffsetFixedBuffer(buffer, valueOffset);
		int elapsed = valueBuffer.readVarInt();

		if (elapsed < responseOffsetFrom || elapsed > responseOffsetTo) {
			return null;
		}

		int exceptionCode = valueBuffer.readSVarInt();
		String agentId = valueBuffer.readPrefixedString();

		long reverseAcceptedTime = BytesUtils.bytesToLong(buffer, kv.getRowOffset() + HBaseTables.APPLICATION_NAME_MAX_LEN + HBaseTables.APPLICATION_TRACE_INDEX_ROW_DISTRIBUTE_SIZE);
		long acceptedTime = TimeUtils.recoveryTimeMillis(reverseAcceptedTime);

		final int qualifierOffset = kv.getQualifierOffset();

		// TransactionId transactionId = new TransactionId(buffer,
		// qualifierOffset);
		// 잠시 TransactionIdMapper의 것을 사용하도록 함.
		TransactionId transactionId = TransactionIdMapper.parseVarTransactionId(buffer, qualifierOffset);

		return new Dot(transactionId, acceptedTime, elapsed, exceptionCode, agentId);
	}
}
