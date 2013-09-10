package com.nhn.pinpoint.web.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.web.vo.TraceId;
import com.nhn.pinpoint.common.util.BytesUtils;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class TraceIndexMapper implements RowMapper<List<TraceId>> {
	@Override
	public List<TraceId> mapRow(Result result, int rowNum) throws Exception {
		if (result == null) {
			return Collections.emptyList();
		}

		KeyValue[] raw = result.raw();

		List<TraceId> traceIdList = new ArrayList<TraceId>(raw.length);

		for (KeyValue kv : raw) {
            byte[] buffer = kv.getBuffer();
            int qualifierOffset = kv.getQualifierOffset();

            String agentId = BytesUtils.toStringAndRightTrim(buffer, qualifierOffset, HBaseTables.AGENT_NAME_MAX_LEN);
            long agentStartTime = BytesUtils.bytesToLong(buffer, qualifierOffset + HBaseTables.AGENT_NAME_MAX_LEN);
            long transactionId = BytesUtils.bytesToLong(buffer, qualifierOffset + BytesUtils.LONG_BYTE_LENGTH + HBaseTables.AGENT_NAME_MAX_LEN);
            TraceId traceId = new TraceId(agentId, agentStartTime, transactionId);

            traceIdList.add(traceId);
		}

		return traceIdList;
	}
}
