package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.AgentIdDao;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.util.AgentIdRowKeyUtils;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class HbaseAgentIdDao implements AgentIdDao {
    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.AGENT_ID;
    private static final byte[] AGENT_STATE_QUALIFIER = HbaseTables.AGENT_ID_STATE_QUALIFIER;

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;

    public HbaseAgentIdDao(HbaseOperations hbaseTemplate, TableNameProvider tableNameProvider) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    @Override
    public void insert(int serviceUid, AgentInfoBo agentInfoBo) {
        byte[] rowKey = AgentIdRowKeyUtils.createRow(serviceUid, agentInfoBo.getApplicationName(), agentInfoBo.getServiceTypeCode(), agentInfoBo.getAgentId(), agentInfoBo.getStartTime());
        byte[] value = createValueBytes(agentInfoBo.getAgentName());

        final Put put = new Put(rowKey, true);
        put.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName(), value);

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.put(applicationIndexTableName, put);
    }

    private byte[] createValueBytes(String agentName) {
        if (!StringUtils.hasText(agentName)) {
            agentName = "";
        }
        AutomaticBuffer buffer = new AutomaticBuffer(32);
        buffer.putByte((byte) 0); // version 0
        buffer.putPrefixedString(agentName);
        return buffer.getBuffer();
    }

    @Override
    public void updateState(int serviceUid, String applicationName, int serviceTypeCode, String agentId, long agentStartTime,
                            long eventTimestamp, AgentLifeCycleState agentLifeCycleState) {
        byte[] rowKey = AgentIdRowKeyUtils.createRow(serviceUid, applicationName, serviceTypeCode, agentId, agentStartTime);
        byte[] value = createStateValueBytes(eventTimestamp, agentLifeCycleState);

        final Put put = new Put(rowKey, true);
        put.addColumn(DESCRIPTOR.getName(), AGENT_STATE_QUALIFIER, value);

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.put(applicationIndexTableName, put);
    }

    private byte[] createStateValueBytes(long eventTimestamp, AgentLifeCycleState state) {
        FixedBuffer buffer = new FixedBuffer(Long.BYTES + Short.BYTES);
        buffer.putLong(eventTimestamp);
        buffer.putShort(state.getCode());
        return buffer.getBuffer();
    }
}
