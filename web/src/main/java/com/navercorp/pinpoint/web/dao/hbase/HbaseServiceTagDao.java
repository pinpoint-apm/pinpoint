package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.UuidUtils;
import com.navercorp.pinpoint.web.dao.ServiceTagDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Repository
public class HbaseServiceTagDao implements ServiceTagDao {

    private static final HbaseColumnFamily.ServiceTag INFO = HbaseColumnFamily.SERVICE_TAG;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<Map<String, String>> serviceInfoMapper;

    public HbaseServiceTagDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider,
                              @Qualifier("serviceTagMapper") RowMapper<Map<String, String>> serviceInfoMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.serviceInfoMapper = Objects.requireNonNull(serviceInfoMapper, "serviceInfoMapper");
    }

    @Override
    public Map<String, String> selectServiceTags(UUID serviceUid) {
        byte[] rowKey = UuidUtils.toBytes(serviceUid);

        Get get = new Get(rowKey);
        get.addFamily(INFO.getName());

        TableName serviceInfoTableName = tableNameProvider.getTableName(INFO.getTable());
        return hbaseOperations.get(serviceInfoTableName, get, serviceInfoMapper);
    }

    @Override
    public void insertServiceTag(UUID serviceUid, Map<String, String> tags) {
        if (tags.isEmpty()) {
            return;
        }
        byte[] rowKey = UuidUtils.toBytes(serviceUid);

        List<Put> puts = new ArrayList<>(tags.size());
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            byte[] qualifier = BytesUtils.toBytes(entry.getKey());
            byte[] value = BytesUtils.toBytes(entry.getValue());

            Put put = new Put(rowKey);
            put.addColumn(INFO.getName(), qualifier, value);

            puts.add(put);
        }

        TableName serviceInfoTableName = tableNameProvider.getTableName(INFO.getTable());
        hbaseOperations.put(serviceInfoTableName, puts);
    }

    @Override
    public void insertServiceTag(UUID serviceUid, String key, String value) {
        insertServiceTag(serviceUid, Map.of(key, value));
    }

    @Override
    public void deleteServiceTag(UUID serviceUid, String key) {
        byte[] rowKey = UuidUtils.toBytes(serviceUid);
        byte[] qualifier = BytesUtils.toBytes(key);

        Delete delete = new Delete(rowKey);
        delete.addColumn(INFO.getName(), qualifier);

        TableName serviceInfoTableName = tableNameProvider.getTableName(INFO.getTable());
        hbaseOperations.delete(serviceInfoTableName, delete);
    }

    @Override
    public void deleteAllServiceTags(UUID serviceUid) {
        byte[] rowKey = UuidUtils.toBytes(serviceUid);

        Delete delete = new Delete(rowKey);

        TableName serviceInfoTableName = tableNameProvider.getTableName(INFO.getTable());
        hbaseOperations.delete(serviceInfoTableName, delete);
    }
}
