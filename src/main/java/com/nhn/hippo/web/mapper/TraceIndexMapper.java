package com.nhn.hippo.web.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nhn.hippo.web.vo.TraceId;
import com.profiler.common.util.BytesUtils;
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
            long least = BytesUtils.bytesToLong(buffer, qualifierOffset + 8);
            long most = BytesUtils.bytesToLong(buffer, qualifierOffset);
            TraceId traceId = new TraceId(most, least);

            traceIdList.add(traceId);
		}

		return traceIdList;
	}
}
