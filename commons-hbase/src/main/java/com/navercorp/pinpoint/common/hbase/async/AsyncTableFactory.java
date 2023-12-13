/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.hbase.async;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.AdvancedScanResultConsumer;
import org.apache.hadoop.hbase.client.AsyncBufferedMutator;
import org.apache.hadoop.hbase.client.AsyncTable;
import org.apache.hadoop.hbase.client.ScanResultConsumer;

import java.util.concurrent.ExecutorService;

public interface AsyncTableFactory {

    AsyncTable<AdvancedScanResultConsumer> getTable(TableName tableName);


    /**
     * Creates a new AsyncTable.
     *
     * @param tableName name of the AsyncTable.
     * @return Table instance.
     */
    AsyncTable<ScanResultConsumer> getTable(TableName tableName, ExecutorService pool);


    AsyncBufferedMutator getBufferedMutator(TableName tableName, ExecutorService pool);

    AsyncBufferedMutator getBufferedMutator(TableName tableName);
}
