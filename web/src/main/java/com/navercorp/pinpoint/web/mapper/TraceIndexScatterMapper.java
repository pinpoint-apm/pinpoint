package com.nhn.pinpoint.web.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.OffsetFixedBuffer;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.util.TimeUtils;
import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.web.vo.scatter.Dot;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.nhn.pinpoint.common.util.BytesUtils;

/**
 * @author emeroad
 * @author netspider
 */
@Component
public class TraceIndexScatterMapper implements RowMapper<List<Dot>> {

	@Override
	public List<Dot> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }

		KeyValue[] raw = result.raw();
		List<Dot> list = new ArrayList<Dot>(raw.length);
		for (KeyValue kv : raw) {
            final Dot dot = createDot(kv);
            list.add(dot);
		}

		return list;
	}

    private Dot createDot(KeyValue kv) {
        final byte[] buffer = kv.getBuffer();

        final int valueOffset = kv.getValueOffset();
        final Buffer valueBuffer = new OffsetFixedBuffer(buffer, valueOffset);
        int elapsed = valueBuffer.readVarInt();
        int exceptionCode = valueBuffer.readSVarInt();
        String agentId = valueBuffer.readPrefixedString();

        long reverseAcceptedTime = BytesUtils.bytesToLong(buffer, kv.getRowOffset() + HBaseTables.APPLICATION_NAME_MAX_LEN + HBaseTables.APPLICATION_TRACE_INDEX_ROW_DISTRIBUTE_SIZE);
        long acceptedTime = TimeUtils.recoveryTimeMillis(reverseAcceptedTime);

        final int qualifierOffset = kv.getQualifierOffset();

		// TransactionId transactionId = new TransactionId(buffer, qualifierOffset);
        // 잠시 TransactionIdMapper의 것을 사용하도록 함.
		TransactionId transactionId = TransactionIdMapper.parseVarTransactionId(buffer, qualifierOffset);
        
        return new Dot(transactionId, acceptedTime, elapsed, exceptionCode, agentId);
    }

    /*
    public static TransactionId parseVarTransactionId(byte[] bytes, int offset) {
        if (bytes == null) {
            throw new NullPointerException("bytes must not be null");
        }
        final Buffer buffer = new OffsetFixedBuffer(bytes, offset);

		// skip elapsed time (not used) hbase column prefix filter에서 filter용도로만 사용함.
        // 데이터 사이즈를 줄일 수 있는지 모르겠음.
		buffer.readInt();
		
        String agentId = buffer.readPrefixedString();
        long agentStartTime = buffer.readSVarLong();
        long transactionSequence = buffer.readVarLong();
        return new TransactionId(agentId, agentStartTime, transactionSequence);
    }
    */
}
