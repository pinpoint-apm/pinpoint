package com.nhn.pinpoint.web.mapper;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.nhn.pinpoint.web.vo.Application;

/**
 *
 */
@Component
public class ApplicationNameMapper implements RowMapper<Application> {
	@Override
	public Application mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }
		String applicationName = Bytes.toString(result.getRow());
		short serviceType = Bytes.toShort(result.value());
		return new Application(applicationName, serviceType);
	}
}
