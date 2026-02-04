package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.util.AgentIdRowKeyUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.mapper.AgentStartTimeInfoMapper;
import com.navercorp.pinpoint.web.util.ListListUtils;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class HbaseAgentIdDao implements AgentIdDao {
    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.AGENT_ID;
    private static final byte VERSION_0 = (byte) 0;

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;
    private final ApplicationFactory applicationFactory;

    public HbaseAgentIdDao(HbaseOperations hbaseTemplate, TableNameProvider tableNameProvider,
                           ApplicationFactory applicationFactory) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
    }

    @Override
    public List<AgentIdEntry> getAgentIdEntry(int serviceUid, String applicationName) {
        byte[] rowKeyPrefix = AgentIdRowKeyUtils.createPrefix(serviceUid, applicationName);
        return getAgentIdEntry(rowKeyPrefix, applicationName);
    }

    @Override
    public List<AgentIdEntry> getAgentIdEntry(int serviceUid, String applicationName, int serviceTypeCode) {
        byte[] rowKeyPrefix = AgentIdRowKeyUtils.createPrefix(serviceUid, applicationName, serviceTypeCode);
        return getAgentIdEntry(rowKeyPrefix, applicationName);
    }

    @Override
    public List<AgentIdEntry> getAgentIdEntry(int serviceUid, String applicationName, int serviceTypeCode, String agentId) {
        byte[] rowKeyPrefix = AgentIdRowKeyUtils.createPrefix(serviceUid, applicationName, serviceTypeCode, agentId);
        return getAgentIdEntry(rowKeyPrefix, applicationName);
    }

    private List<AgentIdEntry> getAgentIdEntry(byte[] rowKeyPrefix, String applicationName) {
        Scan scan = createScan(rowKeyPrefix);
        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        RowMapper<List<AgentIdEntry>> agentStartTimeInfoMapper = new AgentStartTimeInfoMapper(applicationFactory, AgentIdRowKeyUtils.createApplicationNamePredicate(applicationName));
        List<List<AgentIdEntry>> results = hbaseTemplate.find(applicationIndexTableName, scan, agentStartTimeInfoMapper);
        return ListListUtils.toList(results);
    }

    private Scan createScan(byte[] rowKeyPrefix) {
        Scan scan = new Scan();
        scan.setStartStopRowForPrefixScan(rowKeyPrefix);
        scan.addFamily(DESCRIPTOR.getName());
        scan.setCaching(100);
        return scan;
    }

    @Override
    public List<AgentIdEntry> getAgentIdEntryByInsertTimeAfter(int serviceUid, String applicationName, int serviceTypeCode, long minUpdateTime) {
        byte[] rowKeyPrefix = AgentIdRowKeyUtils.createPrefix(serviceUid, applicationName, serviceTypeCode);
        Scan scan = createScan(rowKeyPrefix);
        // TODO: create update time column and use it instead of insert time
        try {
            scan.setTimeRange(minUpdateTime, Long.MAX_VALUE);
        } catch (IOException exception) {
            throw new IllegalArgumentException(exception);
        }

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        RowMapper<List<AgentIdEntry>> agentStartTimeInfoMapper = new AgentStartTimeInfoMapper(applicationFactory, AgentIdRowKeyUtils.createApplicationNamePredicate(applicationName));
        List<List<AgentIdEntry>> results = hbaseTemplate.find(applicationIndexTableName, scan, agentStartTimeInfoMapper);
        return ListListUtils.toList(results);
    }

    @Override
    public void delete(int serviceUid, String applicationName, int serviceTypeCode, String agentId, long agentStartTime) {
        byte[] rowKey = AgentIdRowKeyUtils.createRow(serviceUid, applicationName, serviceTypeCode, agentId, agentStartTime);
        Delete delete = new Delete(rowKey);
        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.delete(agentListTableName, delete);
    }

    @Override
    public void delete(List<AgentIdEntry> agentIdEntryList) {
        List<Delete> deletes = new ArrayList<>(agentIdEntryList.size());
        for (AgentIdEntry agentIdEntry : agentIdEntryList) {
            byte[] rowKey = AgentIdRowKeyUtils.createRow(agentIdEntry.getService().getUid(), agentIdEntry.getApplicationName(), agentIdEntry.getServiceTypeCode(), agentIdEntry.getAgentId(), agentIdEntry.getAgentStartTime());
            deletes.add(new Delete(rowKey));
        }
        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.delete(agentListTableName, deletes);
    }

    @Override
    public void insert(int serviceUid, String applicationName, int serviceTypeCode, String agentId, long agentStartTime, String agentName, long timestamp) {
        byte[] row = AgentIdRowKeyUtils.createRow(serviceUid, applicationName, serviceTypeCode, agentId, agentStartTime);
        byte[] value = createValueBytes(agentName);
        final Put put = new Put(row, true);
        if (timestamp > 0) {
            put.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName(), timestamp, value);
        } else {
            put.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName(), value);
        }
        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.put(applicationIndexTableName, put);
    }

    private byte[] createValueBytes(String agentName) {
        if (!StringUtils.hasText(agentName)) {
            agentName = "";
        }
        AutomaticBuffer buffer = new AutomaticBuffer(32);
        buffer.putByte(VERSION_0);
        buffer.putPrefixedString(agentName);
        return buffer.getBuffer();
    }
}
