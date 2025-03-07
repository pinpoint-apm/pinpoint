package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.dao.ApplicationNameDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ConditionalOnProperty(name = "pinpoint.web.application.uid.enable", havingValue = "true")
public class HbaseApplicationNameDao implements ApplicationNameDao {

    private static final HbaseColumnFamily.ApplicationInfo NAME = HbaseColumnFamily.APPLICATION_NAME;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<String> applicationNameMapper;

    public HbaseApplicationNameDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider,
                                   @Qualifier("applicationUidNameMapper") RowMapper<String> applicationNameMapper) {
        this.hbaseOperations = hbaseOperations;
        this.tableNameProvider = tableNameProvider;
        this.applicationNameMapper = applicationNameMapper;
    }

    @Override
    public List<String> selectApplicationNames(ServiceUid serviceUid) {
        byte[] rowKeyPrefix = Bytes.toBytes(serviceUid.getUid());

        Scan scan = new Scan();
        scan.setCaching(10);
        scan.setStartStopRowForPrefixScan(rowKeyPrefix);
        scan.addColumn(NAME.getName(), NAME.getName());

        TableName applicationNameTableName = tableNameProvider.getTableName(NAME.getTable());
        return hbaseOperations.find(applicationNameTableName, scan, applicationNameMapper);
    }

    @Override
    public String selectApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKey = createRowKey(serviceUid, applicationUid);

        Get get = new Get(rowKey);
        get.addColumn(NAME.getName(), NAME.getName());

        TableName applicationNameTableName = tableNameProvider.getTableName(NAME.getTable());
        return hbaseOperations.get(applicationNameTableName, get, applicationNameMapper);
    }

    @Override
    public void deleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKey = createRowKey(serviceUid, applicationUid);

        Delete delete = new Delete(rowKey);
        delete.addColumn(NAME.getName(), NAME.getName());

        TableName applicationNameTableName = tableNameProvider.getTableName(NAME.getTable());
        hbaseOperations.delete(applicationNameTableName, delete);
    }

    private byte[] createRowKey(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKey = new byte[4 + 8];
        Bytes.putInt(rowKey, 0, serviceUid.getUid());
        Bytes.putLong(rowKey, 4, applicationUid.getUid());
        return rowKey;
    }

}
