package com.nhn.pinpoint.web.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.nhn.pinpoint.thrift.dto.AgentStat;

/**
 * @author harebox
 */
@Component
public class AgentStatMapper implements RowMapper<List<AgentStat>> {

	private TProtocolFactory factory = new TCompactProtocol.Factory();
	
	public List<AgentStat> mapRow(Result result, int rowNum) throws Exception {
		if (result == null) {
			return Collections.emptyList();
		}
		
		KeyValue[] raw = result.raw();
		List<AgentStat> list = new ArrayList<AgentStat>(raw.length);
		
		// CompactProtocol을 사용하고 있음.
		TDeserializer deserializer = new TDeserializer(factory);

		for (KeyValue kv : raw) {
			AgentStat each = new AgentStat();
            deserializer.deserialize(each, kv.getValue());
            list.add(each);
		}

		return list;
	}

}
