package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.AgentIdDao;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.util.AgentIdRowKeyUtils;
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class HbaseAgentIdDao implements AgentIdDao {
    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.AGENT_ID;
    private static final byte VERSION_0 = (byte) 0;

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;

    public HbaseAgentIdDao(HbaseOperations hbaseTemplate, TableNameProvider tableNameProvider) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    @Override
    public void insert(int serviceUid, AgentInfoBo agentInfoBo) {
        byte[] rowKey = AgentIdRowKeyUtils.createRowKey(serviceUid, agentInfoBo.getApplicationName(), agentInfoBo.getServiceTypeCode(), agentInfoBo.getAgentId());
        byte[] qualifier = Bytes.toBytes(LongInverter.invert(agentInfoBo.getStartTime()));
        byte[] value = createValueBytes(agentInfoBo.getAgentName());

        final Put put = new Put(rowKey, true);
        put.addColumn(DESCRIPTOR.getName(), qualifier, value);

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.put(applicationIndexTableName, put);
    }

    private byte[] createValueBytes(String agentName) {
        AutomaticBuffer buffer = new AutomaticBuffer(32);
        buffer.putByte(VERSION_0);
        buffer.putPrefixedString(agentName);
        return buffer.getBuffer();
    }
}
