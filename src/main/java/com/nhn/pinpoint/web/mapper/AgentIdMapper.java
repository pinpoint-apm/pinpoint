package com.nhn.pinpoint.web.mapper;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 *
 */
@Component
public class AgentIdMapper implements RowMapper<String[]> {
    private static final String[] EMPTY_ARRAY = new String[0];

	@Override
	public String[] mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return EMPTY_ARRAY;
        }
		KeyValue[] raw = result.raw();

		String[] ret = new String[raw.length];
		int index = 0;

		for (KeyValue kv : raw) {
			ret[index++] = Bytes.toString(kv.getQualifier());
		}

		return ret;
	}
}
