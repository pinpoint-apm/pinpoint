package com.nhn.hippo.web.mapper;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class AgentIdMapper implements RowMapper<String[]> {

	@Override
	public String[] mapRow(Result result, int rowNum) throws Exception {
		KeyValue[] raw = result.raw();

		String[] ret = new String[raw.length];
		int index = 0;

		for (KeyValue kv : raw) {
			ret[index++] = Bytes.toString(kv.getQualifier());
		}

		return ret;
	}
}
