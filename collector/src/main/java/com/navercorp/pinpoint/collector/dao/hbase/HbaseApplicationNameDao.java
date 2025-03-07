package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.ApplicationNameDao;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.CheckAndMutateResult;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class HbaseApplicationNameDao implements ApplicationNameDao {

    private static final HbaseColumnFamily.ApplicationInfo NAME = HbaseColumnFamily.APPLICATION_NAME;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    public HbaseApplicationNameDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    @Override
    public boolean insertApplicationNameIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, String applicationName) {
        byte[] rowKey = createRowKey(serviceUid, applicationUid);

        Put put = new Put(rowKey);
        put.addColumn(NAME.getName(), NAME.getName(), Bytes.toBytes(applicationName));

        CheckAndMutate.Builder builder = CheckAndMutate.newBuilder(rowKey);
        builder.ifNotExists(NAME.getName(), NAME.getName());
        CheckAndMutate checkAndMutate = builder.build(put);

        TableName applicationNameTableName = tableNameProvider.getTableName(NAME.getTable());
        CheckAndMutateResult checkAndMutateResult = hbaseOperations.checkAndMutate(applicationNameTableName, checkAndMutate);
        return checkAndMutateResult.isSuccess();
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
