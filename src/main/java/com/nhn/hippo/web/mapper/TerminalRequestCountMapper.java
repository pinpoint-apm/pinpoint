package com.nhn.hippo.web.mapper;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.nhn.hippo.web.vo.TerminalRequest;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.util.TerminalSpanUtils;

/**
 *
 */
@Component
public class TerminalRequestCountMapper implements RowMapper<List<TerminalRequest>> {

	@Override
	public List<TerminalRequest> mapRow(Result result, int rowNum) throws Exception {
		KeyValue[] keyList = result.raw();
		
		List<TerminalRequest> requestList = new ArrayList<TerminalRequest>();
		for (KeyValue kv : keyList) {
			if (kv.getFamilyLength() == HBaseTables.TERMINAL_STATISTICS_CF_COUNTER.length) {
				String from = TerminalSpanUtils.getApplicationNameFromRowKey(kv.getRow());
				String to = TerminalSpanUtils.getApplicationNameFromColumnName(kv.getQualifier());
				long requestCount = Bytes.toLong(kv.getValue());
				short serviceType = TerminalSpanUtils.getServiceTypeFromColumnName(kv.getQualifier());

				TerminalRequest request = new TerminalRequest(from, to, serviceType, requestCount);
				requestList.add(request);
			}
		}
		return requestList;
	}
}
