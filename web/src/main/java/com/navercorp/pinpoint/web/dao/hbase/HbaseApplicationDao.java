package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.dao.ApplicationDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class HbaseApplicationDao implements ApplicationDao {
    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.APPLICATION;
    private static final byte[] PREFIXED_EMPTY_VALUE = new byte[1];

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;
    private final RowMapper<Application> applicationMapper;

    public HbaseApplicationDao(HbaseOperations hbaseTemplate, TableNameProvider tableNameProvider,
                               @Qualifier("applicationMapper") RowMapper<Application> applicationMapper) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.applicationMapper = Objects.requireNonNull(applicationMapper, "applicationMapper");
    }

    @Override
    public List<Application> getApplications(ServiceUid serviceUid) {
        byte[] rewKeyPrefix = createRowKey(serviceUid);
        return scanApplications(rewKeyPrefix);
    }

    @Override
    public List<Application> getApplications(ServiceUid serviceUid, String applicationName) {
        byte[] rewKeyPrefix = createRowKey(serviceUid, applicationName);
        return scanApplications(rewKeyPrefix);
    }

    private List<Application> scanApplications(byte[] rowKeyPrefix) {
        Scan scan = new Scan();
        scan.setStartStopRowForPrefixScan(rowKeyPrefix);
        scan.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName());
        scan.setCaching(20);

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseTemplate.find(applicationIndexTableName, scan, applicationMapper);
    }

    @Override
    public void deleteApplication(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        byte[] rowKey = createRowKey(serviceUid, applicationName, serviceTypeCode);
        Delete delete = new Delete(rowKey);

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.delete(applicationIndexTableName, delete);
    }

    @Override
    public void insert(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        byte[] rowKey = createRowKey(serviceUid, applicationName, serviceTypeCode);
        final Put put = new Put(rowKey, true);
        put.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName(), PREFIXED_EMPTY_VALUE);

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.put(applicationIndexTableName, put);

    }

    private byte[] createRowKey(ServiceUid serviceUid) {
        Buffer buffer = new AutomaticBuffer();
        buffer.putInt(serviceUid.getUid());
        return buffer.getBuffer();
    }

    private byte[] createRowKey(ServiceUid serviceUid, String ApplicationName) {
        Buffer buffer = new AutomaticBuffer();
        buffer.putInt(serviceUid.getUid());
        buffer.putPrefixedString(ApplicationName);
        return buffer.getBuffer();
    }

    private byte[] createRowKey(ServiceUid serviceUid, String ApplicationName, int serviceTypeCode) {
        Buffer buffer = new AutomaticBuffer();
        buffer.putInt(serviceUid.getUid());
        buffer.putPrefixedString(ApplicationName);
        buffer.putInt(serviceTypeCode);
        return buffer.getBuffer();
    }

}
