package com.navercorp.pinpoint.uid.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.uid.AgentIdentifier;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.dao.AgentNameDao;
import com.navercorp.pinpoint.uid.utils.UidBytesCreateUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
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
    public void insert(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId, long agentStartTime, String agentName) {
        byte[] rowKey = UidBytesCreateUtils.createAgentNameRowKey(serviceUid, applicationUid, agentId, agentStartTime);
        byte[] value = Bytes.toBytes(agentName);
        Put put = new Put(rowKey, true);
        put.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName(), value);

        final TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseOperations.put(agentListTableName, put);
    }

    @Override
    public List<AgentIdentifier> selectAgentIdentifiers(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKeyPrefix = UidBytesCreateUtils.createRowKey(serviceUid, applicationUid);
        return selectByRowKeyPrefix(rowKeyPrefix);
    }

    @Override
    public List<AgentIdentifier> selectAgentIdentifiers(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId) {
        byte[] rowKeyPrefix = UidBytesCreateUtils.createAgentNameRowKey(serviceUid, applicationUid, agentId);
        return selectByRowKeyPrefix(rowKeyPrefix);
    }

    private List<AgentIdentifier> selectByRowKeyPrefix(byte[] rowKeyPrefix) {
        Scan scan = new Scan();
        scan.setCaching(20);
        scan.setStartStopRowForPrefixScan(rowKeyPrefix);
        scan.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName());
        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseOperations.find(agentListTableName, scan, agentNameMapper);
    }

    @Override
    public void deleteAgents(ServiceUid serviceUid, ApplicationUid applicationUid, List<AgentIdentifier> agents) {
        List<Delete> deleteLists = new ArrayList<>(agents.size());
        for (AgentIdentifier agentIdentifier : agents) {
            byte[] rowKey = UidBytesCreateUtils.createAgentNameRowKey(serviceUid, applicationUid, agentIdentifier.getId(), agentIdentifier.getStartTimestamp());
            Delete delete = new Delete(rowKey);
            delete.addColumns(DESCRIPTOR.getName(), DESCRIPTOR.getName());
            deleteLists.add(delete);
        }

        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseOperations.delete(agentListTableName, deleteLists);
    }
}
