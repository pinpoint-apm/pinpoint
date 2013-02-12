package com.nhn.hippo.web.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.nhn.hippo.web.vo.scatter.Dot;
import com.profiler.common.util.BytesUtils;

/**
 *
 */
@Component
public class TraceIndexScatterMapper implements RowMapper<List<Dot>> {
	@Override
	public List<Dot> mapRow(Result result, int rowNum) throws Exception {
		if (result == null) {
			return Collections.emptyList();
		}

		KeyValue[] raw = result.raw();

		List<Dot> list = new ArrayList<Dot>(raw.length);

		for (KeyValue kv : raw) {
			byte[] v = kv.getValue();

			int elapsed = BytesUtils.bytesToInt(v, 0);
			int exceptionCode = BytesUtils.bytesToInt(v, 4);
			long timestamp = BytesUtils.bytesToLong(kv.getRow(), 24);

			long[] tid = BytesUtils.bytesToLongLong(kv.getQualifier());
			String traceId = new UUID(tid[0], tid[1]).toString();

			list.add(new Dot(exceptionCode, elapsed, timestamp, traceId));
		}

		return list;
	}
}
