/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.TableName;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultTableDescriptor<T extends HbaseColumnFamily> implements TableDescriptor<T> {

    private final TableNameProvider tableNameProvider;
    private final T hbaseColumnFamily;

    public DefaultTableDescriptor(TableNameProvider tableNameProvider, T hbaseColumnFamily) {
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.hbaseColumnFamily = Objects.requireNonNull(hbaseColumnFamily, "hbaseColumnFamily");
    }

    @Override
    public final byte[] getColumnFamilyName() {
        return hbaseColumnFamily.getName();
    }

    @Override
    public final TableName getTableName() {
        return tableNameProvider.getTableName(getColumnFamily().getTable());
    }

    @Override
    public T getColumnFamily() {
        return hbaseColumnFamily;
    }
}