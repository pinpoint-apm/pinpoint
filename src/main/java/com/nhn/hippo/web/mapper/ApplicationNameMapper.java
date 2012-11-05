package com.nhn.hippo.web.mapper;

import org.apache.hadoop.hbase.client.Result;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class ApplicationNameMapper implements RowMapper<String> {

	@Override
	public String mapRow(Result result, int rowNum) throws Exception {
		return new String(result.getRow(), "UTF-8");
	}
}
