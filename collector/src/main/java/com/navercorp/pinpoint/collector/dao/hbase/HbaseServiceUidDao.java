package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.ServiceUidDao;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.config.PinpointIdCacheConfiguration;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.UUID;

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
        byte[] rowKey = BytesUtils.toBytes(serviceName);

        Get get = new Get(rowKey);
        get.addColumn(UID.getName(), UID.getName());

        TableName serviceUidTableName = tableNameProvider.getTableName(UID.getTable());
        return hbaseOperations.get(serviceUidTableName, get, serviceUidMapper);
    }
}
