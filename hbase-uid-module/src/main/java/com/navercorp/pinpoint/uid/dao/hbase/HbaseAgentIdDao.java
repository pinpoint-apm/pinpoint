package com.navercorp.pinpoint.uid.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.uid.AgentIdentifier;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.dao.AgentIdDao;
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
public class HbaseAgentIdDao implements AgentIdDao {

    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.AGENT_ID;
    private static final byte[] VERSION = Bytes.toBytes(1);

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<String> agentNameMapper;

    public HbaseAgentIdDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider,
                           @Qualifier("agentNameMapper") RowMapper<String> agentNameMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.agentNameMapper = Objects.requireNonNull(agentNameMapper, "agentNameMapper");
    }

    @Override
    public void insert(ServiceUid serviceUid, ApplicationUid applicationUid, String agentId) {
        byte[] rowKey = UidBytesCreateUtils.createAgentNameRowKey(serviceUid, applicationUid, agentId);
        Put put = new Put(rowKey, true);
        put.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName(), VERSION);

        final TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseOperations.put(agentListTableName, put);
    }

    @Override
    public List<String> scanAgentId(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKeyPrefix = UidBytesCreateUtils.createRowKey(serviceUid, applicationUid);
        Scan scan = createScan(rowKeyPrefix);

        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseOperations.find(agentListTableName, scan, agentNameMapper);
    }

    @Override
    public List<List<String>> scanAgentId(ServiceUid serviceUid, List<ApplicationUid> applicationUidList) {
        List<Scan> scans = new ArrayList<>(applicationUidList.size());
        for (ApplicationUid applicationUid : applicationUidList) {
            byte[] rowKey = UidBytesCreateUtils.createRowKey(serviceUid, applicationUid);
            Scan scan = createScan(rowKey);
            scans.add(scan);
        }
        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseOperations.findParallel(agentListTableName, scans, agentNameMapper);
    }

    private Scan createScan(byte[] rowKeyPrefix) {
        Scan scan = new Scan();
        scan.setStartStopRowForPrefixScan(rowKeyPrefix);
        scan.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName());
        scan.setCaching(20);
        return scan;
    }

    @Override
    public void deleteAgents(ServiceUid serviceUid, ApplicationUid applicationUid, List<String> agentIdList) {
        List<Delete> deleteLists = new ArrayList<>(agentIdList.size());
        for (String agentId : agentIdList) {
            byte[] rowKey = UidBytesCreateUtils.createAgentNameRowKey(serviceUid, applicationUid, agentId);
            Delete delete = new Delete(rowKey);
            delete.addColumns(DESCRIPTOR.getName(), DESCRIPTOR.getName());
            deleteLists.add(delete);
        }
        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseOperations.delete(agentListTableName, deleteLists);
    }
}
