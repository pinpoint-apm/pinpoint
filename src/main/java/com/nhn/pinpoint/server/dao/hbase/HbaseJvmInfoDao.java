package com.nhn.pinpoint.server.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.SYSTEMINFO;
import static com.nhn.pinpoint.common.hbase.HBaseTables.SYSTEMINFO_CF_JVM;
import static com.nhn.pinpoint.common.hbase.HBaseTables.SYSTEMINFO_CN_INFO;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.common.dto2.thrift.JVMInfoThriftDTO;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.RowKeyUtils;
import com.nhn.pinpoint.server.dao.JvmInfoDao;

public class HbaseJvmInfoDao implements JvmInfoDao {

	@Autowired
	private HbaseOperations2 hbaseOperations;

	@Override
	public void insert(final JVMInfoThriftDTO jvmInfoThriftDTO, final byte[] jvmInfoBytes) {
		byte[] rowKey = getRowKey(jvmInfoThriftDTO);
		Put put = new Put(rowKey, jvmInfoThriftDTO.getDataTime());
		put.add(SYSTEMINFO_CF_JVM, SYSTEMINFO_CN_INFO, jvmInfoBytes);

		hbaseOperations.put(SYSTEMINFO, put);
	}

	byte[] getRowKey(JVMInfoThriftDTO jvmInfoThriftDTO) {
		String agentId = jvmInfoThriftDTO.getAgentId();
		// agentId의 제한 필요?
		// 24byte
		byte[] agnetIdBytes = Bytes.toBytes(agentId);
		long currentTime = jvmInfoThriftDTO.getDataTime();
		return RowKeyUtils.concatFixedByteAndLong(agnetIdBytes, HBaseTables.AGENT_NAME_MAX_LEN, currentTime);
	}
}
