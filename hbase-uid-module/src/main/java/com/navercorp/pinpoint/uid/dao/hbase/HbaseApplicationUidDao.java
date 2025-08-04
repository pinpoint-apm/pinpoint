package com.navercorp.pinpoint.uid.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.async.AsyncHbaseOperations;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.dao.ApplicationUidDao;
import com.navercorp.pinpoint.uid.utils.UidBytesCreateUtils;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;
import com.navercorp.pinpoint.uid.vo.ApplicationUidRow;
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
public class HbaseApplicationUidDao implements ApplicationUidDao {

    private static final HbaseColumnFamily UID = HbaseTables.APPLICATION_UID;

    private final HbaseOperations hbaseOperations;
    private final AsyncHbaseOperations asyncHbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<ApplicationUid> applicationUidValueMapper;
    private final RowMapper<ApplicationUidRow> applicationUidRowMapper;

    public HbaseApplicationUidDao(@Qualifier("uidHbaseTemplate") HbaseOperations hbaseOperations,
                                  @Qualifier("uidAsyncTemplate") AsyncHbaseOperations asyncHbaseOperations,
                                  TableNameProvider tableNameProvider,
                                  @Qualifier("applicationUidValueMapper") RowMapper<ApplicationUid> applicationUidValueMapper,
                                  @Qualifier("applicationUidRowMapper") RowMapper<ApplicationUidRow> applicationUidRowMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.asyncHbaseOperations = Objects.requireNonNull(asyncHbaseOperations, "asyncHbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.applicationUidValueMapper = Objects.requireNonNull(applicationUidValueMapper, "applicationUidValueMapper");
        this.applicationUidRowMapper = Objects.requireNonNull(applicationUidRowMapper, "applicationUidRowMapper");
    }

    @Override
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute) {
        Get get = createGet(serviceUid, applicationUidAttribute);
        TableName applicationIdTableName = tableNameProvider.getTableName(UID.getTable());
        return hbaseOperations.get(applicationIdTableName, get, applicationUidValueMapper);
    }

    @Override
    public CompletableFuture<ApplicationUid> asyncGetApplicationUid(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute) {
        Get get = createGet(serviceUid, applicationUidAttribute);
        TableName applicationIdTableName = tableNameProvider.getTableName(UID.getTable());
        return asyncHbaseOperations.get(applicationIdTableName, get, applicationUidValueMapper);
    }

    private Get createGet(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute) {
        byte[] rowKey = UidBytesCreateUtils.createApplicationUidRowKey(serviceUid, applicationUidAttribute.applicationName(), applicationUidAttribute.serviceTypeCode());
        Get get = new Get(rowKey);
        get.addColumn(UID.getName(), UID.getName());
        return get;
    }

    @Override
    public boolean putApplicationUidIfNotExists(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute, ApplicationUid applicationUid) {
        CheckAndMutate checkAndMutate = createCheckAndPut(serviceUid, applicationUidAttribute, applicationUid);
        TableName applicationIdTableName = tableNameProvider.getTableName(UID.getTable());

        CheckAndMutateResult checkAndMutateResult = hbaseOperations.checkAndMutate(applicationIdTableName, checkAndMutate);
        return checkAndMutateResult.isSuccess();
    }

    @Override
    public CompletableFuture<Boolean> asyncPutApplicationUidIfNotExists(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute, ApplicationUid applicationUid) {
        CheckAndMutate checkAndMutate = createCheckAndPut(serviceUid, applicationUidAttribute, applicationUid);
        TableName applicationIdTableName = tableNameProvider.getTableName(UID.getTable());

        return asyncHbaseOperations.checkAndMutate(applicationIdTableName, checkAndMutate)
                .thenApply(CheckAndMutateResult::isSuccess);
    }

    private CheckAndMutate createCheckAndPut(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute, ApplicationUid applicationUid) {
        byte[] rowKey = UidBytesCreateUtils.createApplicationUidRowKey(serviceUid, applicationUidAttribute.applicationName(), applicationUidAttribute.serviceTypeCode());
        Put put = new Put(rowKey);
        put.addColumn(UID.getName(), UID.getName(), UidBytesCreateUtils.createApplicationUidValue(applicationUid));
        CheckAndMutate.Builder builder = CheckAndMutate.newBuilder(rowKey);
        builder.ifNotExists(UID.getName(), UID.getName());
        return builder.build(put);
    }

    @Override
    public void deleteApplicationUid(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute) {
        byte[] rowKey = UidBytesCreateUtils.createApplicationUidRowKey(serviceUid, applicationUidAttribute.applicationName(), applicationUidAttribute.serviceTypeCode());
        Delete delete = new Delete(rowKey);
        TableName applicationIdTableName = tableNameProvider.getTableName(UID.getTable());
        hbaseOperations.delete(applicationIdTableName, delete);
    }

    @Override
    public List<ApplicationUidRow> scanApplicationUidRow(ServiceUid serviceUid) {
        Scan scan = createScan(serviceUid);
        TableName applicationIdTableName = tableNameProvider.getTableName(UID.getTable());
        return hbaseOperations.find(applicationIdTableName, scan, applicationUidRowMapper);
    }

    @Override
    public List<ApplicationUidRow> scanApplicationUidRow(ServiceUid serviceUid, String applicationName) {
        Scan scan = createScan(serviceUid, applicationName);

        TableName applicationIdTableName = tableNameProvider.getTableName(UID.getTable());
        return hbaseOperations.find(applicationIdTableName, scan, applicationUidRowMapper);
    }

    private Scan createScan(ServiceUid serviceUid) {
        Scan scan = new Scan();
        scan.setCaching(30);
        scan.addColumn(UID.getName(), UID.getName());
        if (serviceUid != null) {
            scan.setStartStopRowForPrefixScan(UidBytesCreateUtils.createRowKey(serviceUid));
        }
        return scan;
    }

    private Scan createScan(ServiceUid serviceUid, String applicationName) {
        Scan scan = new Scan();
        scan.setCaching(30);
        scan.addColumn(UID.getName(), UID.getName());
        scan.setStartStopRowForPrefixScan(UidBytesCreateUtils.createApplicationUidRowKeyPrefix(serviceUid, applicationName));
        return scan;
    }
}
