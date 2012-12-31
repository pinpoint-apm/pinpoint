package com.nhn.hippo.web.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class TraceIndexMapper implements RowMapper<List<byte[]>> {
	@Override
	public List<byte[]> mapRow(Result result, int rowNum) throws Exception {
		if (result == null) {
			return Collections.emptyList();
		}

		KeyValue[] raw = result.raw();

		List<byte[]> list = new ArrayList<byte[]>(raw.length);

		for (KeyValue kv : raw) {
			list.add(kv.getQualifier());
		}

		return list;
	}
}
