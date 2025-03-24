package com.navercorp.pinpoint.collector.uid.dao.hbase;

import com.navercorp.pinpoint.collector.uid.dao.ApplicationNameDao;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.async.AsyncHbaseOperations;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.CheckAndMutateResult;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Repository
@ConditionalOnProperty(value = "pinpoint.collector.application.uid.enable", havingValue = "true")
public class HbaseApplicationNameDao implements ApplicationNameDao {

    private static final HbaseColumnFamily NAME = HbaseTables.APPLICATION_NAME;

    private final HbaseOperations hbaseOperations;
    private final AsyncHbaseOperations asyncHbaseOperations;
    private final TableNameProvider tableNameProvider;

    public HbaseApplicationNameDao(@Qualifier("uidHbaseTemplate") HbaseOperations hbaseOperations,
                                   @Qualifier("uidAsyncTemplate") AsyncHbaseOperations asyncHbaseOperations,
                                   TableNameProvider tableNameProvider) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.asyncHbaseOperations = Objects.requireNonNull(asyncHbaseOperations, "asyncHbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
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
        byte[] rowKey = createRowKey(serviceUid, applicationUid);

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
        byte[] rowKey = createRowKey(serviceUid, applicationUid);

        Delete delete = new Delete(rowKey);
        delete.addColumn(NAME.getName(), NAME.getName());
        return delete;
    }

    private byte[] createRowKey(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKey = new byte[Bytes.SIZEOF_INT + Bytes.SIZEOF_LONG];
        Bytes.putInt(rowKey, 0, serviceUid.getUid());
        Bytes.putLong(rowKey, Bytes.SIZEOF_INT, applicationUid.getUid());
        return rowKey;
    }
}
