package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.ApplicationDao;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.util.ServiceGroupRowKeyPrefixUtils;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class HbaseApplicationDao implements ApplicationDao {
    private static final HbaseColumnFamily DESCRIPTOR = HbaseTables.APPLICATION;
    private static final byte[] PREFIXED_EMPTY_VALUE = new byte[1];

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;

    public HbaseApplicationDao(HbaseOperations hbaseTemplate, TableNameProvider tableNameProvider) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    @Override
    public void insert(int serviceUid, String applicationName, int serviceTypeCode) {
        byte[] rowKey = ServiceGroupRowKeyPrefixUtils.createRowKey(serviceUid, applicationName, serviceTypeCode);
        final Put put = new Put(rowKey, true);
        put.addColumn(DESCRIPTOR.getName(), DESCRIPTOR.getName(), PREFIXED_EMPTY_VALUE);

        final TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseTemplate.put(applicationIndexTableName, put);
    }
}
