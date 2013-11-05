package com.nhn.pinpoint.web.mapper;

import java.util.Arrays;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.common.hbase.HBaseTables;

/**
 * 
 * @author netspider
 * 
 */
@Component
public class HostApplicationMapper implements RowMapper<Application> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public Application mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }
		byte[] value = result.value();

		if (value.length != HBaseTables.APPLICATION_NAME_MAX_LEN + 2) {
			logger.warn("Invalid value. {}", Arrays.toString(value));
		}

		String applicationName = Bytes.toString(value, 0, HBaseTables.APPLICATION_NAME_MAX_LEN - 1).trim();
		short serviceType = Bytes.toShort(value, HBaseTables.APPLICATION_NAME_MAX_LEN);

		return new Application(applicationName, serviceType);
	}
}
