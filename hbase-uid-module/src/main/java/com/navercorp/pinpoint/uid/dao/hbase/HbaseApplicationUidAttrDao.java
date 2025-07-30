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
import com.navercorp.pinpoint.uid.dao.ApplicationUidAttrDao;
import com.navercorp.pinpoint.uid.utils.UidBytesCreateUtils;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;
import jakarta.annotation.Nullable;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.CheckAndMutateResult;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Repository
public class HbaseApplicationUidAttrDao implements ApplicationUidAttrDao {

    private static final HbaseColumnFamily APPLICATION_ATTR = HbaseTables.APPLICATION_UID_ATTR;

    private final HbaseOperations hbaseOperations;
    private final AsyncHbaseOperations asyncHbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<ApplicationUidAttribute> applicationUidInfoValueMapper;
    private final RowMapper<HbaseCellData> applicationUidInfoCellMapper;

    public HbaseApplicationUidAttrDao(@Qualifier("uidHbaseTemplate") HbaseOperations hbaseOperations,
                                      @Qualifier("uidAsyncTemplate") AsyncHbaseOperations asyncHbaseOperations,
                                      TableNameProvider tableNameProvider,
                                      @Qualifier("applicationUidInfoValueMapper") RowMapper<ApplicationUidAttribute> applicationUidInfoValueMapper,
                                      @Qualifier("applicationUidInfoCellMapper") RowMapper<HbaseCellData> applicationUidInfoCellMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.asyncHbaseOperations = Objects.requireNonNull(asyncHbaseOperations, "asyncHbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.applicationUidInfoValueMapper = Objects.requireNonNull(applicationUidInfoValueMapper, "applicationUidInfoValueMapper");
        this.applicationUidInfoCellMapper = Objects.requireNonNull(applicationUidInfoCellMapper, "applicationNameCellMapper");
    }

    @Override
    public ApplicationUidAttribute selectApplicationInfo(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKey = UidBytesCreateUtils.createRowKey(serviceUid, applicationUid);

        Get get = new Get(rowKey);
        get.addColumn(APPLICATION_ATTR.getName(), APPLICATION_ATTR.getName());

        TableName applicationNameTableName = tableNameProvider.getTableName(APPLICATION_ATTR.getTable());
        return hbaseOperations.get(applicationNameTableName, get, applicationUidInfoValueMapper);
    }

    @Override
    public boolean insertApplicationNameIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, ApplicationUidAttribute applicationUidAttribute) {
        CheckAndMutate checkAndMutate = createCheckAndMutate(serviceUid, applicationUid, applicationUidAttribute);

        TableName applicationNameTableName = tableNameProvider.getTableName(APPLICATION_ATTR.getTable());
        CheckAndMutateResult checkAndMutateResult = hbaseOperations.checkAndMutate(applicationNameTableName, checkAndMutate);
        return checkAndMutateResult.isSuccess();
    }

    @Override
    public CompletableFuture<Boolean> asyncInsertApplicationNameIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, ApplicationUidAttribute applicationUidAttribute) {
        CheckAndMutate checkAndMutate = createCheckAndMutate(serviceUid, applicationUid, applicationUidAttribute);

        TableName applicationNameTableName = tableNameProvider.getTableName(APPLICATION_ATTR.getTable());
        return asyncHbaseOperations.checkAndMutate(applicationNameTableName, checkAndMutate)
                .thenApply(CheckAndMutateResult::isSuccess);
    }

    private CheckAndMutate createCheckAndMutate(ServiceUid serviceUid, ApplicationUid applicationUid, ApplicationUidAttribute applicationUidAttribute) {
        byte[] rowKey = UidBytesCreateUtils.createRowKey(serviceUid, applicationUid);
        byte[] value = UidBytesCreateUtils.createApplicationUidAttrValue(applicationUidAttribute.applicationName(), applicationUidAttribute.serviceTypeCode());

        Put put = new Put(rowKey);
        put.addColumn(APPLICATION_ATTR.getName(), APPLICATION_ATTR.getName(), value);

        CheckAndMutate.Builder builder = CheckAndMutate.newBuilder(rowKey);
        builder.ifNotExists(APPLICATION_ATTR.getName(), APPLICATION_ATTR.getName());
        return builder.build(put);
    }

    @Override
    public void deleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        Delete delete = createDelete(serviceUid, applicationUid);

        TableName applicationNameTableName = tableNameProvider.getTableName(APPLICATION_ATTR.getTable());
        hbaseOperations.delete(applicationNameTableName, delete);
    }

    @Override
    public CompletableFuture<Void> asyncDeleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        Delete delete = createDelete(serviceUid, applicationUid);

        TableName applicationNameTableName = tableNameProvider.getTableName(APPLICATION_ATTR.getTable());
        return asyncHbaseOperations.delete(applicationNameTableName, delete);
    }

    private Delete createDelete(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKey = UidBytesCreateUtils.createRowKey(serviceUid, applicationUid);

        Delete delete = new Delete(rowKey);
        return delete;
    }

    @Override
    public List<HbaseCellData> selectCellData(@Nullable ServiceUid serviceUid) {
        Scan scan = new Scan();
        scan.setCaching(30);
        scan.addColumn(APPLICATION_ATTR.getName(), APPLICATION_ATTR.getName());
        if (serviceUid != null) {
            scan.setStartStopRowForPrefixScan(UidBytesCreateUtils.createRowKey(serviceUid));
        }

        TableName applicationNameTableName = tableNameProvider.getTableName(APPLICATION_ATTR.getTable());
        return hbaseOperations.find(applicationNameTableName, scan, applicationUidInfoCellMapper);
    }
}
