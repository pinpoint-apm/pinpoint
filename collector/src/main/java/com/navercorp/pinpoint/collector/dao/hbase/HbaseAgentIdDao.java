package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.AgentIdDao;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.util.ServiceGroupRowKeyPrefixUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class HbaseAgentIdDao implements AgentIdDao {
    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.AGENT_ID;
    private static final byte[] NON_EMPTY_VALUE = new byte[1];

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;

    public HbaseAgentIdDao(HbaseOperations hbaseTemplate, TableNameProvider tableNameProvider) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    @Override
    public void insert(int serviceUid, AgentInfoBo agentInfo) {
        byte[] rowKey = ServiceGroupRowKeyPrefixUtils.createRowKey(serviceUid, agentInfo.getApplicationName(), agentInfo.getServiceTypeCode());
        byte[] qualifier = Bytes.toBytes(agentInfo.getAgentId());

        final Put put = new Put(rowKey, true);
        put.addColumn(DESCRIPTOR.getName(), qualifier, NON_EMPTY_VALUE);

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.put(applicationIndexTableName, put);
    }
}
