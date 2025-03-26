package com.navercorp.pinpoint.web.uid.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.ApplicationUidRowKeyUtils;
import com.navercorp.pinpoint.web.uid.dao.ApplicationNameDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "pinpoint.web.application.uid.enable", havingValue = "true")
public class HbaseApplicationNameDao implements ApplicationNameDao {

    private static final HbaseColumnFamily NAME = HbaseTables.APPLICATION_NAME;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<String> applicationNameValueMapper;

    public HbaseApplicationNameDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider,
                                   @Qualifier("applicationNameValueMapper") RowMapper<String> applicationNameValueMapper) {
        this.hbaseOperations = hbaseOperations;
        this.tableNameProvider = tableNameProvider;
        this.applicationNameValueMapper = applicationNameValueMapper;
    }

    @Override
    public String selectApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKey = ApplicationUidRowKeyUtils.makeRowKey(serviceUid, applicationUid);

        Get get = new Get(rowKey);
        get.addColumn(NAME.getName(), NAME.getName());

        TableName applicationNameTableName = tableNameProvider.getTableName(NAME.getTable());
        return hbaseOperations.get(applicationNameTableName, get, applicationNameValueMapper);
    }

    @Override
    public void deleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        byte[] rowKey = ApplicationUidRowKeyUtils.makeRowKey(serviceUid, applicationUid);

        Delete delete = new Delete(rowKey);

        TableName applicationNameTableName = tableNameProvider.getTableName(NAME.getTable());
        hbaseOperations.delete(applicationNameTableName, delete);
    }
}
