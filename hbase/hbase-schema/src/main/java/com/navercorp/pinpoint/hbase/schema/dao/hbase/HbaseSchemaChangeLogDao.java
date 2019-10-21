/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.hbase.schema.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseAdminOperation;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.hbase.schema.dao.hbase.codec.SchemaChangeLogCodec;
import com.navercorp.pinpoint.hbase.schema.domain.SchemaChangeLog;
import com.navercorp.pinpoint.hbase.schema.dao.SchemaChangeLogDao;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class HbaseSchemaChangeLogDao implements SchemaChangeLogDao {

    public static final String TABLE_QUALIFIER = "SchemaChangeLog";
    public static final byte[] COLUMN_FAMILY_NAME = Bytes.toBytes("Log");
    public static final byte[] COLUMN_QUALIFIER = Bytes.toBytes("L");

    private final HbaseAdminOperation hbaseAdminOperation;

    private final HbaseOperations2 hbaseOperations2;

    private final SchemaChangeLogCodec schemaChangeLogCodec;

    private final RowMapper<SchemaChangeLog> schemaChangeLogRowMapper;

    private final ResultsExtractor<List<SchemaChangeLog>> schemaChangeLogResultsExtractor;

    public HbaseSchemaChangeLogDao(HbaseAdminOperation hbaseAdminOperation,
                                   HbaseOperations2 hbaseOperations2,
                                   SchemaChangeLogCodec schemaChangeLogCodec) {
        this.hbaseAdminOperation = Objects.requireNonNull(hbaseAdminOperation, "hbaseAdminOperation");
        this.hbaseOperations2 = Objects.requireNonNull(hbaseOperations2, "hbaseOperations2");
        this.schemaChangeLogCodec = Objects.requireNonNull(schemaChangeLogCodec, "schemaChangeLogCodec");
        this.schemaChangeLogRowMapper = new SchemaChangeLogRowMapper(this.schemaChangeLogCodec);
        this.schemaChangeLogResultsExtractor = new SchemaChangeLogResultsExtractor(this.schemaChangeLogRowMapper);
    }

    @Override
    public String getSchemaChangeLogTableName() {
        return TABLE_QUALIFIER;
    }

    @Override
    public boolean tableExists(String namespace) {
        TableName tableName = TableName.valueOf(namespace, TABLE_QUALIFIER);
        return hbaseAdminOperation.tableExists(tableName);
    }

    @Override
    public boolean createTable(String namespace) {
        TableName tableName = TableName.valueOf(namespace, TABLE_QUALIFIER);
        HTableDescriptor htd = new HTableDescriptor(tableName);
        HColumnDescriptor hcd = new HColumnDescriptor(COLUMN_FAMILY_NAME);
        htd.addFamily(hcd);
        return hbaseAdminOperation.createTableIfNotExists(htd);
    }

    @Override
    public boolean resetTable(String namespace) {
        TableName tableName = TableName.valueOf(namespace, TABLE_QUALIFIER);
        return hbaseAdminOperation.truncateTable(tableName, false);
    }

    @Override
    public void insertChangeLog(String namespace, SchemaChangeLog schemaChangeLog) {
        TableName tableName = TableName.valueOf(namespace, TABLE_QUALIFIER);
        byte[] rowKey = Bytes.toBytes(schemaChangeLog.getId());
        Put put = new Put(rowKey);
        byte[] value = schemaChangeLogCodec.writeData(schemaChangeLog);
        put.addColumn(COLUMN_FAMILY_NAME, COLUMN_QUALIFIER, value);
        hbaseOperations2.put(tableName, put);
    }

    @Override
    public List<SchemaChangeLog> getChangeLogs(String namespace) {
        TableName tableName = TableName.valueOf(namespace, TABLE_QUALIFIER);
        Scan scan = new Scan();
        scan.addColumn(COLUMN_FAMILY_NAME, COLUMN_QUALIFIER);
        return hbaseOperations2.find(tableName, scan, schemaChangeLogResultsExtractor);
    }

    @Override
    public SchemaChangeLog getChangeLog(String namespace, String id) {
        TableName tableName = TableName.valueOf(namespace, TABLE_QUALIFIER);
        byte[] rowKey = Bytes.toBytes(id);
        Get get = new Get(rowKey);
        return hbaseOperations2.get(tableName, get, schemaChangeLogRowMapper);
    }

    private static class SchemaChangeLogRowMapper implements RowMapper<SchemaChangeLog> {

        private final SchemaChangeLogCodec schemaChangeLogCodec;

        private SchemaChangeLogRowMapper(SchemaChangeLogCodec schemaChangeLogCodec) {
            this.schemaChangeLogCodec = Objects.requireNonNull(schemaChangeLogCodec, "schemaChangeLogCodec");
        }

        @Override
        public SchemaChangeLog mapRow(Result result, int rowNum) throws Exception {
            if (result.isEmpty()) {
                return null;
            }
            byte[] serializedSchemaChangeLog = result.getValue(HbaseSchemaChangeLogDao.COLUMN_FAMILY_NAME, HbaseSchemaChangeLogDao.COLUMN_QUALIFIER);
            return schemaChangeLogCodec.readData(serializedSchemaChangeLog);
        }
    }

    private static class SchemaChangeLogResultsExtractor implements ResultsExtractor<List<SchemaChangeLog>> {

        private final RowMapper<SchemaChangeLog> schemaChangeLogRowMapper;

        private SchemaChangeLogResultsExtractor(RowMapper<SchemaChangeLog> schemaChangeLogRowMapper) {
            this.schemaChangeLogRowMapper = Objects.requireNonNull(schemaChangeLogRowMapper, "schemaChangeLogRowMapper");
        }

        @Override
        public List<SchemaChangeLog> extractData(ResultScanner results) throws Exception {
            List<SchemaChangeLog> schemaChangeLogs = new ArrayList<>();
            int rowNum = 0;
            for (Result result : results) {
                SchemaChangeLog schemaChangeLog = schemaChangeLogRowMapper.mapRow(result, rowNum++);
                if (schemaChangeLog != null) {
                    schemaChangeLogs.add(schemaChangeLog);
                }
            }
            return schemaChangeLogs;
        }
    }
}
