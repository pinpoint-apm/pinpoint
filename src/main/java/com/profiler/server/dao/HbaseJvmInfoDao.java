package com.profiler.server.dao;


import com.profiler.common.dto.thrift.JVMInfoThriftDTO;
import com.profiler.common.hbase.HbaseOperations2;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;


public class HbaseJvmInfoDao implements JvmInfoDao {

    public static final byte[] FamilyJvm = Bytes.toBytes("JVM");
    // 값을 쪼개서 넣지 않고 그냥 넣기로 함.
    public static final byte[] QualifierInfo = Bytes.toBytes("info");


    @Autowired
    private HbaseOperations2 hbaseOperations;

    @Override
    public void insert(final JVMInfoThriftDTO jvmInfoThriftDTO, final byte[] jvmInfoBytes) {
        byte[] rowKey = getRowKey(jvmInfoThriftDTO);
        Put put = new Put(rowKey, jvmInfoThriftDTO.getDataTime());
        put.add(FamilyJvm, QualifierInfo, jvmInfoBytes);

        hbaseOperations.put("SystemInfo", put);
    }

    byte[] getRowKey(JVMInfoThriftDTO jvmInfoThriftDTO) {
        String agentId = jvmInfoThriftDTO.getAgentId();
        // agentId의 제한 필요?
        byte[] agnetIdBytes = Bytes.toBytes(agentId);

        long currentTime = jvmInfoThriftDTO.getDataTime();

        byte[] buffer = new byte[agnetIdBytes.length + 8];
        Bytes.putBytes(buffer, 0, agnetIdBytes, 0, agnetIdBytes.length);
        Bytes.putLong(buffer, agnetIdBytes.length, currentTime);
        return buffer;
    }
}
