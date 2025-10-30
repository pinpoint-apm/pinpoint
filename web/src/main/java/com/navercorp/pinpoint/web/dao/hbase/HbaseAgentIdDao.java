package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class HbaseAgentIdDao implements AgentIdDao {
    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.AGENT_ID;
    private static final byte[] PREFIXED_EMPTY_VALUE = new byte[1];

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;
    private final RowMapper<String> agentMapper;

    public HbaseAgentIdDao(HbaseOperations hbaseTemplate, TableNameProvider tableNameProvider,
                           @Qualifier("agentIdMapperV2") RowMapper<String> agentMapper) {
        this.hbaseTemplate = hbaseTemplate;
        this.tableNameProvider = tableNameProvider;
        this.agentMapper = agentMapper;
    }

    @Override
    public List<String> getAgentIds(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        byte[] rowKeyPrefix = createRowKey(serviceUid, applicationName, serviceTypeCode);
        Scan scan = createScan(rowKeyPrefix);

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseTemplate.find(applicationIndexTableName, scan, agentMapper);
    }

    @Override
    public List<String> getAgentIds(ServiceUid serviceUid, String applicationName) {
        byte[] rowKeyPrefix = createRowKey(serviceUid, applicationName);
        Scan scan = createScan(rowKeyPrefix);

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseTemplate.find(applicationIndexTableName, scan, agentMapper);
    }

    private Scan createScan(byte[] rowKeyPrefix) {
        Scan scan = new Scan();
        scan.setStartStopRowForPrefixScan(rowKeyPrefix);
        scan.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName());
        scan.setCaching(20);
        return scan;
    }

    @Override
    public void deleteAgents(ServiceUid serviceUid, String applicationName, int serviceTypeCode, List<String> agentIdList) {
        List<Delete> deleteLists = new ArrayList<>(agentIdList.size());
        for (String agentId : agentIdList) {
            byte[] rowKey = createRowKey(serviceUid, applicationName, serviceTypeCode, agentId);
            Delete delete = new Delete(rowKey);

            delete.addColumns(DESCRIPTOR.getName(), DESCRIPTOR.getName());
            deleteLists.add(delete);
        }
        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.delete(agentListTableName, deleteLists);
    }

    @Override
    public void insert(ServiceUid serviceUid, String applicationName, int serviceTypeCode, String agentId) {
        byte[] rowKey = createRowKey(serviceUid, applicationName, serviceTypeCode, agentId);
        final Put put = new Put(rowKey, true);
        put.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName(), PREFIXED_EMPTY_VALUE);

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.put(applicationIndexTableName, put);
    }

    private byte[] createRowKey(ServiceUid serviceUid, String ApplicationName, int serviceTypeCode, String agentId) {
        Buffer buffer = new AutomaticBuffer();
        buffer.putInt(serviceUid.getUid());
        buffer.putPrefixedString(ApplicationName);
        buffer.putInt(serviceTypeCode);
        buffer.putPrefixedString(agentId);
        return buffer.getBuffer();
    }

    private byte[] createRowKey(ServiceUid serviceUid, String ApplicationName, int serviceTypeCode) {
        Buffer buffer = new AutomaticBuffer();
        buffer.putInt(serviceUid.getUid());
        buffer.putPrefixedString(ApplicationName);
        buffer.putInt(serviceTypeCode);
        return buffer.getBuffer();
    }

    private byte[] createRowKey(ServiceUid serviceUid, String ApplicationName) {
        Buffer buffer = new AutomaticBuffer();
        buffer.putInt(serviceUid.getUid());
        buffer.putPrefixedString(ApplicationName);
        return buffer.getBuffer();
    }

}
