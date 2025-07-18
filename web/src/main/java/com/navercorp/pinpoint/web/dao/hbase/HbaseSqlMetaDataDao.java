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
import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.DefaultMetaDataRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetaDataRowKey;
import com.navercorp.pinpoint.web.dao.SqlMetaDataDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author minwoo.jung
 */
@Repository
public class HbaseSqlMetaDataDao implements SqlMetaDataDao {

    private final HbaseTables.SqlMetadataV2 DESCRIPTOR = HbaseTables.SQL_METADATA_VER2_SQL;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<List<SqlMetaDataBo>> sqlMetaDataMapper;

    private final RowKeyEncoder<MetaDataRowKey> rowKeyEncoder;

    public HbaseSqlMetaDataDao(HbaseOperations hbaseOperations,
                               @Qualifier("sqlDataRowKeyEncoder")
                               RowKeyEncoder<MetaDataRowKey> rowKeyEncoder,
                               TableNameProvider tableNameProvider,
                               @Qualifier("sqlMetaDataMapper")
                               RowMapper<List<SqlMetaDataBo>> sqlMetaDataMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.rowKeyEncoder = Objects.requireNonNull(rowKeyEncoder, "rowKeyEncoder");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.sqlMetaDataMapper = Objects.requireNonNull(sqlMetaDataMapper, "sqlMetaDataMapper");
    }

    @Override
    public List<SqlMetaDataBo> getSqlMetaData(String agentId, long time, int sqlId) {
        Objects.requireNonNull(agentId, "agentId");

        MetaDataRowKey metaDataRowKey = new DefaultMetaDataRowKey(agentId, time, sqlId);
        byte[] rowKey = rowKeyEncoder.encodeRowKey(metaDataRowKey);

        Get get = new Get(rowKey);
        get.addFamily(DESCRIPTOR.getName());

        TableName sqlMetaDataTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseOperations.get(sqlMetaDataTableName, get, sqlMetaDataMapper);
    }
}
