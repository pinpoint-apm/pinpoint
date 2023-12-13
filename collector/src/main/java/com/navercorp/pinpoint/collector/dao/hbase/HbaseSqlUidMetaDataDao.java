package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.SqlUidMetaDataDao;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.SqlUidMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid.UidMetaDataRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid.UidMetadataEncoder;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class HbaseSqlUidMetaDataDao implements SqlUidMetaDataDao {
    private static final HbaseColumnFamily.SqlUidMetaData descriptor = HbaseColumnFamily.SQL_UID_METADATA_SQL;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;

    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    private final RowKeyEncoder<UidMetaDataRowKey> rowKeyEncoder = new UidMetadataEncoder();

    public HbaseSqlUidMetaDataDao(HbaseOperations hbaseTemplate,
                                  TableNameProvider tableNameProvider,
                                  @Qualifier("metadataRowKeyDistributor2") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.rowKeyDistributorByHashPrefix = Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
    }

    @Override
    public void insert(SqlUidMetaDataBo sqlUidMetaData) {
        Objects.requireNonNull(sqlUidMetaData, "sqlUidMetaData");
        if (logger.isDebugEnabled()) {
            logger.debug("insert:{}", sqlUidMetaData);
        }

        // Assert agentId
        CollectorUtils.checkAgentId(sqlUidMetaData.getAgentId());

        final byte[] rowKey = getDistributedKey(rowKeyEncoder.encodeRowKey(sqlUidMetaData));
        final Put put = new Put(rowKey);
        final String sql = sqlUidMetaData.getSql();
        final byte[] sqlBytes = Bytes.toBytes(sql);
        put.addColumn(descriptor.getName(), descriptor.QUALIFIER_SQLSTATEMENT, sqlBytes);

        final TableName sqlUidMetaDataTableName = tableNameProvider.getTableName(descriptor.getTable());
        hbaseTemplate.put(sqlUidMetaDataTableName, put);
    }

    private byte[] getDistributedKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getDistributedKey(rowKey);
    }
}
