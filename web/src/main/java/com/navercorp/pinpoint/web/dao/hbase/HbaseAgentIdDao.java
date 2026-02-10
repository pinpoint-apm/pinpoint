package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.util.ServiceGroupRowKeyPrefixUtils;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.util.ListListUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Repository
public class HbaseAgentIdDao implements AgentIdDao {
    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.AGENT_ID;
    private static final byte[] NON_EMPTY_VALUE = new byte[1];

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;
    private final RowMapper<List<String>> agentIdMapper;

    public HbaseAgentIdDao(HbaseOperations hbaseTemplate, TableNameProvider tableNameProvider,
                           @Qualifier("agentIdMapper") RowMapper<List<String>> agentIdMapper) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.agentIdMapper = Objects.requireNonNull(agentIdMapper, "agentIdMapper");
    }

    @Override
    public List<String> getAgentIds(int serviceUid, String applicationName, int serviceTypeCode) {
        byte[] rowKeyPrefix = ServiceGroupRowKeyPrefixUtils.createRowKey(serviceUid, applicationName, serviceTypeCode);
        Scan scan = createScan(rowKeyPrefix);

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        List<List<String>> results = hbaseTemplate.find(applicationIndexTableName, scan, agentIdMapper);
        return ListListUtils.toList(results);
    }

    @Override
    public List<String> getAgentIds(int serviceUid, String applicationName) {
        byte[] rowKeyPrefix = ServiceGroupRowKeyPrefixUtils.createRowKey(serviceUid, applicationName);
        Scan scan = createScan(rowKeyPrefix);

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        List<List<String>> results = hbaseTemplate.find(applicationIndexTableName, scan, agentIdMapper);
        return ListListUtils.toList(results);
    }

    @Override
    public List<String> getAgentIds(int serviceUid, String applicationName, int serviceTypeCode, long maxTimestamp) {
        byte[] rowKeyPrefix = ServiceGroupRowKeyPrefixUtils.createRowKey(serviceUid, applicationName, serviceTypeCode);
        Scan scan = createScan(rowKeyPrefix);
        try {
            scan.setTimeRange(0L, maxTimestamp);
        } catch (IOException exception) {
            throw new IllegalArgumentException(exception);
        }

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        List<List<String>> results = hbaseTemplate.find(applicationIndexTableName, scan, agentIdMapper);
        return ListListUtils.toList(results);
    }

    private Scan createScan(byte[] rowKeyPrefix) {
        Scan scan = new Scan();
        scan.setStartStopRowForPrefixScan(rowKeyPrefix);
        scan.addFamily(DESCRIPTOR.getName());
        scan.setCaching(5);
        return scan;
    }

    @Override
    public void deleteAllAgents(int serviceUid, String applicationName, int serviceTypeCode) {
        byte[] rowKey = ServiceGroupRowKeyPrefixUtils.createRowKey(serviceUid, applicationName, serviceTypeCode);
        Delete delete = new Delete(rowKey);

        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.delete(agentListTableName, delete);
    }

    @Override
    public void deleteAgents(int serviceUid, String applicationName, int serviceTypeCode, List<String> agentIdList) {
        if (CollectionUtils.isEmpty(agentIdList)) {
            return;
        }
        byte[] rowKey = ServiceGroupRowKeyPrefixUtils.createRowKey(serviceUid, applicationName, serviceTypeCode);
        Delete delete = new Delete(rowKey);
        for (String agentId : agentIdList) {
            byte[] qualifier = Bytes.toBytes(agentId);
            delete.addColumns(DESCRIPTOR.getName(), qualifier);
        }
        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.delete(agentListTableName, delete);
    }

    @Override
    public void insert(int serviceUid, String applicationName, int serviceTypeCode, List<String> agentIdList) {
        if (CollectionUtils.isEmpty(agentIdList)) {
            return;
        }
        byte[] rowKey = ServiceGroupRowKeyPrefixUtils.createRowKey(serviceUid, applicationName, serviceTypeCode);
        final Put put = new Put(rowKey, true);
        for (String agentId : agentIdList) {
            byte[] qualifier = Bytes.toBytes(agentId);
            put.addColumn(DESCRIPTOR.getName(), qualifier, NON_EMPTY_VALUE);
        }

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.put(applicationIndexTableName, put);
    }

}
