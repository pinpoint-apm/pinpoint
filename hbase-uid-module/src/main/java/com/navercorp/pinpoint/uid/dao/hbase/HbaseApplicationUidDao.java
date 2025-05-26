package com.navercorp.pinpoint.uid.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.async.AsyncHbaseOperations;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.HbaseCellData;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.dao.ApplicationUidDao;
import com.navercorp.pinpoint.uid.utils.UidRowKeyCreateUtils;
import jakarta.annotation.Nullable;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.CheckAndMutateResult;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Repository
public class HbaseApplicationUidDao implements ApplicationUidDao {

    private static final HbaseColumnFamily UID = HbaseTables.APPLICATION_UID;

    private final HbaseOperations hbaseOperations;
    private final AsyncHbaseOperations asyncHbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<ApplicationUid> applicationUidValueMapper;
    private final RowMapper<String> applicationUidNameMapper;
    private final RowMapper<HbaseCellData> applicationUidCellMapper;

    public HbaseApplicationUidDao(@Qualifier("uidHbaseTemplate") HbaseOperations hbaseOperations,
                                  @Qualifier("uidAsyncTemplate") AsyncHbaseOperations asyncHbaseOperations,
                                  TableNameProvider tableNameProvider,
                                  @Qualifier("applicationUidMapper") RowMapper<ApplicationUid> applicationUidValueMapper,
                                  @Qualifier("applicationUidNameMapper") RowMapper<String> applicationUidNameMapper,
                                  @Qualifier("applicationUidCellMapper") RowMapper<HbaseCellData> applicationUidCellMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.asyncHbaseOperations = Objects.requireNonNull(asyncHbaseOperations, "asyncHbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.applicationUidValueMapper = Objects.requireNonNull(applicationUidValueMapper, "applicationUidValueMapper");
        this.applicationUidNameMapper = Objects.requireNonNull(applicationUidNameMapper, "applicationUidNameMapper");
        this.applicationUidCellMapper = Objects.requireNonNull(applicationUidCellMapper, "applicationUidCellMapper");
    }

    @Override
    public ApplicationUid selectApplicationUid(ServiceUid serviceUid, String applicationName) {
        Get get = createGet(serviceUid, applicationName);
        TableName applicationIdTableName = tableNameProvider.getTableName(UID.getTable());
        return hbaseOperations.get(applicationIdTableName, get, applicationUidValueMapper);
    }

    @Override
    public CompletableFuture<ApplicationUid> asyncSelectApplicationUid(ServiceUid serviceUid, String applicationName) {
        Get get = createGet(serviceUid, applicationName);
        TableName applicationIdTableName = tableNameProvider.getTableName(UID.getTable());
        return asyncHbaseOperations.get(applicationIdTableName, get, applicationUidValueMapper);
    }

    private Get createGet(ServiceUid serviceUid, String applicationName) {
        byte[] rowKey = UidRowKeyCreateUtils.createApplicationUidRowKey(serviceUid, applicationName);
        Get get = new Get(rowKey);
        get.addColumn(UID.getName(), UID.getName());
        return get;
    }

    @Override
    public boolean insertApplicationUidIfNotExists(ServiceUid serviceUid, String applicationName, ApplicationUid applicationUid) {
        CheckAndMutate checkAndMutate = createCheckAndPut(serviceUid, applicationName, applicationUid);
        TableName applicationIdTableName = tableNameProvider.getTableName(UID.getTable());

        CheckAndMutateResult checkAndMutateResult = hbaseOperations.checkAndMutate(applicationIdTableName, checkAndMutate);
        return checkAndMutateResult.isSuccess();
    }

    @Override
    public CompletableFuture<Boolean> asyncInsertApplicationUidIfNotExists(ServiceUid serviceUid, String applicationName, ApplicationUid applicationUid) {
        CheckAndMutate checkAndMutate = createCheckAndPut(serviceUid, applicationName, applicationUid);
        TableName applicationIdTableName = tableNameProvider.getTableName(UID.getTable());

        return asyncHbaseOperations.checkAndMutate(applicationIdTableName, checkAndMutate)
                .thenApply(CheckAndMutateResult::isSuccess);
    }

    private CheckAndMutate createCheckAndPut(ServiceUid serviceUid, String applicationName, ApplicationUid applicationUid) {
        byte[] rowKey = UidRowKeyCreateUtils.createApplicationUidRowKey(serviceUid, applicationName);
        Put put = new Put(rowKey);
        put.addColumn(UID.getName(), UID.getName(), Bytes.toBytes(applicationUid.getUid()));
        CheckAndMutate.Builder builder = CheckAndMutate.newBuilder(rowKey);
        builder.ifNotExists(UID.getName(), UID.getName());
        return builder.build(put);
    }

    @Override
    public List<String> selectApplicationNames(ServiceUid serviceUid) {
        Scan scan = createScan(serviceUid);
        TableName applicationIdTableName = tableNameProvider.getTableName(UID.getTable());
        return hbaseOperations.find(applicationIdTableName, scan, applicationUidNameMapper);
    }

    private Scan createScan(ServiceUid serviceUid) {
        Scan scan = new Scan();
        scan.setCaching(30);
        scan.addColumn(UID.getName(), UID.getName());
        if (serviceUid != null) {
            scan.setStartStopRowForPrefixScan(UidRowKeyCreateUtils.createRowKey(serviceUid));
        }
        return scan;
    }

    @Override
    public void deleteApplicationUid(ServiceUid serviceUid, String applicationName) {
        byte[] rowKey = UidRowKeyCreateUtils.createApplicationUidRowKey(serviceUid, applicationName);
        Delete delete = new Delete(rowKey);
        TableName applicationIdTableName = tableNameProvider.getTableName(UID.getTable());
        hbaseOperations.delete(applicationIdTableName, delete);
    }

    @Override
    public List<HbaseCellData> selectCellData(@Nullable ServiceUid serviceUid) {
        Scan scan = createScan(serviceUid);
        TableName applicationNameTableName = tableNameProvider.getTableName(UID.getTable());
        return hbaseOperations.find(applicationNameTableName, scan, applicationUidCellMapper);
    }
}
