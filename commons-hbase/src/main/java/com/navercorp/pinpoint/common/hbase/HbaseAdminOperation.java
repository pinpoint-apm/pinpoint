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

package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptor;

import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public interface HbaseAdminOperation {

    boolean createNamespaceIfNotExists(String namespace);

    boolean createNamespaceIfNotExists(String namespace, Map<String, String> configurations);

    List<TableDescriptor> getTableDescriptors(String namespace);

    TableDescriptor getTableDescriptor(TableName tableName);

    void createTable(TableDescriptor tableDescriptor);

    void createTable(TableDescriptor tableDescriptor, byte[][] splitKeys);

    boolean createTableIfNotExists(TableDescriptor tableDescriptor);

    boolean tableExists(TableName tableName);

    boolean truncateTable(TableName tableName, boolean preserveSplits);

    boolean dropTableIfExists(TableName tableName);

    void dropTable(TableName tableName);

    void modifyTable(TableDescriptor tableDescriptor);

    void addColumn(TableName tableName, ColumnFamilyDescriptor columnDescriptor);

    <T> T execute(AdminCallback<T> action);
}
