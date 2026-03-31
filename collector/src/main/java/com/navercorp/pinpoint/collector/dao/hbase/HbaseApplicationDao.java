package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.ApplicationDao;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.util.ApplicationRowKeyUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class HbaseApplicationDao implements ApplicationDao {
    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.APPLICATION;
    private static final byte[] PREFIXED_EMPTY_VALUE = new byte[1];

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;

    public HbaseApplicationDao(HbaseOperations hbaseTemplate, TableNameProvider tableNameProvider) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    @Override
    public void insert(int serviceUid, String applicationName, int serviceTypeCode) {
        byte[] rowKey = ApplicationRowKeyUtils.createRow(serviceUid, applicationName, serviceTypeCode);
        final Put put = new Put(rowKey, true);
        put.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName(), PREFIXED_EMPTY_VALUE);

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.put(applicationIndexTableName, put);
    }

    @Override
    public List<Integer> selectServiceTypeCodes(int serviceUid, String applicationName) {
        byte[] rowKeyPrefix = ApplicationRowKeyUtils.createPrefix(serviceUid, applicationName);

        Scan scan = new Scan();
        scan.setStartStopRowForPrefixScan(rowKeyPrefix);
        scan.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName());

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        List<Integer> results = hbaseTemplate.find(applicationIndexTableName, scan, serviceTypeCodeMapper());
        results.removeIf(Objects::isNull);
        return results;
    }

    private RowMapper<Integer> serviceTypeCodeMapper() {
        return (result, rowNum) -> {
            if (result.isEmpty()) {
                return null;
            }
            return ApplicationRowKeyUtils.extractServiceTypeCode(result.getRow());
        };
    }
}
