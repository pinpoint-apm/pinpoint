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

package com.navercorp.pinpoint.common.hbase.util;

import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author HyunGil Jeong
 */
public class HbaseTableNameCache {

    private final Map<TableNameKey, TableName> tableNameCache = new ConcurrentHashMap<>();

    public TableName get(String qualifier) {
        return get(NamespaceDescriptor.DEFAULT_NAMESPACE_NAME_STR, qualifier);
    }

    public TableName get(String namespace, String qualifier) {
        Objects.requireNonNull(qualifier, "qualifier");
        String nonEmptyNamespace = StringUtils.isEmpty(namespace) ? NamespaceDescriptor.DEFAULT_NAMESPACE_NAME_STR : namespace;

        TableNameKey tableNameKey = new TableNameKey(nonEmptyNamespace, qualifier);
        TableName tableName = tableNameCache.get(tableNameKey);
        if (tableName != null) {
            return tableName;
        }
        tableName = TableName.valueOf(tableNameKey.getNamespace(), tableNameKey.getQualifier());
        TableName prevTableName = tableNameCache.putIfAbsent(tableNameKey, tableName);
        if (prevTableName != null) {
            return prevTableName;
        }
        return tableName;
    }

    private static class TableNameKey {

        private final String namespace;
        private final String qualifier;

        private TableNameKey(String namespace, String qualifier) {
            this.namespace = Objects.requireNonNull(namespace, "namespace");
            this.qualifier = Objects.requireNonNull(qualifier, "qualifier");
        }

        public String getNamespace() {
            return namespace;
        }

        public String getQualifier() {
            return qualifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TableNameKey that = (TableNameKey) o;

            if (!namespace.equals(that.namespace)) return false;
            return qualifier.equals(that.qualifier);
        }

        @Override
        public int hashCode() {
            int result = namespace.hashCode();
            result = 31 * result + qualifier.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return namespace + ":" + qualifier;
        }
    }
}
