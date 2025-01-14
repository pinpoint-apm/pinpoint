package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.config.PinpointIdCacheConfiguration;
import com.navercorp.pinpoint.common.util.UuidUtils;
import com.navercorp.pinpoint.web.dao.ServiceUidDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.CheckAndMutateResult;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.UUID;

// serviceName -> serviceUid
@Repository
public class HbaseServiceUidDao implements ServiceUidDao {

    private static final HbaseColumnFamily.ServiceUid UID = HbaseColumnFamily.SERVICE_UID;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<UUID> serviceUidMapper;

    public HbaseServiceUidDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider,
                              @Qualifier("serviceUidMapper") RowMapper<UUID> serviceUidMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.serviceUidMapper = Objects.requireNonNull(serviceUidMapper, "serviceUidMapper");
    }



    @Override
    @Cacheable(cacheNames = "serviceUidCache", key = "#serviceName", cacheManager = PinpointIdCacheConfiguration.SERVICE_UID_CACHE_NAME, unless = "#result == null")
    public UUID selectServiceUid(String serviceName) {
        byte[] rowKey = Bytes.toBytes(serviceName);

        Get get = new Get(rowKey);
        get.addColumn(UID.getName(), UID.getName());

        TableName serviceUidTableName = tableNameProvider.getTableName(UID.getTable());
        return hbaseOperations.get(serviceUidTableName, get, serviceUidMapper);
    }

    @Override
    public boolean insertServiceUidIfNotExists(String serviceName, UUID serviceUid) {
        byte[] rowKey = Bytes.toBytes(serviceName);

        Put put = new Put(rowKey);
        put.addColumn(UID.getName(), UID.getName(), UuidUtils.toBytes(serviceUid));

        CheckAndMutate.Builder builder = CheckAndMutate.newBuilder(rowKey);
        builder.ifNotExists(UID.getName(), UID.getName());
        CheckAndMutate checkAndMutate = builder.build(put);

        TableName serviceUidTableName = tableNameProvider.getTableName(UID.getTable());
        CheckAndMutateResult checkAndMutateResult = hbaseOperations.checkAndMutate(serviceUidTableName, checkAndMutate);
        return checkAndMutateResult.isSuccess();
    }

    @Override
    @CacheEvict(cacheNames = "serviceUidCache", key = "#serviceName", cacheManager = PinpointIdCacheConfiguration.SERVICE_UID_CACHE_NAME)
    public void deleteServiceUid(String serviceName) {
        byte[] rowKey = Bytes.toBytes(serviceName);

        Delete delete = new Delete(rowKey);

        TableName serviceUidTableName = tableNameProvider.getTableName(UID.getTable());
        hbaseOperations.delete(serviceUidTableName, delete);
    }
}
