package com.navercorp.pinpoint.common.server.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.util.ApplicationRowKeyUtils;
import com.navercorp.pinpoint.common.server.dao.ApplicationDao;
import com.navercorp.pinpoint.common.server.bo.ApplicationFactory;
import com.navercorp.pinpoint.common.server.dao.hbase.mapper.ApplicationMapper;
import com.navercorp.pinpoint.common.server.dao.hbase.mapper.ListMergeResultsExtractor;
import com.navercorp.pinpoint.common.server.bo.Application;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
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
    private final ApplicationMapper applicationMapper;

    public HbaseApplicationDao(HbaseOperations hbaseTemplate, TableNameProvider tableNameProvider,
                               ApplicationFactory applicationFactory) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.applicationMapper = new ApplicationMapper(applicationFactory);
    }

    @Override
    public List<Application> getApplications(int serviceUid) {
        byte[] rowKeyPrefix = ApplicationRowKeyUtils.createPrefix(serviceUid);
        return scanApplications(rowKeyPrefix);
    }

    @Override
    public List<Application> getApplications(int serviceUid, String applicationName) {
        byte[] rowKeyPrefix = ApplicationRowKeyUtils.createPrefix(serviceUid, applicationName);
        return scanApplications(rowKeyPrefix);
    }

    private List<Application> scanApplications(byte[] rowKeyPrefix) {
        Scan scan = new Scan();
        scan.setStartStopRowForPrefixScan(rowKeyPrefix);
        scan.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName());

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseTemplate.find(applicationIndexTableName, scan,
                new ListMergeResultsExtractor<>(applicationMapper));
    }

    @Override
    public void deleteApplication(int serviceUid, String applicationName, int serviceTypeCode) {
        deleteApplication(serviceUid, applicationName, serviceTypeCode, Long.MAX_VALUE);
    }

    @Override
    public void deleteApplication(int serviceUid, String applicationName, int serviceTypeCode, long timestamp) {
        byte[] rowKey = ApplicationRowKeyUtils.createRow(serviceUid, applicationName, serviceTypeCode);
        Delete delete = new Delete(rowKey);
        if (timestamp != Long.MAX_VALUE) {
            delete.setTimestamp(timestamp);
        }

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.delete(applicationIndexTableName, delete);
    }

    @Override
    public void insert(int serviceUid, String applicationName, int serviceTypeCode) {
        byte[] rowKey = ApplicationRowKeyUtils.createRow(serviceUid, applicationName, serviceTypeCode);
        final Put put = new Put(rowKey, true);
        put.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName(), PREFIXED_EMPTY_VALUE);

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.put(applicationIndexTableName, put);
    }
}
