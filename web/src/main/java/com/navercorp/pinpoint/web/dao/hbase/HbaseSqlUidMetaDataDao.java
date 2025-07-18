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

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.SqlUidMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid.DefaultUidMetaDataRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid.UidMetaDataRowKey;
import com.navercorp.pinpoint.web.dao.SqlUidMetaDataDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class HbaseSqlUidMetaDataDao implements SqlUidMetaDataDao {
    private final HbaseTables.SqlUidMetaData DESCRIPTOR = HbaseTables.SQL_UID_METADATA_SQL;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<List<SqlUidMetaDataBo>> sqlUidMetaDataMapper;

    private final RowKeyEncoder<UidMetaDataRowKey> rowKeyEncoder;

    public HbaseSqlUidMetaDataDao(HbaseOperations hbaseOperations,
                                  RowKeyEncoder<UidMetaDataRowKey> rowKeyEncoder,
                                  TableNameProvider tableNameProvider,
                                  @Qualifier("sqlUidMetaDataMapper") RowMapper<List<SqlUidMetaDataBo>> sqlUidMetaDataMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.rowKeyEncoder = Objects.requireNonNull(rowKeyEncoder, "rowKeyEncoder");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.sqlUidMetaDataMapper = Objects.requireNonNull(sqlUidMetaDataMapper, "sqlUidMetaDataMapper");
    }

    @Override
    public List<SqlUidMetaDataBo> getSqlUidMetaData(String agentId, long time, byte[] sqlUid) {
        Objects.requireNonNull(agentId, "agentId");

        UidMetaDataRowKey uidMetaDataRowKey = new DefaultUidMetaDataRowKey(agentId, time, sqlUid);
        byte[] rowKey = rowKeyEncoder.encodeRowKey(uidMetaDataRowKey);

        Get get = new Get(rowKey);
        get.addFamily(DESCRIPTOR.getName());

        TableName sqlUidMetaDataTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseOperations.get(sqlUidMetaDataTableName, get, sqlUidMetaDataMapper);
    }

}
