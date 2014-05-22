package com.nhn.pinpoint.web.dao.hbase;

import java.util.List;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.web.dao.ApplicationIndexDao;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;

/**
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseApplicationIndexDao implements ApplicationIndexDao {

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("applicationNameMapper")
	private RowMapper<Application> applicationNameMapper;

	@Autowired
	@Qualifier("agentIdMapper")
	private RowMapper<List<String>> agentIdMapper;

	@Override
	public List<Application> selectAllApplicationNames() {
		Scan scan = new Scan();
		scan.setCaching(30);
		return hbaseOperations2.find(HBaseTables.APPLICATION_INDEX, scan, applicationNameMapper);
	}

	@Override
	public List<String> selectAgentIds(String applicationName) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        byte[] rowKey = Bytes.toBytes(applicationName);

		Get get = new Get(rowKey);
		get.addFamily(HBaseTables.APPLICATION_INDEX_CF_AGENTS);

		return hbaseOperations2.get(HBaseTables.APPLICATION_INDEX, get, agentIdMapper);
	}
	
	@Override
	public void deleteApplicationName(String applicationName) {
		byte[] rowKey = Bytes.toBytes(applicationName);
		Delete delete = new Delete(rowKey);
		delete.setWriteToWAL(false);
		hbaseOperations2.delete(HBaseTables.APPLICATION_INDEX, delete);
	}
}
