package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.AGENTID_APPLICATION_INDEX;

import static com.nhn.pinpoint.common.hbase.HBaseTables.AGENTID_APPLICATION_INDEX_CF_APPLICATION;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;

import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.collector.dao.AgentIdApplicationIndexDao;
import org.springframework.stereotype.Repository;

/**
 * find applicationname by agentId
 * 
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseAgentIdApplicationIndexDao implements AgentIdApplicationIndexDao {

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Autowired
	@Qualifier("applicationNameMapper")
	private RowMapper<String> applicationNameMapper;

	@Override
	public void insert(String agentId, String applicationName) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }

        byte[] agentIdByte = Bytes.toBytes(agentId);
		byte[] appNameByte = Bytes.toBytes(applicationName);

		Put put = new Put(agentIdByte);
		put.add(AGENTID_APPLICATION_INDEX_CF_APPLICATION, appNameByte, appNameByte);

		hbaseTemplate.put(AGENTID_APPLICATION_INDEX, put);
	}

	@Override
	public String selectApplicationName(String agentId) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        byte[] rowKey = Bytes.toBytes(agentId);
		Get get = new Get(rowKey);
		get.addFamily(AGENTID_APPLICATION_INDEX_CF_APPLICATION);

		return hbaseTemplate.get(AGENTID_APPLICATION_INDEX, get, applicationNameMapper);
	}
}
