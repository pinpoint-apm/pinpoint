package com.nhn.hippo.web.mapper;

import com.profiler.common.util.BytesUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class ApplicationNameMapper implements RowMapper<String> {

	@Override
	public String mapRow(Result result, int rowNum) throws Exception {
		return Bytes.toString(result.getRow());
	}
}
