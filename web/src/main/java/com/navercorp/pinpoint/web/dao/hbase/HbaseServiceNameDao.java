package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.config.PinpointIdCacheConfiguration;
import com.navercorp.pinpoint.common.util.UuidUtils;
import com.navercorp.pinpoint.web.dao.ServiceNameDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.CheckAndMutateResult;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

// serviceUid -> serviceName
@Repository
public class HbaseServiceNameDao implements ServiceNameDao {

    private static final HbaseColumnFamily.ServiceName NAME = HbaseColumnFamily.SERVICE_NAME;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<String> serviceNameMapper;

    public HbaseServiceNameDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider,
                               @Qualifier("serviceNameMapper") RowMapper<String> serviceNameMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.serviceNameMapper = Objects.requireNonNull(serviceNameMapper, "serviceNameMapper");
    }


    @Override
    public List<String> selectAllServiceNames() {
        Scan scan = new Scan();
        scan.addFamily(NAME.getName());
        scan.setCaching(30);

        TableName serviceInfoTableName = tableNameProvider.getTableName(NAME.getTable());
        return hbaseOperations.find(serviceInfoTableName, scan, serviceNameMapper);
    }

    @Override
    @Cacheable(cacheNames = "serviceNameCache", key = "#serviceUid", cacheManager = PinpointIdCacheConfiguration.SERVICE_NAME_CACHE_NAME, unless = "#result == null")
    public String selectServiceName(UUID serviceUid) {
        byte[] rowKey = UuidUtils.toBytes(serviceUid);

        Get get = new Get(rowKey);
        get.addFamily(NAME.getName());

        TableName serviceInfoTableName = tableNameProvider.getTableName(NAME.getTable());

        return hbaseOperations.get(serviceInfoTableName, get, serviceNameMapper);
    }


    @Override
    public boolean insertServiceNameIfNotExists(UUID serviceUid, String serviceName) {
        byte[] rowKey = UuidUtils.toBytes(serviceUid);

        Put put = new Put(rowKey);
        put.addColumn(NAME.getName(), NAME.getName(), Bytes.toBytes(serviceName));

        CheckAndMutate.Builder builder = CheckAndMutate.newBuilder(rowKey);
        builder.ifNotExists(NAME.getName(), NAME.getName());
        CheckAndMutate checkAndMutate = builder.build(put);

        TableName serviceInfoTableName = tableNameProvider.getTableName(NAME.getTable());
        CheckAndMutateResult checkAndMutateResult = hbaseOperations.checkAndMutate(serviceInfoTableName, checkAndMutate);
        return checkAndMutateResult.isSuccess();
    }

    @Override
    @CacheEvict(cacheNames = "serviceNameCache", key = "#serviceUid", cacheManager = PinpointIdCacheConfiguration.SERVICE_NAME_CACHE_NAME)
    public void deleteServiceName(UUID serviceUid) {
        byte[] rowKey = UuidUtils.toBytes(serviceUid);
        Delete delete = new Delete(rowKey);

        TableName ServiceInfoTableName = tableNameProvider.getTableName(NAME.getTable());
        hbaseOperations.delete(ServiceInfoTableName, delete);
    }

}
