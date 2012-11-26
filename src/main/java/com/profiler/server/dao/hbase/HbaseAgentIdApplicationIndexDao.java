package com.profiler.server.dao.hbase;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;

import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.server.dao.AgentIdApplicationIndex;

/**
 * find applicationname by agentId
 * 
 * @author netspider
 * 
 */
public class HbaseAgentIdApplicationIndexDao implements AgentIdApplicationIndex {

	String TABLE_NAME = HBaseTables.AGENTID_APPLICATION_INDEX;
	byte[] COLFAM_TRACE = HBaseTables.AGENTID_APPLICATION_INDEX_CF_APPLICATION;

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Autowired
	@Qualifier("applicationNameMapper")
	private RowMapper<String> applicationNameMapper;

	@Override
	public void insert(String agentId, String applicationName) {
		byte[] agentIdByte = Bytes.toBytes(agentId);
		byte[] appNameByte = Bytes.toBytes(applicationName);

		Put put = new Put(agentIdByte);
		put.add(COLFAM_TRACE, appNameByte, appNameByte);

		hbaseTemplate.put(TABLE_NAME, put);
	}

	@Override
	public String selectApplicationName(String agentId) {
		byte[] rowKey = Bytes.toBytes(agentId);
		Get get = new Get(rowKey);
		get.addFamily(HBaseTables.AGENTID_APPLICATION_INDEX_CF_APPLICATION);

		return hbaseTemplate.get(TABLE_NAME, get, applicationNameMapper);
	}
}
