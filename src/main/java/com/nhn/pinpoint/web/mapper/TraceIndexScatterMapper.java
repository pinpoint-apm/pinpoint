package com.nhn.pinpoint.web.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.util.TimeUtils;
import com.nhn.pinpoint.web.vo.TransactionId;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.nhn.pinpoint.web.vo.scatter.Dot;
import com.nhn.pinpoint.common.util.BytesUtils;

/**
 * @author emeroad
 */
@Component
public class TraceIndexScatterMapper implements RowMapper<List<Dot>> {

	@Override
	public List<Dot> mapRow(Result result, int rowNum) throws Exception {
        if(result.isEmpty()) {
            return Collections.emptyList();
        }

		KeyValue[] raw = result.raw();
		List<Dot> list = new ArrayList<Dot>(raw.length);

		for (KeyValue kv : raw) {
            final byte[] buffer = kv.getBuffer();

            final int valueOffset = kv.getValueOffset();
            int elapsed = BytesUtils.bytesToInt(buffer, valueOffset);
			int exceptionCode = BytesUtils.bytesToInt(buffer, valueOffset + BytesUtils.INT_BYTE_LENGTH);

            long reverseAcceptedTime = BytesUtils.bytesToLong(buffer, kv.getRowOffset() + HBaseTables.APPLICATION_NAME_MAX_LEN + HBaseTables.APPLICATION_TRACE_INDEX_ROW_DISTRIBUTE_SIZE);
			long acceptedTime = TimeUtils.recoveryCurrentTimeMillis(reverseAcceptedTime);

            final int qualifierOffset = kv.getQualifierOffset();

            TransactionId transactionId = new TransactionId(buffer, qualifierOffset);
             Dot dot = new Dot(transactionId, acceptedTime, elapsed, exceptionCode);
            list.add(dot);
		}

		return list;
	}
}
