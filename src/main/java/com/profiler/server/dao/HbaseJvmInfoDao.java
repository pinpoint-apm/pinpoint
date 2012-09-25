package com.profiler.server.dao;


import com.profiler.common.dto.thrift.JVMInfoThriftDTO;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseOperations;
import org.springframework.data.hadoop.hbase.TableCallback;


public class HbaseJvmInfoDao implements JvmInfoDao {

    public static final byte[] FamilyJvm = Bytes.toBytes("JVM");
    // 값을 쪼개서 넣지 않고 그냥 넣기로 함.
    public static final byte[] QualifierInfo = Bytes.toBytes("info");


    @Autowired
    private HbaseOperations hbaseOperations;

    @Override
    public void insert(final JVMInfoThriftDTO jvmInfoThriftDTO, final byte[] jvmInfoBytes) {
        hbaseOperations.execute("SystemInfo", new TableCallback<Object>() {
            @Override
            public Object doInTable(HTable table) throws Throwable {
                byte[] rowKey = getRowKey(jvmInfoThriftDTO);

                Put put = new Put(rowKey, jvmInfoThriftDTO.getDataTime());
                put.add(FamilyJvm, QualifierInfo, jvmInfoBytes);
                table.put(put);

                return null;
            }
        });
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
