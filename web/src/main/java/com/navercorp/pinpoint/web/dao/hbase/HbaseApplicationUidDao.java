package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.util.ApplicationUidRowKeyUtils;
import com.navercorp.pinpoint.common.server.vo.ApplicationIdentifier;
import com.navercorp.pinpoint.common.server.vo.ApplicationUid;
import com.navercorp.pinpoint.common.server.vo.ServiceUid;
import com.navercorp.pinpoint.web.dao.ApplicationUidDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Scan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ConditionalOnProperty(name = "pinpoint.web.application.uid.enable", havingValue = "true")
public class HbaseApplicationUidDao implements ApplicationUidDao {

    private final HbaseColumnFamily.ApplicationUid ID = HbaseColumnFamily.APPLICATION_UID;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<ApplicationIdentifier> applicationIdentifierMapper;
    private final RowMapper<ApplicationUid> applicationIdMapper;

    public HbaseApplicationUidDao(HbaseOperations hbaseOperations, TableNameProvider tableNameProvider,
                                  @Qualifier("applicationIdentifierMapper") RowMapper<ApplicationIdentifier> applicationIdentifierMapper,
                                  @Qualifier("applicationUidMapper") RowMapper<ApplicationUid> applicationIdMapper) {
        this.hbaseOperations = hbaseOperations;
        this.tableNameProvider = tableNameProvider;
        this.applicationIdentifierMapper = applicationIdentifierMapper;
        this.applicationIdMapper = applicationIdMapper;
    }

    @Override
    public List<ApplicationIdentifier> selectApplicationIds(String applicationName) {
        byte[] rowKeyPrefix = ApplicationUidRowKeyUtils.makeRowKey(applicationName);

        Scan scan = new Scan();
        scan.setCaching(30);
        scan.setStartStopRowForPrefixScan(rowKeyPrefix);
        scan.addColumn(ID.getName(), ID.getName());

        TableName applicationIdTableName = tableNameProvider.getTableName(ID.getTable());
        return hbaseOperations.find(applicationIdTableName, scan, applicationIdentifierMapper);
    }

    @Override
    public ApplicationUid selectApplicationId(ServiceUid serviceUid, String applicationName) {
        byte[] rowKey = ApplicationUidRowKeyUtils.makeRowKey(serviceUid, applicationName);

        Get get = new Get(rowKey);
        get.addColumn(ID.getName(), ID.getName());

        TableName applicationIdTableName = tableNameProvider.getTableName(ID.getTable());
        return hbaseOperations.get(applicationIdTableName, get, applicationIdMapper);
    }

    @Override
    public void deleteApplicationId(ServiceUid serviceUid, String applicationName) {
        byte[] rowKey = ApplicationUidRowKeyUtils.makeRowKey(serviceUid, applicationName);

        Delete delete = new Delete(rowKey);
        delete.addColumn(ID.getName(), ID.getName());

        TableName applicationIdTableName = tableNameProvider.getTableName(ID.getTable());
        hbaseOperations.delete(applicationIdTableName, delete);
    }
}
