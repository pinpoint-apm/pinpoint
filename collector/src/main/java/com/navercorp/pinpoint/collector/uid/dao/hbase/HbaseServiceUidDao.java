package com.navercorp.pinpoint.collector.uid.dao.hbase;

import com.navercorp.pinpoint.collector.uid.dao.ServiceUidDao;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
@ConditionalOnProperty(name = "pinpoint.collector.v4.enable", havingValue = "true")
public class HbaseServiceUidDao implements ServiceUidDao {

    private static final HbaseColumnFamily UID = HbaseTables.SERVICE_UID;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<ServiceUid> serviceUidMapper;

    public HbaseServiceUidDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider,
                              @Qualifier("serviceUidMapper") RowMapper<ServiceUid> serviceUidMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.serviceUidMapper = Objects.requireNonNull(serviceUidMapper, "serviceUidMapper");
    }

    @Override
    public ServiceUid selectServiceUid(String serviceName) {
        byte[] rowKey = Bytes.toBytes(serviceName);

        Get get = new Get(rowKey);
        get.addColumn(UID.getName(), UID.getName());

        TableName serviceUidTableName = tableNameProvider.getTableName(UID.getTable());
        return hbaseOperations.get(serviceUidTableName, get, serviceUidMapper);
    }
}
