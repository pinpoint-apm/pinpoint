package com.navercorp.pinpoint.web.uid.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.HbaseCellData;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.ApplicationUidRowKeyUtils;
import com.navercorp.pinpoint.web.uid.dao.ApplicationUidDao;
import jakarta.annotation.Nullable;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Scan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
@ConditionalOnProperty(name = "pinpoint.web.application.uid.enable", havingValue = "true")
public class HbaseApplicationUidDao implements ApplicationUidDao {

    private final HbaseColumnFamily UID = HbaseTables.APPLICATION_UID;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<ApplicationUid> applicationUidMapper;
    private final RowMapper<String> applicationUidNameMapper;
    private final RowMapper<HbaseCellData> applicationUidCellMapper;

    public HbaseApplicationUidDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider,
                                  @Qualifier("applicationUidMapper") RowMapper<ApplicationUid> applicationUidMapper,
                                  @Qualifier("applicationUidNameMapper") RowMapper<String> applicationUidNameMapper,
                                  @Qualifier("applicationUidCellMapper") RowMapper<HbaseCellData> applicationUidCellMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.applicationUidMapper = Objects.requireNonNull(applicationUidMapper, "applicationUidMapper");
        this.applicationUidNameMapper = Objects.requireNonNull(applicationUidNameMapper, "applicationUidNameMapper");
        this.applicationUidCellMapper = Objects.requireNonNull(applicationUidCellMapper, "applicationUidCellMapper");
    }

    @Override
    public List<String> selectApplicationNames(ServiceUid serviceUid) {
        byte[] rowKeyPrefix = ApplicationUidRowKeyUtils.makeRowKey(serviceUid);

        Scan scan = new Scan();
        scan.setCaching(30);
        scan.setStartStopRowForPrefixScan(rowKeyPrefix);
        scan.addColumn(UID.getName(), UID.getName());

        TableName applicationIdTableName = tableNameProvider.getTableName(UID.getTable());
        return hbaseOperations.find(applicationIdTableName, scan, applicationUidNameMapper);
    }

    @Override
    public ApplicationUid selectApplication(ServiceUid serviceUid, String applicationName) {
        byte[] rowKey = ApplicationUidRowKeyUtils.makeRowKey(serviceUid, applicationName);

        Get get = new Get(rowKey);
        get.addColumn(UID.getName(), UID.getName());

        TableName applicationIdTableName = tableNameProvider.getTableName(UID.getTable());
        return hbaseOperations.get(applicationIdTableName, get, applicationUidMapper);
    }

    @Override
    public void deleteApplicationUid(ServiceUid serviceUid, String applicationName) {
        byte[] rowKey = ApplicationUidRowKeyUtils.makeRowKey(serviceUid, applicationName);

        Delete delete = new Delete(rowKey);

        TableName applicationIdTableName = tableNameProvider.getTableName(UID.getTable());
        hbaseOperations.delete(applicationIdTableName, delete);
    }

    @Override
    public List<HbaseCellData> selectCellData(@Nullable ServiceUid serviceUid) {
        Scan scan = new Scan();
        scan.setCaching(30);
        scan.addColumn(UID.getName(), UID.getName());
        if (serviceUid != null) {
            scan.setStartStopRowForPrefixScan(ApplicationUidRowKeyUtils.makeRowKey(serviceUid));
        }

        TableName applicationNameTableName = tableNameProvider.getTableName(UID.getTable());
        return hbaseOperations.find(applicationNameTableName, scan, applicationUidCellMapper);
    }
}
