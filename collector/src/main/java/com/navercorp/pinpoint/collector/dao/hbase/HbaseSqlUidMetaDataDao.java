/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.SqlUidMetaDataDao;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.SqlUidMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid.UidMetaDataRowKey;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class HbaseSqlUidMetaDataDao implements SqlUidMetaDataDao {
    private static final HbaseTables.SqlUidMetaData descriptor = HbaseTables.SQL_UID_METADATA_SQL;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;

    private final RowKeyEncoder<UidMetaDataRowKey> rowKeyEncoder;

    public HbaseSqlUidMetaDataDao(HbaseOperations hbaseTemplate,
                                  RowKeyEncoder<UidMetaDataRowKey> rowKeyEncoder,
                                  TableNameProvider tableNameProvider) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.rowKeyEncoder = Objects.requireNonNull(rowKeyEncoder, "rowKeyEncoder");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    @Override
    public void insert(SqlUidMetaDataBo sqlUidMetaData) {
        Objects.requireNonNull(sqlUidMetaData, "sqlUidMetaData");
        if (logger.isDebugEnabled()) {
            logger.debug("insert:{}", sqlUidMetaData);
        }

        byte[] rowKey = rowKeyEncoder.encodeRowKey(sqlUidMetaData);

        final Put put = new Put(rowKey, true);
        final String sql = sqlUidMetaData.getSql();
        final byte[] sqlBytes = Bytes.toBytes(sql);
        put.addColumn(descriptor.getName(), descriptor.QUALIFIER_SQLSTATEMENT, sqlBytes);

        final TableName sqlUidMetaDataTableName = tableNameProvider.getTableName(descriptor.getTable());
        hbaseTemplate.put(sqlUidMetaDataTableName, put);
    }
}
