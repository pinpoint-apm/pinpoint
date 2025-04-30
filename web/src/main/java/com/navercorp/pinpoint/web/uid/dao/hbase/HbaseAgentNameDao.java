package com.navercorp.pinpoint.web.uid.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.uid.AgentIdentifier;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.AgentListRowKeyUtils;
import com.navercorp.pinpoint.web.uid.dao.AgentNameDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Scan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
@ConditionalOnProperty(name = "pinpoint.web.application.uid.enable", havingValue = "true")
public class HbaseAgentNameDao implements AgentNameDao {
    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.AGENT_NAME;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<AgentIdentifier> agentNameMapper;

    public HbaseAgentNameDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider,
                             @Qualifier("agentNameMapper") RowMapper<AgentIdentifier> agentNameMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.agentNameMapper = Objects.requireNonNull(agentNameMapper, "agentNameMapper");
    }

    @Override
    public List<AgentIdentifier> selectAgentIdentifiers(ServiceUid serviceUid) {
        byte[] rowKeyPrefix = AgentListRowKeyUtils.makeRowKey(serviceUid);
        Scan scan = createScan(rowKeyPrefix);

        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseOperations.find(agentListTableName, scan, agentNameMapper);
    }

    @Override
    public List<AgentIdentifier> selectAgentIdentifiers(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKeyPrefix = AgentListRowKeyUtils.makeRowKey(serviceUid, applicationUid);
        Scan scan = createScan(rowKeyPrefix);

        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseOperations.find(agentListTableName, scan, agentNameMapper);
    }

    @Override
    public List<AgentIdentifier> selectAgentIdentifiers(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId) {
        byte[] rowKeyPrefix = AgentListRowKeyUtils.makeRowKey(serviceUid, applicationUid, agentId);
        Scan scan = createScan(rowKeyPrefix);

        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseOperations.find(agentListTableName, scan, agentNameMapper);
    }

    private Scan createScan(byte[] rowKeyPrefix) {
        Scan scan = new Scan();
        scan.setCaching(20);
        scan.setStartStopRowForPrefixScan(rowKeyPrefix);
        scan.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName());
        return scan;
    }

    @Override
    public void deleteAgents(ServiceUid serviceUid, ApplicationUid applicationUid, List<AgentIdentifier> agents) {
        List<Delete> deleteLists = new ArrayList<>(agents.size());
        for (AgentIdentifier agentIdentifier : agents) {
            byte[] rowKey = AgentListRowKeyUtils.makeRowKey(serviceUid, applicationUid, agentIdentifier.getId(), agentIdentifier.getStartTimestamp());

            Delete delete = new Delete(rowKey);
            delete.addColumns(DESCRIPTOR.getName(), DESCRIPTOR.getName());

            deleteLists.add(delete);
        }

        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseOperations.delete(agentListTableName, deleteLists);
    }
}
