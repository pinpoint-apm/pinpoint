/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.hbase.schema.core;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class HtdHbaseSchemaVerifier implements HbaseSchemaVerifier<TableDescriptor> {

    /**
     * Returns {@code true} if the schema definitions specified by {@code expectedSchemas} matches those
     * specified by {@code actualSchemas}.
     * <p>This implementation compares hbase schemas using {@link TableDescriptor}. Note that the expected schema and
     * the actual schema do not have to match exactly - there may be additional tables and column families in the actual
     * schema, and the method returns {@code true} as long as all tables and column families from the expected schema
     * are present.
     *
     * @param expectedSchemas expected table descriptors
     * @param actualSchemas actual table descriptors
     * @return {@code true} if the actual schema matches the expected schema
     */
    @Override
    public boolean verifySchemas(List<TableDescriptor> expectedSchemas, List<TableDescriptor> actualSchemas) {
        if (CollectionUtils.isEmpty(expectedSchemas)) {
            return true;
        }
        if (CollectionUtils.isEmpty(actualSchemas)) {
            return false;
        }

        Map<TableName, TableDescriptor> actualSchemaMap = new HashMap<>();
        for (TableDescriptor actualSchema : actualSchemas) {
            actualSchemaMap.put(actualSchema.getTableName(), actualSchema);
        }

        for (TableDescriptor expectedSchema : expectedSchemas) {
            TableName tableName = expectedSchema.getTableName();
            TableDescriptor actualSchema = actualSchemaMap.get(tableName);
            if (actualSchema == null) {
                return false;
            }
            if (!verifySchema(expectedSchema, actualSchema)) {
                return false;
            }
        }
        return true;
    }

    private boolean verifySchema(TableDescriptor expected, TableDescriptor actual) {
        if (!expected.getTableName().equals(actual.getTableName())) {
            return false;
        }
        for (ColumnFamilyDescriptor expectedHcd : expected.getColumnFamilies()) {
            if (!actual.hasColumnFamily(expectedHcd.getName())) {
                return false;
            }
        }
        return true;
    }
}
