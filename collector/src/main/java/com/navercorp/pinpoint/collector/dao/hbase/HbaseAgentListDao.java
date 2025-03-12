package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.AgentListDao;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.AgentListRowKeyUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
@ConditionalOnProperty(name = "pinpoint.collector.application.uid.enable", havingValue = "true")
public class HbaseAgentListDao implements AgentListDao {

    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.AGENT_LIST;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    public HbaseAgentListDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    @Override
    public void insert(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId, long agentStartTime, String agentName) {
        byte[] rowKey = AgentListRowKeyUtils.makeRowKey(serviceUid, applicationUid, agentId, agentStartTime);
        byte[] value = Bytes.toBytes(agentName);

        Put put = new Put(rowKey, true);
        put.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName(), value);

        final TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseOperations.put(agentListTableName, put);
    }
}
