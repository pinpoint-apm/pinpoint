package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.util.AgentIdRowKeyUtils;
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.util.ListListUtils;
import com.navercorp.pinpoint.web.vo.agent.AgentListItem;
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
    private static final byte VERSION_0 = (byte) 0;
    private static final byte[] EMPTY_VALUE = new byte[]{VERSION_0, 0};

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;
    private final RowMapper<List<AgentListItem>> agentListItemMapper;

    public HbaseAgentIdDao(HbaseOperations hbaseTemplate, TableNameProvider tableNameProvider,
                           @Qualifier("agentListItemMapper") RowMapper<List<AgentListItem>> agentListItemMapper) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.agentListItemMapper = Objects.requireNonNull(agentListItemMapper, "agentListItemMapper");
    }

    @Override
    public List<AgentListItem> getAgentListItems(int serviceUid, String applicationName) {
        byte[] rowKeyPrefix = AgentIdRowKeyUtils.createRowKey(serviceUid, applicationName);
        return getAgentListItems(rowKeyPrefix);
    }

    @Override
    public List<AgentListItem> getAgentListItems(int serviceUid, String applicationName, int serviceTypeCode) {
        byte[] rowKeyPrefix = AgentIdRowKeyUtils.createRowKey(serviceUid, applicationName, serviceTypeCode);
        return getAgentListItems(rowKeyPrefix);
    }

    @Override
    public List<AgentListItem> getAgentListItems(int serviceUid, String applicationName, int serviceTypeCode, String agentId) {
        byte[] rowKeyPrefix = AgentIdRowKeyUtils.createRowKey(serviceUid, applicationName, serviceTypeCode, agentId);
        return getAgentListItems(rowKeyPrefix);
    }

    private List<AgentListItem> getAgentListItems(byte[] rowKeyPrefix) {
        Scan scan = createScan(rowKeyPrefix);
        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        List<List<AgentListItem>> results = hbaseTemplate.find(applicationIndexTableName, scan, agentListItemMapper);
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
    public List<AgentListItem> getAgentListItems(int serviceUid, String applicationName, int serviceTypeCode, long maxTimestamp) {
        byte[] rowKeyPrefix = AgentIdRowKeyUtils.createRowKey(serviceUid, applicationName, serviceTypeCode);
        Scan scan = createScan(rowKeyPrefix);
        try {
            scan.setTimeRange(0L, maxTimestamp);
        } catch (IOException exception) {
            throw new IllegalArgumentException(exception);
        }

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        List<List<AgentListItem>> results = hbaseTemplate.find(applicationIndexTableName, scan, agentListItemMapper);
        return ListListUtils.toList(results);
    }

    @Override
    public void delete(int serviceUid, String applicationName, int serviceTypeCode, String agentId) {
        byte[] rowKey = AgentIdRowKeyUtils.createRowKey(serviceUid, applicationName, serviceTypeCode, agentId);
        Delete delete = new Delete(rowKey);
        TableName agentListTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.delete(agentListTableName, delete);
    }

    @Override
    public void insert(int serviceUid, String applicationName, int serviceTypeCode, String agentId, long agentStartTime, String agentName, long timestamp) {
        byte[] row = AgentIdRowKeyUtils.createRowKey(serviceUid, applicationName, serviceTypeCode, agentId);
        byte[] qualifier = Bytes.toBytes(LongInverter.invert(agentStartTime));
        byte[] value = createValueBytes(agentId, agentName);
        final Put put = new Put(row, true);
        if (timestamp > 0) {
            put.addColumn(DESCRIPTOR.getName(), qualifier, timestamp, value);
        } else {
            put.addColumn(DESCRIPTOR.getName(), qualifier, value);
        }
        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.put(applicationIndexTableName, put);
    }

    private byte[] createValueBytes(String agentId, String agentName) {
        if (agentId.equals(agentName)) {
            return EMPTY_VALUE;
        }
        AutomaticBuffer buffer = new AutomaticBuffer(32);
        buffer.putByte(VERSION_0);
        buffer.putPrefixedString(agentName);
        return buffer.getBuffer();
    }
}
