package com.nhn.pinpoint.web.mapper;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
@Component
public class AgentIdMapper implements RowMapper<List<String>> {

	@Override
	public List<String> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
		final KeyValue[] raw = result.raw();
		final List<String> agentIdList = new ArrayList<String>(raw.length);

		for (KeyValue kv : raw) {
            final String agentId = Bytes.toString(kv.getQualifier());
            agentIdList.add(agentId);
		}

		return agentIdList;
	}
}
