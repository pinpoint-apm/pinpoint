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
import com.navercorp.pinpoint.uid.dao.ApplicationNameDao;
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
public class HbaseApplicationNameDao implements ApplicationNameDao {

    private static final HbaseColumnFamily NAME = HbaseTables.APPLICATION_NAME;

    private final HbaseOperations hbaseOperations;
    private final AsyncHbaseOperations asyncHbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<String> applicationNameValueMapper;
    private final RowMapper<HbaseCellData> applicationNameCellMapper;

    public HbaseApplicationNameDao(@Qualifier("uidHbaseTemplate") HbaseOperations hbaseOperations,
                                   @Qualifier("uidAsyncTemplate") AsyncHbaseOperations asyncHbaseOperations,
                                   TableNameProvider tableNameProvider,
                                   @Qualifier("applicationNameValueMapper") RowMapper<String> applicationNameValueMapper,
                                   @Qualifier("applicationNameCellMapper") RowMapper<HbaseCellData> applicationNameCellMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.asyncHbaseOperations = Objects.requireNonNull(asyncHbaseOperations, "asyncHbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.applicationNameValueMapper = Objects.requireNonNull(applicationNameValueMapper, "applicationNameValueMapper");
        this.applicationNameCellMapper = Objects.requireNonNull(applicationNameCellMapper, "applicationNameCellMapper");
    }

    @Override
    public String selectApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKey = UidRowKeyCreateUtils.createRowKey(serviceUid, applicationUid);

        Get get = new Get(rowKey);
        get.addColumn(NAME.getName(), NAME.getName());

        TableName applicationNameTableName = tableNameProvider.getTableName(NAME.getTable());
        return hbaseOperations.get(applicationNameTableName, get, applicationNameValueMapper);
    }

    @Override
    public boolean insertApplicationNameIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, String applicationName) {
        CheckAndMutate checkAndMutate = createCheckAndMutate(serviceUid, applicationUid, applicationName);

        TableName applicationNameTableName = tableNameProvider.getTableName(NAME.getTable());
        CheckAndMutateResult checkAndMutateResult = hbaseOperations.checkAndMutate(applicationNameTableName, checkAndMutate);
        return checkAndMutateResult.isSuccess();
    }

    @Override
    public CompletableFuture<Boolean> asyncInsertApplicationNameIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, String applicationName) {
        CheckAndMutate checkAndMutate = createCheckAndMutate(serviceUid, applicationUid, applicationName);

        TableName applicationNameTableName = tableNameProvider.getTableName(NAME.getTable());
        return asyncHbaseOperations.checkAndMutate(applicationNameTableName, checkAndMutate)
                .thenApply(CheckAndMutateResult::isSuccess);
    }

    private CheckAndMutate createCheckAndMutate(ServiceUid serviceUid, ApplicationUid applicationUid, String applicationName) {
        byte[] rowKey = UidRowKeyCreateUtils.createRowKey(serviceUid, applicationUid);

        Put put = new Put(rowKey);
        put.addColumn(NAME.getName(), NAME.getName(), Bytes.toBytes(applicationName));

        CheckAndMutate.Builder builder = CheckAndMutate.newBuilder(rowKey);
        builder.ifNotExists(NAME.getName(), NAME.getName());
        return builder.build(put);
    }

    @Override
    public void deleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        Delete delete = createDelete(serviceUid, applicationUid);

        TableName applicationNameTableName = tableNameProvider.getTableName(NAME.getTable());
        hbaseOperations.delete(applicationNameTableName, delete);
    }

    @Override
    public CompletableFuture<Void> asyncDeleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        Delete delete = createDelete(serviceUid, applicationUid);

        TableName applicationNameTableName = tableNameProvider.getTableName(NAME.getTable());
        return asyncHbaseOperations.delete(applicationNameTableName, delete);
    }

    private Delete createDelete(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKey = UidRowKeyCreateUtils.createRowKey(serviceUid, applicationUid);

        Delete delete = new Delete(rowKey);
        return delete;
    }

    @Override
    public List<HbaseCellData> selectCellData(@Nullable ServiceUid serviceUid) {
        Scan scan = new Scan();
        scan.setCaching(30);
        scan.addColumn(NAME.getName(), NAME.getName());
        if (serviceUid != null) {
            scan.setStartStopRowForPrefixScan(UidRowKeyCreateUtils.createRowKey(serviceUid));
        }

        TableName applicationNameTableName = tableNameProvider.getTableName(NAME.getTable());
        return hbaseOperations.find(applicationNameTableName, scan, applicationNameCellMapper);
    }
}
