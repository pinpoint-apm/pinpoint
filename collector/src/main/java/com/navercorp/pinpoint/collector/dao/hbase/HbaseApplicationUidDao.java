package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.config.CollectorApplicationIdCacheConfig;
import com.navercorp.pinpoint.collector.dao.ApplicationUidDao;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.ApplicationUidRowKeyUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.CheckAndMutateResult;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class HbaseApplicationUidDao implements ApplicationUidDao {

    private static final HbaseColumnFamily.ApplicationUid APPLICATION_ID = HbaseColumnFamily.APPLICATION_UID;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<ApplicationUid> applicationIdMapper;

    public HbaseApplicationUidDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider,
                                  @Qualifier("applicationUidMapper") RowMapper<ApplicationUid> applicationIdMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.applicationIdMapper = Objects.requireNonNull(applicationIdMapper, "applicationIdMapper");
    }

    @Override
    @Cacheable(cacheNames = "applicationUidCache", key = "{#serviceUid, #applicationName}", cacheManager = CollectorApplicationIdCacheConfig.APPLICATION_UID_CACHE_NAME, unless = "#result == null")
    public ApplicationUid selectApplicationUid(ServiceUid serviceUid, String applicationName) {
        byte[] rowKey = ApplicationUidRowKeyUtils.makeRowKey(serviceUid, applicationName);

        Get get = new Get(rowKey);
        get.addColumn(APPLICATION_ID.getName(), APPLICATION_ID.getName());

        TableName applicationIdTableName = tableNameProvider.getTableName(APPLICATION_ID.getTable());
        ApplicationUid applicationUid = hbaseOperations.get(applicationIdTableName, get, applicationIdMapper);

        return applicationUid;
    }

    @Override
    public boolean insertApplicationUidIfNotExists(ServiceUid serviceUid, String applicationName, ApplicationUid applicationUid) {
        byte[] rowKey = ApplicationUidRowKeyUtils.makeRowKey(serviceUid, applicationName);

        Put put = new Put(rowKey);
        put.addColumn(APPLICATION_ID.getName(), APPLICATION_ID.getName(), Bytes.toBytes(applicationUid.getUid()));

        CheckAndMutate.Builder builder = CheckAndMutate.newBuilder(rowKey);
        builder.ifNotExists(APPLICATION_ID.getName(), APPLICATION_ID.getName());
        CheckAndMutate checkAndMutate = builder.build(put);

        TableName applicationIdTableName = tableNameProvider.getTableName(APPLICATION_ID.getTable());
        CheckAndMutateResult checkAndMutateResult = hbaseOperations.checkAndMutate(applicationIdTableName, checkAndMutate);
        return checkAndMutateResult.isSuccess();
    }
}
