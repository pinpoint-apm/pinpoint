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

package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Increment;

import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public interface BulkIncrementer {

    void increment(TableName tableName, RowKey rowKey, ColumnName columnName);

    void increment(TableName tableName, RowKey rowKey, ColumnName columnName, long addition);

    Map<TableName, List<Increment>> getIncrements(RowKeyDistributorByHashPrefix rowKeyDistributor);

    int getSize();

}