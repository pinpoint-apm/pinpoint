package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.AgentListRowKeyUtils;
import com.navercorp.pinpoint.web.dao.AgentListDao;
import com.navercorp.pinpoint.web.vo.agent.AgentListEntry;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Scan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
@ConditionalOnProperty(name = "pinpoint.web.application.uid.enable", havingValue = "true")
public class HbaseAgentListDao implements AgentListDao {
    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.AGENT_LIST;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<AgentListEntry> simpleAgentInfoRowMapper;

    public HbaseAgentListDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider, RowMapper<AgentListEntry> simpleAgentInfoRowMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.simpleAgentInfoRowMapper = Objects.requireNonNull(simpleAgentInfoRowMapper, "simpleAgentInfoRowMapper");
    }

    @Override
    public List<AgentListEntry> selectAgentListEntry(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKeyPrefix = AgentListRowKeyUtils.createScanPrefix(serviceUid, applicationUid);

        Scan scan = new Scan();
        scan.setCaching(10);
        scan.setStartStopRowForPrefixScan(rowKeyPrefix);
        scan.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName());

        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseOperations.find(agentListTableName, scan, simpleAgentInfoRowMapper);
    }

    @Override
    public List<AgentListEntry> selectAgentListEntry(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId) {
        byte[] rowKeyPrefix = AgentListRowKeyUtils.createScanPrefix(serviceUid, applicationUid, agentId);

        Scan scan = new Scan();
        scan.setCaching(5);
        scan.setStartStopRowForPrefixScan(rowKeyPrefix);
        scan.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName());

        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseOperations.find(agentListTableName, scan, simpleAgentInfoRowMapper);
    }

    @Override
    public void deleteAgents(ServiceUid serviceUid, ApplicationUid applicationUid, List<AgentListEntry> agents) {
        List<Delete> deleteLists = new ArrayList<>(agents.size());
        for (AgentListEntry agentListEntry : agents) {
            byte[] rowKey = AgentListRowKeyUtils.agentListRowKey(serviceUid, applicationUid, agentListEntry.getAgentId(), agentListEntry.getStartTimestamp());

            Delete delete = new Delete(rowKey);
            delete.addColumns(DESCRIPTOR.getName(), DESCRIPTOR.getName());

            deleteLists.add(delete);
        }

        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseOperations.delete(agentListTableName, deleteLists);
    }
}
