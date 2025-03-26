package com.navercorp.pinpoint.web.uid.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.uid.dao.ServiceUidDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.CheckAndMutateResult;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

// serviceName -> serviceUid
@Repository
@ConditionalOnProperty(name = "pinpoint.web.v4.enable", havingValue = "true")
public class HbaseServiceUidDao implements ServiceUidDao {

    private static final HbaseColumnFamily UID = HbaseTables.SERVICE_UID;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<ServiceUid> serviceUidMapper;
    private final RowMapper<String> serviceUidNameMapper;

    public HbaseServiceUidDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider,
                              @Qualifier("serviceUidMapper") RowMapper<ServiceUid> serviceUidMapper,
                              @Qualifier("serviceUidNameMapper") RowMapper<String> serviceUidNameMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.serviceUidMapper = Objects.requireNonNull(serviceUidMapper, "serviceUidMapper");
        this.serviceUidNameMapper = Objects.requireNonNull(serviceUidNameMapper, "serviceUidNameMapper");
    }

    @Override
    public List<String> selectAllServiceNames() {
        Scan scan = new Scan();
        scan.setCaching(20);
        scan.addColumn(UID.getName(), UID.getName());

        TableName serviceInfoTableName = tableNameProvider.getTableName(UID.getTable());
        return hbaseOperations.find(serviceInfoTableName, scan, serviceUidNameMapper);
    }

    @Override
    public ServiceUid selectServiceUid(String serviceName) {
        byte[] rowKey = Bytes.toBytes(serviceName);

        Get get = new Get(rowKey);
        get.addColumn(UID.getName(), UID.getName());

        TableName serviceUidTableName = tableNameProvider.getTableName(UID.getTable());
        return hbaseOperations.get(serviceUidTableName, get, serviceUidMapper);
    }

    @Override
    public boolean insertServiceUidIfNotExists(String serviceName, ServiceUid serviceUid) {
        byte[] rowKey = Bytes.toBytes(serviceName);

        Put put = new Put(rowKey);
        put.addColumn(UID.getName(), UID.getName(), Bytes.toBytes(serviceUid.getUid()));

        CheckAndMutate.Builder builder = CheckAndMutate.newBuilder(rowKey);
        builder.ifNotExists(UID.getName(), UID.getName());
        CheckAndMutate checkAndMutate = builder.build(put);

        TableName serviceUidTableName = tableNameProvider.getTableName(UID.getTable());
        CheckAndMutateResult checkAndMutateResult = hbaseOperations.checkAndMutate(serviceUidTableName, checkAndMutate);
        return checkAndMutateResult.isSuccess();
    }

    @Override
    public void deleteServiceUid(String serviceName) {
        byte[] rowKey = Bytes.toBytes(serviceName);

        Delete delete = new Delete(rowKey);

        TableName serviceUidTableName = tableNameProvider.getTableName(UID.getTable());
        hbaseOperations.delete(serviceUidTableName, delete);
    }
}
