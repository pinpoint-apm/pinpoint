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
import com.navercorp.pinpoint.web.uid.dao.ApplicationNameDao;
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
public class HbaseApplicationNameDao implements ApplicationNameDao {

    private static final HbaseColumnFamily NAME = HbaseTables.APPLICATION_NAME;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<String> applicationNameValueMapper;
    private final RowMapper<HbaseCellData> applicationNameCellMapper;

    public HbaseApplicationNameDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider,
                                   @Qualifier("applicationNameValueMapper") RowMapper<String> applicationNameValueMapper,
                                   @Qualifier("applicationNameCellMapper") RowMapper<HbaseCellData> applicationNameCellMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.applicationNameValueMapper = Objects.requireNonNull(applicationNameValueMapper, "applicationNameValueMapper");
        this.applicationNameCellMapper = Objects.requireNonNull(applicationNameCellMapper, "applicationNameCellMapper");
    }

    @Override
    public String selectApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKey = ApplicationUidRowKeyUtils.makeRowKey(serviceUid, applicationUid);

        Get get = new Get(rowKey);
        get.addColumn(NAME.getName(), NAME.getName());

        TableName applicationNameTableName = tableNameProvider.getTableName(NAME.getTable());
        return hbaseOperations.get(applicationNameTableName, get, applicationNameValueMapper);
    }

    @Override
    public void deleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKey = ApplicationUidRowKeyUtils.makeRowKey(serviceUid, applicationUid);

        Delete delete = new Delete(rowKey);

        TableName applicationNameTableName = tableNameProvider.getTableName(NAME.getTable());
        hbaseOperations.delete(applicationNameTableName, delete);
    }

    @Override
    public List<HbaseCellData> selectCellData(@Nullable ServiceUid serviceUid) {
        Scan scan = new Scan();
        scan.setCaching(30);
        scan.addColumn(NAME.getName(), NAME.getName());
        if (serviceUid != null) {
            scan.setStartStopRowForPrefixScan(ApplicationUidRowKeyUtils.makeRowKey(serviceUid));
        }

        TableName applicationNameTableName = tableNameProvider.getTableName(NAME.getTable());
        return hbaseOperations.find(applicationNameTableName, scan, applicationNameCellMapper);
    }
}
